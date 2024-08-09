package com.jelly.mightyminerv2.Feature.impl;

import cc.polyfrost.oneconfig.utils.Multithreading;
import com.jelly.mightyminerv2.Config.MightyMinerConfig;
import com.jelly.mightyminerv2.Feature.FeatureManager;
import com.jelly.mightyminerv2.Feature.IFeature;
import com.jelly.mightyminerv2.Handler.RouteHandler;
import com.jelly.mightyminerv2.Util.LogUtil;
import com.jelly.mightyminerv2.Util.PlayerUtil;
import com.jelly.mightyminerv2.Util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.Util.helper.route.TransportMethod;
import lombok.Getter;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

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
        send("Enabling RouteBuilder.");
    }

    @Override
    public void stop() {
        this.enabled = false;
        send("Disabling RouteBuilder.");
    }

    @Override
    public void resetStatesAfterStop() {
    }

    @Override
    public boolean shouldCheckForFailsafe() {
        return false;
    }

    @SubscribeEvent
    public void onKeyEvent(InputEvent.KeyInputEvent event) {
        if (!this.isRunning()) return;

        if (MightyMinerConfig.routeBuilderAotvAddKeybind.isActive()) {
            this.addToRoute(TransportMethod.AOTV);
            LogUtil.send("Added Aotv");
        }

        if (MightyMinerConfig.routeBuilderEtherwarpAddKeybind.isActive()) {
            this.addToRoute(TransportMethod.ETHERWARP);
            LogUtil.send("Added Etherwarp");
        }

        if (MightyMinerConfig.routeBuilderRemoveKeybind.isActive()) {
            this.removeFromRoute();
        }
    }

    public void addToRoute(final TransportMethod method) {
        RouteHandler.getInstance().addToCurrentRoute(PlayerUtil.getBlockStandingOn(), method);
    }

    public void removeFromRoute() {
        RouteHandler.getInstance().removeFromCurrentRoute(PlayerUtil.getBlockStandingOn());
    }

    public void replaceNode(final int index) {
        RouteHandler.getInstance().replaceInCurrentRoute(index, new RouteWaypoint(PlayerUtil.getBlockStandingOn(), TransportMethod.ETHERWARP));
    }
}
