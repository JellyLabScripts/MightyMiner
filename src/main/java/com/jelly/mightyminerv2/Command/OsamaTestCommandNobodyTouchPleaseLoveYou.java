package com.jelly.mightyminerv2.Command;

import static java.lang.Math.ceil;

import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.google.common.collect.ImmutableList;
import com.jelly.mightyminerv2.Feature.impl.AutoCommissionClaim;
import com.jelly.mightyminerv2.Feature.impl.AutoInventory;
import com.jelly.mightyminerv2.Feature.impl.AutoMobKiller;
import com.jelly.mightyminerv2.Feature.impl.AutoWarp;
import com.jelly.mightyminerv2.Feature.impl.MithrilMiner;
import com.jelly.mightyminerv2.Feature.impl.Pathfinder;
import com.jelly.mightyminerv2.Feature.impl.RouteNavigator;
import com.jelly.mightyminerv2.Handler.GraphHandler;
import com.jelly.mightyminerv2.Handler.RouteHandler;
import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.Util.CommissionUtil;
import com.jelly.mightyminerv2.Util.LogUtil;
import com.jelly.mightyminerv2.Util.PlayerUtil;
import com.jelly.mightyminerv2.Util.RenderUtil;
import com.jelly.mightyminerv2.Util.helper.location.SubLocation;
import com.jelly.mightyminerv2.Util.helper.route.Route;
import com.jelly.mightyminerv2.Util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.Util.helper.route.TransportMethod;
import com.jelly.mightyminerv2.pathfinder.calculate.PathNode;
import com.jelly.mightyminerv2.pathfinder.calculate.path.AStarPathFinder;
import com.jelly.mightyminerv2.pathfinder.goal.Goal;
import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext;
import com.jelly.mightyminerv2.pathfinder.movement.Movement;
import com.jelly.mightyminerv2.pathfinder.movement.MovementResult;
import com.jelly.mightyminerv2.pathfinder.movement.Moves;
import com.jelly.mightyminerv2.pathfinder.movement.movements.MovementAscend;
import com.jelly.mightyminerv2.pathfinder.movement.movements.MovementDescend;
import com.jelly.mightyminerv2.pathfinder.movement.movements.MovementDiagonal;
import com.jelly.mightyminerv2.pathfinder.movement.movements.MovementTraverse;
import com.jelly.mightyminerv2.pathfinder.util.BlockUtil;
import com.jelly.mightyminerv2.pathfinder.calculate.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import kotlin.Pair;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@Command(value = "set")
public class OsamaTestCommandNobodyTouchPleaseLoveYou {

  @Getter
  private static OsamaTestCommandNobodyTouchPleaseLoveYou instance = new OsamaTestCommandNobodyTouchPleaseLoveYou();
  private final Minecraft mc = Minecraft.getMinecraft();
  RouteWaypoint first;
  RouteWaypoint second;

  Entity entTodraw = null;
  BlockPos block = null;
  List<BlockPos> blockToDraw = new CopyOnWriteArrayList<>();
  List<Vec3> points = new ArrayList<>();
  int tick = 0;
  Path path;
  AStarPathFinder pathfinder;
  PathNode curr;
  List<Pair<EntityPlayer, Pair<Double, Double>>> mobs = new ArrayList<>();
  boolean allowed = false;

  @Main
  public void main() {
//    btd = com.jelly.mightyminerv2.Util.BlockUtil.getBestMithrilBlocksDebug(new int[]{5, 3, 1, 10});
    for(Pair<String, Vec3> emissary: CommissionUtil.emissaries){
      LogUtil.send(emissary.getFirst() + " Dist: " + mc.thePlayer.getPositionVector().squareDistanceTo(emissary.getSecond()));
    }
  }

  @SubCommand
  public void calc() {
    BlockPos pos = new BlockPos(mc.thePlayer.posX, ceil(mc.thePlayer.posY) - 1, mc.thePlayer.posZ);
    MovementResult res = new MovementResult();
    double walkSpeed = mc.thePlayer.getAIMoveSpeed();
    CalculationContext ctx = new CalculationContext(MightyMiner.instance, walkSpeed * 1.3, walkSpeed, walkSpeed * 0.3);
    for (Moves move : Moves.getEntries()) {
      res.reset();
      move.calculate(ctx, pos.getX(), pos.getY(), pos.getZ(), res);
      double cost = res.getCost();
      if (cost >= 1e6) {
        continue;
      }
      LogUtil.send("Name: " + move.name() + ", Movement to: " + res.getDest() + ", Cost: " + cost);
      this.blockToDraw.add(res.getDest());
    }
  }

  private boolean canStandOn(final BlockPos pos) {
    return mc.theWorld.isBlockFullCube(pos)
        && mc.theWorld.isAirBlock(pos.add(0, 1, 0))
        && mc.theWorld.isAirBlock(pos.add(0, 2, 0));
  }

  @SubCommand
  public void f() {
    BlockPos playerPos = PlayerUtil.getBlockStandingOn();
    first = new RouteWaypoint(playerPos, TransportMethod.WALK);
  }

  @SubCommand
  public void s() {
    BlockPos playerPos = PlayerUtil.getBlockStandingOn();
    second = new RouteWaypoint(playerPos, TransportMethod.WALK);
  }

  @SubCommand
  public void graph() {
    GraphHandler.getInstance().toggleEdit();
  }

  @SubCommand
  public void findg() {
//    GraphHandler.getInstance().stop();
    List<RouteWaypoint> path = GraphHandler.getInstance().findPath(first, second);
    Route route = new Route();
    path.forEach(k -> route.insert(k));
    blockToDraw.clear();
//    path.forEach(i -> blockToDraw.add(new BlockPos(i.toVec3())));
    RouteNavigator.getInstance().enable(route);
  }

  String[] name = {"Ice Walker", "Goblin"};

  @SubCommand
  public void k(int i) {
    AutoMobKiller.getInstance().enable(name[i]);
//    this.c = !this.c;
  }

  @SubCommand
  public void stop() {
    RouteNavigator.getInstance().disable();
    AutoMobKiller.getInstance().stop();
    Pathfinder.getInstance().stop();
  }

  @SubCommand
  public void b() {
    this.block = PlayerUtil.getBlockStandingOn();
  }

  @SubCommand
  public void between() {
    if (this.block != null) {
      this.blockToDraw.clear();
      boolean bs = BlockUtil.INSTANCE.bresenham(new CalculationContext(MightyMiner.instance),
          new Vec3(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ), new Vec3(this.block));
      LogUtil.send("Walkable: " + bs);
    }
  }

  @SubCommand
  public void mine(int t) {
    if (!MithrilMiner.getInstance().isRunning()) {
      int[] p = new int[]{6, 4, 2, 1};
      if (t == 1) {
        LogUtil.send("Tita");
        p[3] = 10;
      }
      MithrilMiner.getInstance().enable(p);
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
  public void move() {
    AutoInventory.getInstance().moveItems(Arrays.asList("Pickonimbus 2000", "Aspect of the Void"));
  }

  @SubCommand
  public void clear() {
    blockToDraw.clear();
    entTodraw = null;
    block = null;
    path = null;
    first = null;
    second = null;
    pathfinder = null;
    curr = null;
    btd.clear();
  }

  @SubCommand
  public void aotv() {
    if (RouteHandler.getInstance().getSelectedRoute().isEmpty()) {
      LogUtil.send("Selected Route is empty.");
      return;
    }
    RouteNavigator.getInstance().queueRoute(RouteHandler.getInstance().getSelectedRoute());
    RouteNavigator.getInstance().goTo(36);
  }

  @SubCommand
  public void c() {
    BlockPos pp = PlayerUtil.getBlockStandingOn();
//    this.curr = this.pathfinder.getClosedSet().get(PathNode.Companion.longHash(pp.getX(), pp.getY(), pp.getZ()));
    LogUtil.send("Curr: " + curr);
  }

  @SubCommand
  public void go(int go) {
//    if (first == null || second == null) {
//      LogUtil.error("First or sec is null");
//      return;
//    }
//    Multithreading.schedule(() -> {
//      double walkSpeed = mc.thePlayer.getAIMoveSpeed();
//      CalculationContext ctx = new CalculationContext(MightyMiner.instance, walkSpeed * 1.3, walkSpeed, walkSpeed * 0.3);
//      BlockPos first = PlayerUtil.getBlockStandingOn();
//      BlockPos second = this.block;
//      AStarPathFinder finder = new AStarPathFinder(
//          first.getX(), first.getY(), first.getZ(),
//          new Goal(second.getX(), second.getY(), second.getZ(), ctx),
//          ctx
//      );
//      Path path = finder.calculatePath();
//      if (path == null) {
//        LogUtil.send("No path found");
//      } else {
//        LogUtil.send("path found");
//        blockToDraw.clear();
//        blockToDraw.addAll(path.getSmoothedPath());
//        if (go == 0) {
//          Pathfinder.getInstance().queue();
//        }
//      }
//    }, 0, TimeUnit.MILLISECONDS);
    Pathfinder.getInstance().queue(PlayerUtil.getBlockStandingOn(), this.block);
//    Pathfinder.getInstance().queue(new BlockPos(first.toVec3()), new BlockPos(second.toVec3()));
//
    Pathfinder.getInstance().start();
  }

  @SubscribeEvent
  public void onTick(ClientTickEvent event) {
    if (!allowed) {
      return;
    }
    mobs = CommissionUtil.getMobListDebug("Goblin", new HashSet<>());
  }

  List<Pair<BlockPos, List<Float>>> btd = new ArrayList<>();

  @SubscribeEvent
  public void onRender(RenderWorldLastEvent event) {
    if (entTodraw != null) {
      RenderUtil.drawBox(entTodraw.getEntityBoundingBox(), new Color(123, 214, 44, 150));
      RenderUtil.drawBlockBox(new BlockPos(entTodraw.posX, Math.ceil(entTodraw.posY) - 1, entTodraw.posZ), new Color(123, 214, 44, 150));
    }

    if (!blockToDraw.isEmpty()) {
      blockToDraw.forEach(b -> RenderUtil.drawBlockBox(b, new Color(255, 0, 0, 200)));
    }

    if (this.block != null) {
      RenderUtil.drawBlockBox(this.block, new Color(255, 0, 0, 50));
    }

    if (this.first != null) {
      RenderUtil.drawBlockBox(new BlockPos(this.first.toVec3()), new Color(0, 0, 0, 200));
    }

    if (this.second != null) {
      RenderUtil.drawBlockBox(new BlockPos(this.second.toVec3()), new Color(0, 0, 0, 200));
    }

//    if (pathfinder != null) {
//      pathfinder.getClosedSet().values().forEach(it -> RenderUtil.drawBlockBox(it.getBlock(), new Color(213, 124, 124, 100)));
//    }

    if (path != null) {
      path.getPath().forEach(tit -> RenderUtil.drawBlockBox(tit, new Color(255, 0, 0, 200)));
    }

    if (curr != null) {
      RenderUtil.drawBlockBox(curr.getBlock(), new Color(0, 255, 0, 255));
      if (curr.getParentNode() != null) {
        RenderUtil.drawBlockBox(curr.getParentNode().getBlock(), new Color(0, 0, 255, 255));
      }
    }
//
    if (!mobs.isEmpty()) {
      Pair<EntityPlayer, Pair<Double, Double>> best = mobs.get(0);
      Vec3 pos = best.getFirst().getPositionVector();
      RenderUtil.drawBox(new AxisAlignedBB(pos.xCoord - 0.5, pos.yCoord, pos.zCoord - 0.5, pos.xCoord + 0.5, pos.yCoord + 2, pos.zCoord + 0.5),
          new Color(255, 0, 241, 150));
      RenderUtil.drawText(String.format("Dist: %.2f, Angle: %.2f", best.getSecond().getFirst(), best.getSecond().getSecond()), pos.xCoord,
          pos.yCoord + 2.2, pos.zCoord, 1);

      for (int i = 1; i < mobs.size(); i++) {
        best = mobs.get(i);
        pos = best.getFirst().getPositionVector();
        RenderUtil.drawBox(new AxisAlignedBB(pos.xCoord - 0.5, pos.yCoord, pos.zCoord - 0.5, pos.xCoord + 0.5, pos.yCoord + 2, pos.zCoord + 0.5),
            new Color(123, 0, 234, 150));
        RenderUtil.drawText(String.format("Dist: %.2f, Angle: %.2f", best.getSecond().getFirst(), best.getSecond().getSecond()), pos.xCoord,
            pos.yCoord + 2.2, pos.zCoord, 1);
      }
    }

    if (!btd.isEmpty()) {
      Pair<BlockPos, List<Float>> best = btd.get(0);
      BlockPos pos = best.getFirst();
      RenderUtil.drawBlockBox(best.getFirst(), new Color(123, 0, 234, 150));
      if (com.jelly.mightyminerv2.Util.BlockUtil.getBlockLookingAt().equals(pos)) {
        RenderUtil.drawText(
            String.format("Hardness: %.2f, Angle: %.2f, Dist: %.2f", best.getSecond().get(0), best.getSecond().get(1), best.getSecond().get(2)),
            pos.getX() + 0.5,
            pos.getY() + 1.2, pos.getZ() + 0.5, 1);
      }

      for (int i = 1; i < btd.size(); i++) {
        best = btd.get(i);
        pos = best.getFirst();
        RenderUtil.drawBlockBox(best.getFirst(), new Color(255, 0, 241, 50));
        if (com.jelly.mightyminerv2.Util.BlockUtil.getBlockLookingAt().equals(pos)) {
          RenderUtil.drawText(
              String.format("Hardness: %.2f, Angle: %.2f, Dist: %.2f", best.getSecond().get(0), best.getSecond().get(1), best.getSecond().get(2)),
              pos.getX() + 0.5,
              pos.getY() + 1.2, pos.getZ() + 0.5, 1);
        }
      }
    }
  }

  @SubCommand
  public void trav() {
    double walkSpeed = mc.thePlayer.getAIMoveSpeed();
    CalculationContext ctx = new CalculationContext(MightyMiner.instance, walkSpeed * 1.3, walkSpeed, walkSpeed * 0.3);
    BlockPos pp = PlayerUtil.getBlockStandingOn();
    EnumFacing d = mc.thePlayer.getHorizontalFacing();
    MovementResult res = new MovementResult();
    Movement trav = new MovementTraverse(MightyMiner.instance, pp, pp.add(d.getFrontOffsetX(), d.getFrontOffsetY(), d.getFrontOffsetZ()));
    trav.calculateCost(ctx, res);
    LogUtil.send("Movement cost: " + res.getCost());
    this.block = res.getDest();
  }


  @SubCommand
  public void asc() {
    CalculationContext ctx = new CalculationContext(MightyMiner.instance);
    BlockPos pp = PlayerUtil.getBlockStandingOn();
    EnumFacing d = mc.thePlayer.getHorizontalFacing();
    MovementResult res = new MovementResult();
    Movement trav = new MovementAscend(MightyMiner.instance, pp, pp.add(d.getFrontOffsetX(), d.getFrontOffsetY() + 1, d.getFrontOffsetZ()));
    trav.calculateCost(ctx, res);
    LogUtil.send("Movement cost: " + res.getCost());
    this.block = res.getDest();
  }

  @SubCommand
  public void desc() {
    double walkSpeed = mc.thePlayer.getAIMoveSpeed();
    CalculationContext ctx = new CalculationContext(MightyMiner.instance, walkSpeed * 1.3, walkSpeed, walkSpeed * 0.3);
    BlockPos pp = PlayerUtil.getBlockStandingOn();
    EnumFacing d = mc.thePlayer.getHorizontalFacing();
    MovementResult res = new MovementResult();
    Movement trav = new MovementDescend(MightyMiner.instance, pp, pp.add(d.getFrontOffsetX(), d.getFrontOffsetY() - 1, d.getFrontOffsetZ()));
    trav.calculateCost(ctx, res);
    LogUtil.send("Movement cost: " + res.getCost());
    this.block = res.getDest();
  }

  @SubCommand
  public void diag() {
    double walkSpeed = mc.thePlayer.getAIMoveSpeed();
    CalculationContext ctx = new CalculationContext(MightyMiner.instance, walkSpeed * 1.3, walkSpeed, walkSpeed * 0.3);
    BlockPos pp = PlayerUtil.getBlockStandingOn();
    MovementResult res = new MovementResult();
    Movement diag = new MovementDiagonal(MightyMiner.instance, pp, pp.add(1, 0, 1));
    diag.calculateCost(ctx, res);
    LogUtil.send("Movement cost: " + res.getCost());
    this.block = res.getDest();
  }
}
