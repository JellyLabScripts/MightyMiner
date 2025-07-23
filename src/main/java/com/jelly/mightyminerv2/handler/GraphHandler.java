package com.jelly.mightyminerv2.handler;

import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.annotations.Expose;
import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.RenderUtil;
import com.jelly.mightyminerv2.util.helper.graph.Graph;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.util.helper.route.TransportMethod;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.awt.*;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GraphHandler {

    public static final GraphHandler instance = new GraphHandler();
    @Expose
    public final Map<String, Graph<RouteWaypoint>> graphs = new HashMap<>();
    private String activeGraphKey = "default";
    private boolean editing = false;
    private boolean dirty = false;
    private RouteWaypoint lastPos = null;

    public Graph<RouteWaypoint> getActiveGraph() {
        return graphs.computeIfAbsent(activeGraphKey, k -> new Graph<>());
    }

    public void switchGraph(AbstractMacro macro) {
        activeGraphKey = macro.getName();
        getActiveGraph();
        Logger.sendMessage("Switched to graph: " + macro.getName());
    }

    public void toggleEdit(String graphName) {
        if (!graphs.containsKey(graphName)) return;
        activeGraphKey = graphName;
        if (editing) stop();
        else start();
        Logger.sendMessage(editing ? "Editing " + graphName : "Stopped Editing " + graphName);
    }

    public void toggleEdit() {
        if (editing) stop();
        else start();
        Logger.sendMessage(editing ? "Editing " + activeGraphKey : "Stopped Editing " + activeGraphKey);
    }

    public void start() {
        editing = true;
        Multithreading.schedule(this::save, 0, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        editing = false;
        dirty = false;
    }

    public double distance(String graphName, BlockPos start, RouteWaypoint end) {
        List<RouteWaypoint> route = findPathFrom(graphName, start, end);
        if (route.size() < 2) return -1;

        double distance = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            distance += route.get(i).toBlockPos().distanceSq(route.get(i + 1).toBlockPos());
        }

        return distance;
    }

    public List<RouteWaypoint> findPath(BlockPos start, RouteWaypoint end) {
        Graph<RouteWaypoint> graph = getActiveGraph();
        RouteWaypoint startWp = graph.map.keySet().stream()
                .min(Comparator.comparing(wp -> start.distanceSq(wp.toBlockPos())))
                .orElse(new RouteWaypoint(start, TransportMethod.WALK));
        if (!graph.map.containsKey(end)) return Collections.emptyList();
        return findPath(startWp, end);
    }

    public List<RouteWaypoint> findPath(RouteWaypoint first, RouteWaypoint second) {
        Graph<RouteWaypoint> graph = getActiveGraph();
        List<RouteWaypoint> route = graph.findPath(first, second);
        if (route.size() < 2 || PlayerUtil.getBlockStandingOn().distanceSq(route.get(0).toBlockPos()) >=
                route.get(0).toBlockPos().distanceSq(route.get(1).toBlockPos())) {
            return route;
        }
        route.remove(0);
        return route;
    }

    public List<RouteWaypoint> findPathFrom(String graphName, BlockPos start, RouteWaypoint end) {
        Graph<RouteWaypoint> graph = graphs.get(graphName);
        if (graph == null) return Collections.emptyList();
        RouteWaypoint startWp = graph.map.keySet().stream()
                .min(Comparator.comparing(wp -> start.distanceSq(wp.toBlockPos())))
                .orElse(new RouteWaypoint(start, TransportMethod.WALK));
        if (!graph.map.containsKey(end)) return Collections.emptyList();
        return findPathFrom(graphName, startWp, end);
    }

    public List<RouteWaypoint> findPathFrom(String graphName, RouteWaypoint first, RouteWaypoint second) {
        Graph<RouteWaypoint> graph = graphs.get(graphName);
        if (graph == null) return Collections.emptyList();
        List<RouteWaypoint> route = graph.findPath(first, second);
        if (route.size() < 2 || PlayerUtil.getBlockStandingOn().distanceSq(route.get(0).toBlockPos()) >=
                route.get(0).toBlockPos().distanceSq(route.get(1).toBlockPos())) {
            return route;
        }
        route.remove(0);
        return route;
    }

    public synchronized void save() {
        while (editing) {
            if (!dirty) continue;
            Graph<RouteWaypoint> graph = graphs.get(activeGraphKey);

            try (BufferedWriter writer = Files.newBufferedWriter(MightyMiner.routesDirectory.resolve(activeGraphKey + ".json"), StandardCharsets.UTF_8)) {
                writer.write(MightyMiner.gson.toJson(graph));
                Logger.sendLog("Saved graph: " + activeGraphKey);
            } catch (Exception e) {
                Logger.sendLog("Failed to save graph: " + activeGraphKey);
            }

            dirty = false;
        }
    }

    @SubscribeEvent
    public void onInput(InputEvent event) {
        if (!editing) return;
        RouteWaypoint currentWaypoint = new RouteWaypoint(PlayerUtil.getBlockStandingOn(), TransportMethod.WALK);
        Graph<RouteWaypoint> graph = getActiveGraph();
        if (MightyMinerConfig.routeBuilderSelect.isActive()) {
            lastPos = currentWaypoint;
            Logger.sendMessage("Changed Parent");
        }
        if (MightyMinerConfig.routeBuilderUnidi.isActive() || MightyMinerConfig.routeBuilderBidi.isActive()) {
            if (lastPos != null) {
                graph.add(lastPos, currentWaypoint, MightyMinerConfig.routeBuilderBidi.isActive());
                Logger.sendMessage("Added " + (MightyMinerConfig.routeBuilderBidi.isActive() ? "Bidirectional" : "Unidirectional"));
            } else {
                graph.add(currentWaypoint);
                Logger.sendMessage("Added Single Waypoint");
            }
            lastPos = currentWaypoint;
            dirty = true;
        }
        if (MightyMinerConfig.routeBuilderMove.isActive() && lastPos != null) {
            graph.update(lastPos, currentWaypoint);
            lastPos = currentWaypoint;
            dirty = true;
            Logger.sendMessage("Updated");
        }
        if (MightyMinerConfig.routeBuilderDelete.isActive() && lastPos != null) {
            graph.remove(lastPos);
            lastPos = null;
            dirty = true;
            Logger.sendMessage("Removed");
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (!editing) return;
        Graph<RouteWaypoint> graph = getActiveGraph();
        for (Map.Entry<RouteWaypoint, List<RouteWaypoint>> entry : graph.map.entrySet()) {
            RenderUtil.drawBlock(entry.getKey().toBlockPos(), new Color(101, 10, 142, 186));
            for (RouteWaypoint edge : entry.getValue()) {
                RenderUtil.drawLine(entry.getKey().toVec3().addVector(0.5, 0.5, 0.5), edge.toVec3().addVector(0.5, 0.5, 0.5), new Color(194, 12, 164, 179));
            }
        }
        if (lastPos != null) {
            RenderUtil.drawBlock(new BlockPos(lastPos.toVec3()), new Color(255, 0, 0, 150));
        }
    }

}
