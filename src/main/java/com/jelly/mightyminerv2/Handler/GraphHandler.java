package com.jelly.mightyminerv2.Handler;

import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.annotations.Expose;
import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.Util.LogUtil;
import com.jelly.mightyminerv2.Util.PlayerUtil;
import com.jelly.mightyminerv2.Util.RenderUtil;
import com.jelly.mightyminerv2.Util.helper.graph.Graph;
import com.jelly.mightyminerv2.Util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.Util.helper.route.TransportMethod;
import java.awt.Color;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;

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
    LogUtil.send("Editing: " + this.editing);
  }

  public void start() {
    this.editing = true;
    Multithreading.schedule(this::save, 0, TimeUnit.MILLISECONDS);
    LogUtil.send("Enabled. Editing: " + this.editing);
  }

  public void stop() {
    this.editing = false;
    LogUtil.send("Disabled. Editing: " + this.editing);
  }

  public List<RouteWaypoint> findPath(RouteWaypoint first, RouteWaypoint second){
    return this.graph.findPath(first, second);
  }

  public synchronized void save() {
    LogUtil.send("Started Save");
    while (this.editing) {
      if (!this.dirty) {
        continue;
      }
      try (BufferedWriter writer = Files.newBufferedWriter(MightyMiner.commRoutePath, StandardCharsets.UTF_8)) {
        writer.write(MightyMiner.gson.toJson(instance));
        this.dirty = false;
        LogUtil.send("saved Data");
      } catch (Exception e) {
        LogUtil.send("No saved data");
        e.printStackTrace();
      }
    }
  }

  @SubscribeEvent
  public void onInput(KeyInputEvent event) {
    if (!this.editing) {
      return;
    }

    RouteWaypoint currentWaypoint = new RouteWaypoint(PlayerUtil.getBlockStandingOn(), TransportMethod.WALK);

    if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD4)) {
      lastPos = currentWaypoint;
      LogUtil.send("Changed Parent");
    }

    if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD7) || Keyboard.isKeyDown(Keyboard.KEY_NUMPAD8)) {
      if (lastPos != null) {
        boolean bidi = Keyboard.isKeyDown(Keyboard.KEY_NUMPAD8); // Check if KEY_8 is pressed for bidirectional edges
        this.graph.add(lastPos, currentWaypoint, bidi);
        LogUtil.send("Added " + (bidi ? "Bidirectional" : "Unidirectional"));
      } else {
        this.graph.add(currentWaypoint);
        LogUtil.send("Added Single Waypoint");
      }
      lastPos = currentWaypoint;
      this.dirty = true;
    }

    if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD5)) {
      if (lastPos == null) {
        return;
      }
      this.graph.update(lastPos, currentWaypoint);
      lastPos = currentWaypoint;
      this.dirty = true;
      LogUtil.send("Updated");
    }

    if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD6)) {
      if (lastPos == null) {
        return;
      }
      this.graph.remove(lastPos);
      lastPos = null;
      this.dirty = true;
      LogUtil.send("Removed");
    }
  }

  @SubscribeEvent
  public void onRender(RenderWorldLastEvent event) {
    if (!this.editing) {
      return;
    }
    for (Entry<RouteWaypoint, List<RouteWaypoint>> entry : this.graph.map.entrySet()) {
      RenderUtil.drawBlockBox(entry.getKey().toBlockPos(), new Color(101, 10, 142, 186));
      for (RouteWaypoint edge : entry.getValue()) {
        RenderUtil.drawTracer(entry.getKey().toVec3().addVector(0.5, 0.5, 0.5), edge.toVec3().addVector(0.5, 0.5, 0.5), new Color(194, 12, 164, 179));
      }
    }
    if (lastPos != null) {
      RenderUtil.drawBlockBox(new BlockPos(lastPos.toVec3()), new Color(255, 0, 0, 150));
    }
  }
}
