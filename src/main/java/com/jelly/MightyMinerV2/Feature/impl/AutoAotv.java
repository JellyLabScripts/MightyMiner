package com.jelly.MightyMinerV2.Feature.impl;

import com.jelly.MightyMinerV2.Config.MightyMinerConfig;
import com.jelly.MightyMinerV2.Event.PacketEvent;
import com.jelly.MightyMinerV2.Feature.FeatureManager;
import com.jelly.MightyMinerV2.Feature.IFeature;
import com.jelly.MightyMinerV2.Handler.RotationHandler;
import com.jelly.MightyMinerV2.Util.AngleUtil;
import com.jelly.MightyMinerV2.Util.KeyBindUtil;
import com.jelly.MightyMinerV2.Util.PlayerUtil;
import com.jelly.MightyMinerV2.Util.helper.Clock;
import com.jelly.MightyMinerV2.Util.helper.RotationConfiguration;
import com.jelly.MightyMinerV2.Util.helper.Target;
import com.jelly.MightyMinerV2.Util.helper.route.Route;
import com.jelly.MightyMinerV2.Util.helper.route.RouteWaypoint;
import com.jelly.MightyMinerV2.Util.helper.route.TransportMethod;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Getter
public class AutoAotv implements IFeature {
    private static AutoAotv instance;

    public static AutoAotv getInstance() {
        if (instance == null) {
            instance = new AutoAotv();
            FeatureManager.getInstance().addFeature(instance);
        }
        return instance;
    }

    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public String getName() {
        return "AutoAotv";
    }

    @Override
    public boolean isRunning() {
        return this.enabled;
    }

    @Override
    public boolean shouldPauseMacroExecution() {
        return true;
    }

    @Override
    public boolean shouldStartAtLaunch() {
        return false;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        this.enabled = false;
    }

    @Override
    public void resetStatesAfterStop() {
        this.state = State.STARTING;
    }

    @Override
    public boolean isToggle() {
        return false;
    }

    @Override
    public boolean shouldCheckForFailSafe() {
        return true;
    }

    private final Clock timer = new Clock();
    private boolean enabled = false;
    private Route routeToFollow;
    private int currentRouteIndex = -1;
    private int targetRouteIndex = -1;
    private State state = State.STARTING;

    public void queueRoute(final Route routeToFollow) {
        this.routeToFollow = routeToFollow;
    }

    public void goTo(final int index) {
        if (this.routeToFollow == null || this.routeToFollow.isEmpty()) {
            error("No Route Was Selected or its empty.");
            return;
        }
        this.targetRouteIndex = index;
        this.currentRouteIndex = this.getCurrentIndex() - 1;
        this.normalizeIndexes();
        this.enabled = true;
    }

    public void enable(final Route routeToFollow) {
        this.routeToFollow = routeToFollow;
        this.enabled = true;
        this.targetRouteIndex = -1;
        this.normalizeIndexes();
        this.currentRouteIndex = -1;

        success("Enabling AutoAotv.");
    }

    public void pause() {
        this.enabled = false;
        this.timer.reset();
        RotationHandler.getInstance().reset();
        this.resetStatesAfterStop();

        success("Pausing AutoAotv");
    }

    public void disable() {
        this.enabled = false;
        this.routeToFollow = null;
        this.timer.reset();
        this.targetRouteIndex = -1;
        this.currentRouteIndex = -1;
        RotationHandler.getInstance().reset();
        this.resetStatesAfterStop();

        success("Disabling Aotv");
    }

    private void normalizeIndexes() {
        this.targetRouteIndex = this.normalizeIndex(this.targetRouteIndex);
        this.currentRouteIndex = this.normalizeIndex(this.currentRouteIndex);
        if (this.targetRouteIndex < this.currentRouteIndex) {
            this.targetRouteIndex += this.routeToFollow.size();
        }
    }

    private int normalizeIndex(final int index) {
        return (index + this.routeToFollow.size()) % this.routeToFollow.size();
    }

    public int getCurrentIndex() {
        int index = this.routeToFollow.indexOf(new RouteWaypoint(PlayerUtil.getBlockStandingOn(), TransportMethod.ETHERWARP));
        if (index != -1) return index;
        return this.routeToFollow.indexOf(this.routeToFollow.getClosest(PlayerUtil.getBlockStandingOn()).get());
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!this.enabled || mc.thePlayer == null || mc.theWorld == null) return;

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
                this.swapState(State.ROTATION, 0);
                break;
            }
            case ROTATION: {
                // Todo: add check to detect if it can look at next block or not
                // Todo: improve the time thing
                RouteWaypoint nextPoint = this.routeToFollow.get(this.currentRouteIndex);
                int time;
                if (nextPoint.getTransportMethod().ordinal() == 0) {
                    time = MightyMinerConfig.delayAutoAotvLookDelay;
                } else {
                    time = MightyMinerConfig.delayAutoAotvEtherwarpLookDelay;
                }
                RotationHandler.getInstance().easeTo(
                        new RotationConfiguration(new Target(nextPoint.toVec3()), time, () -> {
                        }).followTarget(true)
                );
                this.swapState(State.ROTATION_VERIFY, 2000);
                break;
            }
            case ROTATION_VERIFY: {
                if (this.timer.isScheduled() && this.timer.passed()) {
                    error("Could not look in time. Disabling.");
                    this.disable();
                    return;
                }

                RouteWaypoint target = this.routeToFollow.get(this.currentRouteIndex);

                // Todo: add a better method to verify if rotation was completed or not
                if (!AngleUtil.isLookingAt(target.toVec3(), 2f)) return;
                int sneakTime = 0;
                if (target.getTransportMethod().ordinal() == 1) {
                    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, true);
                    sneakTime = 250;
                }
                this.swapState(State.AOTV, sneakTime);
                break;
            }
            case AOTV: {
                if (this.timer.isScheduled() && !this.timer.passed()) return;
                // Todo: test Etherwarp
                KeyBindUtil.rightClick();
                KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, false);
                RotationHandler.getInstance().reset();
                this.swapState(State.AOTV_VERIFY, 2000);
                break;
            }
            case AOTV_VERIFY:
                break;
            case END_VERIFY:
                if (this.routeToFollow.size() == this.targetRouteIndex - 1) {
                    this.disable();
                } else {
                    this.pause();
                }
                break;
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onPacketReceive(PacketEvent.Received event) {
        if (!this.enabled || this.state != State.AOTV_VERIFY) return;
        if (this.timer.isScheduled() && this.timer.passed()) {
            error("Did not receive teleport packet in time. Disabling");
            this.disable();
            return;
        }
        if (!(event.packet instanceof S08PacketPlayerPosLook)) return;

        // Todo: Add checks to verify teleport
        this.swapState(State.STARTING, 0);
        S08PacketPlayerPosLook pack = (S08PacketPlayerPosLook) event.packet;
        Vec3 pos = new Vec3(pack.getX(), pack.getY(), pack.getZ());
        if (pos.distanceTo(this.routeToFollow.get(this.currentRouteIndex).toVec3()) > 6) {
            this.swapState(State.ROTATION, 0);
        }
    }

    public void swapState(final State toState, final int delay) {
        this.state = toState;
        this.timer.schedule(delay);
    }

    enum State {
        STARTING,
        DETECT_ROUTE,
        ROTATION,
        ROTATION_VERIFY,
        AOTV,
        AOTV_VERIFY,
        END_VERIFY
    }
}
