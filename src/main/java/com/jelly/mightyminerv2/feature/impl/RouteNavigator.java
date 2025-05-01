package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.util.AngleUtil;
import com.jelly.mightyminerv2.util.BlockUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration.RotationType;
import com.jelly.mightyminerv2.util.helper.Target;
import com.jelly.mightyminerv2.util.helper.route.Route;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.util.helper.route.TransportMethod;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

// This works under the assumption that the blocks between every node are clear and traversable.
// Checks to determine if the player can move between two nodes must be done beforehand; otherwise, it will cause bugs.

public class RouteNavigator extends AbstractFeature {

    private static RouteNavigator instance;
    private Route routeToFollow;
    private int currentRouteIndex = -1;
    private int targetRouteIndex = -1;
    private State state = State.STARTING;
    private boolean isQueued = false;
    @Getter
    private NavError navError = NavError.NONE;
    @Setter
    private RotationType rotationType = RotationType.CLIENT;
    private Vec3 rotationTarget = null;

    public static RouteNavigator getInstance() {
        if (instance == null) {
            instance = new RouteNavigator();
        }
        return instance;
    }

    @Override
    public String getName() {
        return "RouteNavigator";
    }

    @Override
    public void resetStatesAfterStop() {
        this.state = State.STARTING;
        this.rotationType = RotationType.CLIENT;
        KeyBindUtil.releaseAllExcept();
        RotationHandler.getInstance().stop();
    }

    @Override
    public boolean shouldNotCheckForFailsafe() {
        return this.state == State.AOTV_VERIFY;
    }

    public void queueRoute(final Route routeToFollow) {
        if (this.enabled) {
            return;
        }
        this.routeToFollow = routeToFollow;
        this.currentRouteIndex = -1;
        this.targetRouteIndex = -1;
        this.isQueued = true;
    }

    public void goTo(final int index) {
        if (this.routeToFollow == null || this.routeToFollow.isEmpty()) {
            sendError("No Route Was Selected or its empty.");
            return;
        }
        this.targetRouteIndex = index;
        this.currentRouteIndex = this.getCurrentIndex(PlayerUtil.getBlockStandingOn()) - 1;
        this.normalizeIndices();
        this.navError = NavError.NONE;
        this.enabled = true;
        this.start();
    }

    public void gotoNext() {
        this.goTo(this.targetRouteIndex + 1);
    }

    public void start(final Route routeToFollow) {
        this.routeToFollow = routeToFollow;
        this.enabled = true;
        this.targetRouteIndex = -1;
        this.normalizeIndices();
        this.currentRouteIndex = -1;
        this.navError = NavError.NONE;

        this.start();
        send("Enabling RouteNavigator.");
    }

    public void pause() {
        this.enabled = false;
        this.timer.reset();
        this.resetStatesAfterStop();

        send("Pausing RouteNavigator");
    }

    @Override
    public void stop() {
        if (!this.enabled) {
            return;
        }

        this.enabled = false;
        this.isQueued = false;
        this.routeToFollow = null;
        this.timer.reset();
        this.targetRouteIndex = -1;
        this.currentRouteIndex = -1;
        this.rotationTarget = null;
        this.resetStatesAfterStop();

        send("RouteNavigator Stopped");
    }

    public void stop(NavError error) {
        this.navError = error;
        this.stop();
    }

    private void normalizeIndices() {
        this.targetRouteIndex = this.normalizeIndex(this.targetRouteIndex);
        this.currentRouteIndex = this.normalizeIndex(this.currentRouteIndex);
        if (this.targetRouteIndex < this.currentRouteIndex) {
            this.targetRouteIndex += this.routeToFollow.size();
        }
    }

    private int normalizeIndex(final int index) {
        return (index + this.routeToFollow.size()) % this.routeToFollow.size();
    }

    private int getLookTime(final RouteWaypoint waypoint) {
        if (this.rotationType == RotationType.SERVER) {
            return MightyMinerConfig.delayAutoAotvServerRotation;
        }
        if (waypoint.getTransportMethod().ordinal() == 0) {
            return MightyMinerConfig.delayAutoAotvLookDelay;
        }
        return MightyMinerConfig.delayAutoAotvEtherwarpLookDelay;
    }

    public int getCurrentIndex(final BlockPos playerBlock) {
        int index = this.routeToFollow.indexOf(new RouteWaypoint(playerBlock, TransportMethod.ETHERWARP));
        if (index != -1) {
            return index;
        }
        return this.routeToFollow.getClosest(playerBlock).map(routeWaypoint -> this.routeToFollow.indexOf(routeWaypoint)).orElse(-1);
    }

    public boolean succeeded() {
        return !this.enabled && this.navError == NavError.NONE;
    }

    @SubscribeEvent
    protected void onTick(ClientTickEvent event) {
        if (!this.enabled) {
            return;
        }

        switch (this.state) {
            case STARTING: {
                this.swapState(State.DETECT_ROUTE, 0);
                break;
            }
            case DETECT_ROUTE: {
                if (this.currentRouteIndex++ == this.targetRouteIndex) {
                    this.swapState(State.END_VERIFY, 0);
                    return;
                }
                if (this.routeToFollow.get(this.currentRouteIndex).getTransportMethod() == TransportMethod.WALK) {
                    this.swapState(State.WALK, 0);
                } else {
                    this.swapState(State.ROTATION, 0);
                }

                log("Going To Index: " + this.currentRouteIndex);
                break;
            }
            case ROTATION: {
                RouteWaypoint nextPoint = this.routeToFollow.get(this.currentRouteIndex);

                if (nextPoint.getTransportMethod() == TransportMethod.ETHERWARP) {
                    BlockPos block = nextPoint.toBlockPos();

                    if (mc.theWorld.getBlockState(block).getBlock() == Blocks.snow_layer) {
                        block = block.down();
                    }

                    this.rotationTarget = BlockUtil.getClosestVisibleSidePos(block);
                } else this.rotationTarget = BlockUtil.getClosestVisibleSidePos(nextPoint.toBlockPos());

                RotationConfiguration config = new RotationConfiguration(new Target(this.rotationTarget),
                        this.getLookTime(nextPoint),
                        this.rotationType,
                        null)
                        .followTarget(true);

                RotationHandler.getInstance().easeTo(config);
                this.swapState(State.ROTATION_VERIFY, 2000);
                log("Rotating to " + this.rotationTarget);
                break;
            }
            case ROTATION_VERIFY: {
                if (this.timer.isScheduled() && this.timer.passed()) {
                    sendError("Could not look in time. Disabling.");
                    this.stop(NavError.TIME_FAIL);
                    return;
                }

                if (!AngleUtil.isLookingAt(this.rotationTarget, 0.5f) && !RotationHandler.getInstance().isFollowingTarget()) {
                    return;
                }

                System.out.println("IsLookingAt: " + AngleUtil.isLookingAtDebug(this.rotationTarget, 0.5f));
                System.out.println("Following: " + RotationHandler.getInstance().isFollowingTarget());
                int sneakTime = 0;

                RouteWaypoint target = this.routeToFollow.get(this.currentRouteIndex);
                if (target.getTransportMethod() == TransportMethod.ETHERWARP && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, true);
                    sneakTime = 250;
                }

                this.swapState(State.AOTV, sneakTime);
                break;
            }
            case AOTV: {
                if (this.timer.isScheduled() && !this.timer.passed()) {
                    return;
                }
                // Todo: test Etherwarp
                if (this.routeToFollow.get(this.currentRouteIndex + 1).getTransportMethod() != TransportMethod.ETHERWARP) {
                    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, false);
                }
                KeyBindUtil.rightClick();
                System.out.println("clicked");
                this.swapState(State.AOTV_VERIFY, 2000);
                break;
            }
            case AOTV_VERIFY: {
                if (this.timer.isScheduled() && this.timer.passed()) {
                    sendError("Did not receive teleport packet in time. Disabling");
                    this.stop(NavError.TIME_FAIL);
                    return;
                }
                break;
            }
            case WALK:
                BlockPos source = this.routeToFollow.get(this.currentRouteIndex).toBlockPos();
                if (this.currentRouteIndex == 0) {
                    log("queued first");
                    BlockPos p = PlayerUtil.getBlockStandingOn();
                    Pathfinder.getInstance().queue(p, source);
                }
                if (this.currentRouteIndex + 1 <= this.targetRouteIndex) {
                    log("queued next");
                    RouteWaypoint target = this.routeToFollow.get(this.currentRouteIndex + 1);
                    Pathfinder.getInstance().queue(source, target.toBlockPos());
                }
                if (!Pathfinder.getInstance().isRunning()) {
                    log("Started");
                    Pathfinder.getInstance().setInterpolationState(true);
                    Pathfinder.getInstance().start();
                }
                this.swapState(State.WALK_VERIFY, 2000);
                log("Walking");
                break;
            case WALK_VERIFY:
                BlockPos targetPos = this.routeToFollow.get(this.currentRouteIndex).toBlockPos();
                if (Pathfinder.getInstance().completedPathTo(targetPos) || (!Pathfinder.getInstance().isRunning() && Pathfinder.getInstance().succeeded()) || PlayerUtil.getBlockStandingOn().equals(targetPos)) {
                    log("Completed path. going to next");
                    this.swapState(State.STARTING, 0);
                    log("Done Walking");
                    return;
                }

                if (Pathfinder.getInstance().failed()) {
                    sendError("Pathfinding failed");
                    this.stop(NavError.PATHFIND_FAILED);
                    return;
                }

                break;
            case END_VERIFY: {
                // Todo: add something to verify if player is at the end or not preferably something that checks for distance from end

                if (this.isQueued) {
                    this.pause();
                } else {
                    this.stop();
                }
                break;
            }
        }
    }

    @SubscribeEvent
    protected void onPacketReceive(PacketEvent.Received event) {
        if (!this.enabled) {
            return;
        }
        if (this.state != State.AOTV_VERIFY) {
            return;
        }
        if (!(event.packet instanceof S08PacketPlayerPosLook)) {
            return;
        }

        this.swapState(State.STARTING, 0);
        S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook) event.packet;
        RotationHandler.getInstance().stop();

        Vec3 playerPos = mc.thePlayer.getPositionVector();
        Vec3 pos = new Vec3(
                packet.getX() + (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.X) ? playerPos.xCoord : 0),
                packet.getY() + (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y) ? playerPos.yCoord : 0),
                packet.getZ() + (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Z) ? playerPos.zCoord : 0)
        );
        if (pos.distanceTo(this.routeToFollow.get(this.currentRouteIndex).toVec3()) > 6) {
            this.swapState(State.ROTATION, 0);
        }
    }

    @SubscribeEvent
    protected void onRender(RenderWorldLastEvent event) {
        if (!this.isQueued) {
            return;
        }

        this.routeToFollow.drawRoute();
    }

    public void swapState(final State toState, final int delay) {
        this.state = toState;
        this.timer.schedule(delay);
    }

    enum State {
        STARTING, DETECT_ROUTE, ROTATION, ROTATION_VERIFY, AOTV, AOTV_VERIFY, WALK, WALK_VERIFY, END_VERIFY
    }

    public enum NavError {
        NONE, TIME_FAIL, PATHFIND_FAILED
    }
}
