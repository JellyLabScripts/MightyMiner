package com.jelly.mightyminerv2.handler;

import com.google.gson.annotations.Expose;
import com.jelly.mightyminerv2.feature.impl.RouteBuilder;
import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.route.Route;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.util.helper.route.TransportMethod;
import lombok.Getter;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

@Getter
public class RouteHandler {
    public static RouteHandler instance;

    public static RouteHandler getInstance() {
        if (instance == null) instance = new RouteHandler();
        return instance;
    }

    @Expose
    private final HashMap<String, Route> routes = new HashMap<String, Route>() {{
        put("Default", new Route());
    }};
    private Route selectedRoute = this.routes.get("Default");
    private volatile boolean dirty = false;

    public void selectRoute(String routeName) {
        if (!this.routes.containsKey(routeName)) {
            this.createRoute(routeName);
        }
        this.selectedRoute = routes.get(routeName);
        this.markDirty();
    }

    public void createRoute(String routeName) {
        if (this.routes.containsKey(routeName)) return;
        this.routes.put(routeName, new Route());
        this.markDirty();
    }

    public void addToCurrentRoute(final BlockPos block, final TransportMethod method) {
        if (this.selectedRoute == this.routes.get("Default")) {
            Logger.sendError("Cannot Edit Default Route.");
            return;
        }
        final RouteWaypoint waypoint = new RouteWaypoint(block, method);
        if (this.selectedRoute.indexOf(waypoint) != -1) return;
        this.selectedRoute.insert(waypoint);
        this.markDirty();
    }

    public void removeFromCurrentRoute(final BlockPos block) {
        this.selectedRoute.remove(new RouteWaypoint(block, TransportMethod.ETHERWARP));
        this.markDirty();
    }

    public void replaceInCurrentRoute(final int index, final RouteWaypoint waypoint) {
        this.selectedRoute.replace(index, waypoint);
        this.markDirty();
    }

    public void clearRoute(final String routeName) {
        if (this.selectedRoute == this.routes.remove(routeName)) {
            this.selectedRoute = this.getRoutes().get("Default");
        }
        this.markDirty();
    }

    public void markDirty() {
        this.dirty = true;
    }

    public synchronized void saveData() {
        while (RouteBuilder.getInstance().isRunning()) {
            try {
                if (!this.dirty) continue;
                String data = MightyMiner.gson.toJson(instance);
                Files.write(MightyMiner.routesDirectory, data.getBytes(StandardCharsets.UTF_8));
                this.dirty = false;
            } catch (IOException e) {
                System.out.println("Save Loop Crashed.");
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        // move it inside gemstone macro
        this.selectedRoute.drawRoute();
    }
}
