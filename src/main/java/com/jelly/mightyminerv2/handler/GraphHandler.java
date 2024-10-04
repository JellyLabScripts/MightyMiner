package com.jelly.mightyminerv2.handler;

import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.annotations.Expose;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.MightyMiner;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class GraphHandler {

  public static GraphHandler instance;

  public static GraphHandler getInstance() {
    if (instance == null) {
      instance = new GraphHandler();
    }
    return instance;
  }

  @Expose
  private final Graph<RouteWaypoint> graph = new Graph<RouteWaypoint>();
  private boolean editing = false;
  private volatile boolean dirty = false;
  private RouteWaypoint lastPos = null;

  public void toggleEdit() {
    if (editing) {
      stop();
    } else {
      start();
    }
    Logger.sendMessage("Editing: " + this.editing);
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
    if (!this.graph.map.containsKey(startWp)) {
      startWp = this.graph.map.keySet().stream().min(Comparator.comparing(it -> start.distanceSq(it.toBlockPos()))).orElse(null);
    }

    if (startWp == null) {
      Logger.sendLog("StartWP is null");
      return new ArrayList<>();
    }

    if (!this.graph.map.containsKey(end)) {
      Logger.sendLog("GraphMap Does Not Contain End");
      return new ArrayList<>();
    }

    return findPath(startWp, end);
  }

  public List<RouteWaypoint> findPath(RouteWaypoint first, RouteWaypoint second) {
    List<RouteWaypoint> route = this.graph.findPath(first, second);
    if (route.size() < 2) {
      return route;
    }
    if (PlayerUtil.getBlockStandingOn().distanceSq(route.get(0).toBlockPos()) < route.get(0).toBlockPos().distanceSq(route.get(1).toBlockPos())) {
      route.remove(0);
    }
    return route;
  }

  public synchronized void save() {
    Logger.sendMessage("Started Save");
    while (this.editing) {
      if (!this.dirty) {
        continue;
      }
      try (BufferedWriter writer = Files.newBufferedWriter(MightyMiner.commRoutePath, StandardCharsets.UTF_8)) {
        writer.write(MightyMiner.gson.toJson(instance));
        Logger.sendMessage("saved Data");
      } catch (Exception e) {
        Logger.sendMessage("No saved data");
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

    if (MightyMinerConfig.routeBuilderSelect.isActive()) {
      lastPos = currentWaypoint;
      Logger.sendMessage("Changed Parent");
    }

    if (MightyMinerConfig.routeBuilderUnidi.isActive() || MightyMinerConfig.routeBuilderBidi.isActive()) {
      if (lastPos != null) {
        boolean bidi = MightyMinerConfig.routeBuilderBidi.isActive(); // Check if KEY_8 is pressed for bidirectional edges
        this.graph.add(lastPos, currentWaypoint, bidi);
        Logger.sendMessage("Added " + (bidi ? "Bidirectional" : "Unidirectional"));
      } else {
        this.graph.add(currentWaypoint);
        Logger.sendMessage("Added Single Waypoint");
      }
      lastPos = currentWaypoint;
      this.dirty = true;
    }

    if (MightyMinerConfig.routeBuilderMove.isActive()) {
      if (lastPos == null) {
        return;
      }
      this.graph.update(lastPos, currentWaypoint);
      lastPos = currentWaypoint;
      this.dirty = true;
      Logger.sendMessage("Updated");
    }

    if (MightyMinerConfig.routeBuilderDelete.isActive()) {
      if (lastPos == null) {
        return;
      }
      this.graph.remove(lastPos);
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
    for (Entry<RouteWaypoint, List<RouteWaypoint>> entry : this.graph.map.entrySet()) {
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
