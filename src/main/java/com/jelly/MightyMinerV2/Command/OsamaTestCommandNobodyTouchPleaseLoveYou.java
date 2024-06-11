package com.jelly.MightyMinerV2.Command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.MightyMinerV2.Feature.impl.AutoCommissionClaim;
import com.jelly.MightyMinerV2.Feature.impl.AutoInventory;
import com.jelly.MightyMinerV2.Feature.impl.MithrilMiner;
import com.jelly.MightyMinerV2.Feature.impl.RouteNavigator;
import com.jelly.MightyMinerV2.Handler.RouteHandler;
import com.jelly.MightyMinerV2.Util.*;
import com.jelly.MightyMinerV2.Util.LogUtil.ELogType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.World;
import com.jelly.MightyMinerV2.Handler.BaritoneHandler;
import com.jelly.MightyMinerV2.Handler.Waypoints.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Command(value = "set")
public class OsamaTestCommandNobodyTouchPleaseLoveYou {
  private static OsamaTestCommandNobodyTouchPleaseLoveYou instance;
  private final Graph graph;
  private final BaritoneWaypointHandler handler;
  private final String graphName;

  public OsamaTestCommandNobodyTouchPleaseLoveYou(BaritoneWaypointHandler handler, String graphName, Graph graph) {
    this.handler = handler;
    this.graphName = graphName;
    this.graph = graph;
  }


  public static OsamaTestCommandNobodyTouchPleaseLoveYou getInstance(BaritoneWaypointHandler handler, String graphName, Graph graph) {
    if (instance == null) {
      instance = new OsamaTestCommandNobodyTouchPleaseLoveYou(handler, graphName, graph);
    }
    return instance;
  }

  private final Minecraft mc = Minecraft.getMinecraft();

  Entity entTodraw = null;
  List<BlockPos> blockToDraw = new ArrayList<>();
  List<Vec3> points = new ArrayList<>();

  @Main
  public void main() {
//    LogUtil.send("CurrentCommission: " + CommissionUtil.getCurrentCommission(), ELogType.SUCCESS);
    AutoInventory.getInstance().retrieveSpeedBoost();
  }

  private boolean canStandOn(final BlockPos pos) {
    return mc.theWorld.isBlockFullCube(pos)
            && mc.theWorld.isAirBlock(pos.add(0, 1, 0))
            && mc.theWorld.isAirBlock(pos.add(0, 2, 0));
  }

  @SubCommand
  public void mine(final String t) {
    if (!MithrilMiner.getInstance().isRunning()) {
      int[] p = new int[]{1, 1, 1, 1};
      if (t.equals("t")) {
        LogUtil.send("Tita", ELogType.SUCCESS);
        p[3] = 10;
      }
      MithrilMiner.getInstance().enable(2134, 200, p);
    } else {
      MithrilMiner.getInstance().stop();
    }
  }

  @SubCommand
  public void claim() {
    if (!AutoCommissionClaim.getInstance().isRunning()) {
      AutoCommissionClaim.getInstance().start();
    } else {
      AutoCommissionClaim.getInstance().stop();
    }
  }

  @SubCommand
  public void clear() {
    blockToDraw.clear();
    entTodraw = null;
  }

  @SubCommand
  public void aotv() {
    if (RouteHandler.getInstance().getSelectedRoute().isEmpty()) {
      LogUtil.send("Selected Route is empty.", LogUtil.ELogType.SUCCESS);
      return;
    }
    RouteNavigator.getInstance().queueRoute(RouteHandler.getInstance().getSelectedRoute());
    RouteNavigator.getInstance().goTo(36);
  }
  @SubCommand
  public void pathfind(final int x, final int y, final int z) {
    BlockPos end = new BlockPos(x, y, z);
    BlockPos start = mc.thePlayer.getPosition();

    BaritoneHandler.walkToBlockPos(end);
  }

  @SubCommand
  public void waypoints(final String graphName, final String startName, final String endName) {
    // Retrieve the graph by name
    Graph graph = handler.getGraph(graphName);

    // Find the start and end waypoints by name
    Waypoint start = null;
    Waypoint end = null;
    for (Waypoint waypoint : graph.getWaypoints()) {
      if (waypoint.getName().equals(startName)) {
        start = waypoint;
      } else if (waypoint.getName().equals(endName)) {
        end = waypoint;
      }
    }

    // Check if the start and end waypoints were found
    if (start == null || end == null) {
      LogUtil.send("Start or end waypoint not found.", LogUtil.ELogType.ERROR);
      return;
    }

    // Use the A* algorithm to calculate the shortest path between the start and end waypoints
    AStar aStar = new AStar(graph);
    List<Waypoint> shortestPath = aStar.findShortestPath(start, end);

    // Print the shortest path to the console
    LogUtil.send("Shortest path:", LogUtil.ELogType.SUCCESS);
    for (Waypoint waypoint : shortestPath) {
      LogUtil.send(waypoint.getName(), LogUtil.ELogType.SUCCESS);
    }
  }

  @SubCommand
  public void pathfindWaypoints(final String graphName, final String startName, final String endName) {
    // Retrieve the graph by name
    Graph graph = handler.getGraph(graphName);

    // Find the start and end waypoints by name
    Waypoint start = null;
    Waypoint end = null;
    for (Waypoint waypoint : graph.getWaypoints()) {
      if (waypoint.getName().equals(startName)) {
        start = waypoint;
      } else if (waypoint.getName().equals(endName)) {
        end = waypoint;
      }
    }

    // Check if the start and end waypoints were found
    if (start == null || end == null) {
      LogUtil.send("Start or end waypoint not found.", LogUtil.ELogType.ERROR);
      return;
    }

    // Use the A* algorithm to calculate the shortest path between the start and end waypoints
    AStar aStar = new AStar(graph);
    List<Waypoint> shortestPath = aStar.findShortestPath(start, end);

    // Convert the shortest path to a list of BlockPos objects
    List<BlockPos> waypoints = new ArrayList<>();
    for (Waypoint waypoint : shortestPath) {
      Vec3 position = waypoint.getPosition();
      waypoints.add(new BlockPos(position.xCoord, position.yCoord, position.zCoord));
    }

    // Print the shortest route in the chat
    StringBuilder routeString = new StringBuilder("Shortest route: ");
    for (BlockPos waypoint : waypoints) {
      routeString.append(waypoint.toString()).append(" -> ");
    }
    routeString.setLength(routeString.length() - 4); // Remove the last " -> "
    LogUtil.send(routeString.toString(), LogUtil.ELogType.SUCCESS);

    // Pathfind through the waypoints
    BaritoneHandler.walkThroughWaypoints(waypoints);
  }





  @SubscribeEvent
  public void onRender(RenderWorldLastEvent event) {
    if (entTodraw != null) {
      RenderUtil.drawBox(((EntityLivingBase) entTodraw).getEntityBoundingBox(), Color.CYAN);
    }

    if (!blockToDraw.isEmpty()) {
      blockToDraw.forEach(it -> RenderUtil.drawBlockBox(it, new Color(0, 255, 255, 50)));
    }

    if (!points.isEmpty()) {
      points.forEach(it -> RenderUtil.drawPoint(it, new Color(255, 0, 0, 100)));
    }
  }
}
