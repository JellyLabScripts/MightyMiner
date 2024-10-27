package com.jelly.mightyminerv2.handler;

import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.annotations.Expose;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.RenderUtil;
import com.jelly.mightyminerv2.util.helper.graph.Graph;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.util.helper.route.TransportMethod;
import java.awt.Color;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class GraphHandler {

  // no need for a redundant getInstance() call - useless overhead
  public static GraphHandler instance = new GraphHandler();

  @Expose
  public Map<String, Graph<RouteWaypoint>> graphs = new HashMap<>();  // Multiple graphs
  private String activeGraphKey = "default";  // Default graph

  private boolean editing = false;
  private volatile boolean dirty = false;
  private RouteWaypoint lastPos = null;

  // Get the active graph
  public Graph<RouteWaypoint> getActiveGraph() {
    return graphs.getOrDefault(activeGraphKey, new Graph<>());  // Return active graph or create a new one if not present
  }

  // Switch to a different graph by key
  public void switchGraph(AbstractMacro macro) {
    activeGraphKey = macro.getName();
    graphs.putIfAbsent(macro.getName(), new Graph<>());  // Create new graph if it doesn't exist
    Logger.sendMessage("Switched to graph: " + macro.getName());
  }

  public Graph<RouteWaypoint> getGraph(AbstractMacro macro) {
    return graphs.get(macro.getName());
  }

  public void toggleEdit(String graphName) {
    if (!this.graphs.containsKey(graphName)) {
      return;
    }
    this.activeGraphKey = graphName;
    if (editing) {
      stop();
    } else {
      start();
    }
    Logger.sendMessage((this.editing ? "" : "Stopped ") + "Editing " + this);
  }

  public void toggleEdit() {
    if (editing) {
      stop();
    } else {
      start();
    }
    Logger.sendMessage((this.editing ? "" : "Stopped ") + "Editing " + this.activeGraphKey);
  }

  public void start() {
    this.editing = true;
    Multithreading.schedule(this::save, 0, TimeUnit.MILLISECONDS);
    Logger.sendMessage("Enabled. Editing: " + this.editing);
  }

  public void stop() {
    this.editing = false;
    Logger.sendMessage("Disabled. Editing: " + this.editing);
  }

  public List<RouteWaypoint> findPath(BlockPos start, RouteWaypoint end) {
    RouteWaypoint startWp = new RouteWaypoint(start, TransportMethod.WALK);
    Graph<RouteWaypoint> graph = getActiveGraph();  // Use the active graph

    if (!graph.map.containsKey(startWp)) {
      startWp = graph.map.keySet().stream().min(Comparator.comparing(it -> start.distanceSq(it.toBlockPos()))).orElse(null);
    }

    if (startWp == null) {
      Logger.sendLog("StartWP is null");
      return new ArrayList<>();
    }

    if (!graph.map.containsKey(end)) {
      Logger.sendLog("GraphMap Does Not Contain End");
      return new ArrayList<>();
    }

    return findPath(startWp, end);
  }

  public List<RouteWaypoint> findPath(RouteWaypoint first, RouteWaypoint second) {
    Graph<RouteWaypoint> graph = getActiveGraph();  // Use the active graph
    List<RouteWaypoint> route = graph.findPath(first, second);
    if (route.size() < 2) {
      return route;
    }
    if (PlayerUtil.getBlockStandingOn().distanceSq(route.get(0).toBlockPos()) < route.get(0).toBlockPos().distanceSq(route.get(1).toBlockPos())) {
      route.remove(0);
    }
    return route;
  }

  public List<RouteWaypoint> findPathFrom(String graphName, BlockPos start, RouteWaypoint end) {
    RouteWaypoint startWp = new RouteWaypoint(start, TransportMethod.WALK);
    Graph<RouteWaypoint> graph = this.graphs.get(graphName);  // Use the active graph
    if (graph == null) {
      System.out.println("No graph found for " + graphName);
      return Collections.emptyList();
    }

    if (!graph.map.containsKey(startWp)) {
      startWp = graph.map.keySet().stream().min(Comparator.comparing(it -> start.distanceSq(it.toBlockPos()))).orElse(null);
    }

    if (startWp == null) {
      Logger.sendLog("StartWP is null");
      return new ArrayList<>();
    }

    if (!graph.map.containsKey(end)) {
      Logger.sendLog("GraphMap Does Not Contain End");
      return new ArrayList<>();
    }

    return findPathFrom(graphName, startWp, end);
  }

  public List<RouteWaypoint> findPathFrom(String graphName, RouteWaypoint first, RouteWaypoint second) {
    Graph<RouteWaypoint> graph = this.graphs.get(graphName);  // Use the active graph
    if (graph == null) {
      System.out.println("No graph found for " + graphName);
      return Collections.emptyList();
    }
    List<RouteWaypoint> route = graph.findPath(first, second);
    if (route.size() < 2) {
      return route;
    }
    if (PlayerUtil.getBlockStandingOn().distanceSq(route.get(0).toBlockPos()) < route.get(0).toBlockPos().distanceSq(route.get(1).toBlockPos())) {
      route.remove(0);
    }
    return route;
  }

  public synchronized void save() {
    while (this.editing) {
      if (!this.dirty) {
        continue;
      }

      String graphKey = this.activeGraphKey;
      Graph<RouteWaypoint> graph = this.graphs.get(graphKey);
      // Use file name from graph key
      try (BufferedWriter writer = Files.newBufferedWriter(MightyMiner.routesDirectory.resolve(graphKey + ".json"), StandardCharsets.UTF_8)) {
        writer.write(MightyMiner.gson.toJson(graph));
        Logger.sendLog("Saved graph: " + graphKey);
      } catch (Exception e) {
        Logger.sendLog("Failed to save graph: " + graphKey);
        e.printStackTrace();
      }

      this.dirty = false;
    }
  }

  @SubscribeEvent
  public void onInput(InputEvent event) {
    if (!this.editing) {
      return;
    }

    RouteWaypoint currentWaypoint = new RouteWaypoint(PlayerUtil.getBlockStandingOn(), TransportMethod.WALK);
    Graph<RouteWaypoint> graph = getActiveGraph();  // Use the active graph

    if (MightyMinerConfig.routeBuilderSelect.isActive()) {
      lastPos = currentWaypoint;
      Logger.sendMessage("Changed Parent");
    }

    if (MightyMinerConfig.routeBuilderUnidi.isActive() || MightyMinerConfig.routeBuilderBidi.isActive()) {
      if (lastPos != null) {
        boolean bidi = MightyMinerConfig.routeBuilderBidi.isActive();  // Check if bidirectional edges
        graph.add(lastPos, currentWaypoint, bidi);
        Logger.sendMessage("Added " + (bidi ? "Bidirectional" : "Unidirectional"));
      } else {
        graph.add(currentWaypoint);
        Logger.sendMessage("Added Single Waypoint");
      }
      lastPos = currentWaypoint;
      this.dirty = true;
    }

    if (MightyMinerConfig.routeBuilderMove.isActive()) {
      if (lastPos == null) {
        return;
      }
      graph.update(lastPos, currentWaypoint);
      lastPos = currentWaypoint;
      this.dirty = true;
      Logger.sendMessage("Updated");
    }

    if (MightyMinerConfig.routeBuilderDelete.isActive()) {
      if (lastPos == null) {
        return;
      }
      graph.remove(lastPos);
      lastPos = null;
      this.dirty = true;
      Logger.sendMessage("Removed");
    }
  }

  @SubscribeEvent
  public void onRender(RenderWorldLastEvent event) {
    if (!this.editing) {
      return;
    }
    Graph<RouteWaypoint> graph = getActiveGraph();  // Use the active graph
    for (Entry<RouteWaypoint, List<RouteWaypoint>> entry : graph.map.entrySet()) {
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
