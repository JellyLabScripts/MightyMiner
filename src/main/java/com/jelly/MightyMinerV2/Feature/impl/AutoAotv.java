package com.jelly.MightyMinerV2.Feature.impl;

import com.jelly.MightyMinerV2.Config.MightyMinerConfig;
import com.jelly.MightyMinerV2.Event.PacketEvent;
import com.jelly.MightyMinerV2.Feature.FeatureManager;
import com.jelly.MightyMinerV2.Feature.IFeature;
import com.jelly.MightyMinerV2.Handler.RotationHandler;
import com.jelly.MightyMinerV2.Util.AngleUtil;
import com.jelly.MightyMinerV2.Util.KeyBindUtil;
import com.jelly.MightyMinerV2.Util.helper.Clock;
import com.jelly.MightyMinerV2.Util.helper.RotationConfiguration;
import com.jelly.MightyMinerV2.Util.helper.Target;
import com.jelly.MightyMinerV2.Util.helper.route.Route;
import com.jelly.MightyMinerV2.Util.helper.route.RouteWaypoint;
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
    private State state = State.STARTING;

    public void enable(Route routeToFollow) {
        this.routeToFollow = routeToFollow;
        this.enabled = true;

        success("Enabling AutoAotv.");
    }

    public void disable() {
        this.enabled = false;
        this.routeToFollow = null;
        this.timer.reset();
        this.currentRouteIndex = -1;
        RotationHandler.getInstance().reset();
        this.resetStatesAfterStop();

        success("Disabling Aotv");
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
                if (this.routeToFollow.isEnd(this.currentRouteIndex++)) {
                    this.swapState(State.END_VERIFY, 0);
                    return;
                }
                this.swapState(State.ROTATION, 0);
                break;
            }
            case ROTATION: {
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

                KeyBindUtil.rightClick();
                KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, false);
                RotationHandler.getInstance().reset();
                this.swapState(State.AOTV_VERIFY, 0);
                break;
            }
            case AOTV_VERIFY:
                break;
            case END_VERIFY:
                this.disable();
                break;
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Received event) {
        if (!this.enabled || this.state != State.AOTV_VERIFY) return;
        if (!(event.packet instanceof S08PacketPlayerPosLook)) return;
        // Add checks to verify teleport
        this.swapState(State.STARTING, 0);
        S08PacketPlayerPosLook pack = (S08PacketPlayerPosLook) event.packet;
        Vec3 pos = new Vec3(pack.getX(), pack.getY(), pack.getZ());
        if(pos.distanceTo(this.routeToFollow.get(this.currentRouteIndex).toVec3()) > 6){
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
