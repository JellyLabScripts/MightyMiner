package com.jelly.MightyMinerV2.Feature.impl;

import cc.polyfrost.oneconfig.utils.Multithreading;
import com.jelly.MightyMinerV2.Config.MightyMinerConfig;
import com.jelly.MightyMinerV2.Feature.FeatureManager;
import com.jelly.MightyMinerV2.Feature.IFeature;
import com.jelly.MightyMinerV2.Handler.RouteHandler;
import com.jelly.MightyMinerV2.Util.LogUtil;
import com.jelly.MightyMinerV2.Util.PlayerUtil;
import com.jelly.MightyMinerV2.Util.helper.route.Route;
import com.jelly.MightyMinerV2.Util.helper.route.RouteWaypoint;
import com.jelly.MightyMinerV2.Util.helper.route.TransportMethod;
import lombok.Getter;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Getter
public class RouteBuilder implements IFeature {
    private static RouteBuilder instance;
    public static RouteBuilder getInstance() {
        if (instance == null) {
            instance = new RouteBuilder();
            FeatureManager.getInstance().addFeature(instance);
        }
        return instance;
    }

    private boolean enabled = false;

    @Override
    public String getName() {
        return "RouteBuilder";
    }

    @Override
    public boolean isRunning() {
        return this.enabled;
    }

    @Override
    public boolean shouldPauseMacroExecution() {
        return false;
    }

    @Override
    public boolean shouldStartAtLaunch() {
        return false;
    }

    public void toggle() {
        if (!this.enabled) {
            this.start();
        } else {
            this.stop();
        }
    }

    @Override
    public void start() {
        this.enabled = true;
        Multithreading.schedule(RouteHandler.getInstance()::saveData, 0, TimeUnit.MILLISECONDS);
        success("Enabling RouteBuilder.");
    }

    @Override
    public void stop() {
        this.enabled = false;
        success("Disabling RouteBuilder.");
    }

    @Override
    public void resetStatesAfterStop() {
    }

    @Override
    public boolean isToggle() {
        return false;
    }

    @Override
    public boolean shouldCheckForFailSafe() {
        return false;
    }

    @SubscribeEvent
    public void onKeyEvent(InputEvent.KeyInputEvent event) {
        if (!this.isRunning()) return;

        if (MightyMinerConfig.routeBuilderAddKeybind.isActive()) {
            this.addToRoute();
        }

        if (MightyMinerConfig.routeBuilderRemoveKeybind.isActive()) {
            this.removeFromRoute();
        }
    }

    public void addToRoute() {
        RouteHandler.getInstance().addToCurrentRoute(PlayerUtil.getBlockStandingOn());
    }

    public void removeFromRoute() {
        RouteHandler.getInstance().removeFromCurrentRoute(PlayerUtil.getBlockStandingOn());
    }

    public void replaceNode(final int index) {
        RouteHandler.getInstance().replaceInCurrentRoute(index, new RouteWaypoint(PlayerUtil.getBlockStandingOn(), TransportMethod.ETHERWARP));
    }
}
