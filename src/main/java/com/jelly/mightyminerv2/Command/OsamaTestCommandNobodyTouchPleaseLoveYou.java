package com.jelly.mightyminerv2.Command;

import static java.lang.Math.ceil;

import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.mightyminerv2.Feature.impl.AutoCommissionClaim;
import com.jelly.mightyminerv2.Feature.impl.AutoMobKiller;
import com.jelly.mightyminerv2.Feature.impl.MithrilMiner;
import com.jelly.mightyminerv2.Feature.impl.RouteNavigator;
import com.jelly.mightyminerv2.Handler.GraphHandler;
import com.jelly.mightyminerv2.Handler.RouteHandler;
import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.Util.LogUtil;
import com.jelly.mightyminerv2.Util.PlayerUtil;
import com.jelly.mightyminerv2.Util.RenderUtil;
import com.jelly.mightyminerv2.Util.StrafeUtil;
import com.jelly.mightyminerv2.Util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.Util.helper.route.TransportMethod;
import com.jelly.mightyminerv2.pathfinder.calculate.PathNode;
import com.jelly.mightyminerv2.pathfinder.calculate.path.AStarPathFinder;
import com.jelly.mightyminerv2.pathfinder.calculate.path.PathExecutor;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

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

  @Main
  public void main() {
    if (StrafeUtil.enabled) {
      StrafeUtil.enabled = false;
    } else {
      StrafeUtil.enabled = true;
      StrafeUtil.yaw = 0;
    }
//    BlockPos pos = new BlockPos(mc.thePlayer.posX, ceil(mc.thePlayer.posY) - 1, mc.thePlayer.posZ);
//    this.block = pos;
//    IBlockState state = mc.theWorld.getBlockState(pos);
//    LogUtil.send("CurrMax: " + state.getBlock().getCollisionBoundingBox(mc.theWorld, pos, state).maxY);
//    for(PathNode node: path.getNode()){
//      if(node.getBlock().equals(pos)){
//        LogUtil.send("Found Block. Node: " + node + ", Parent: " + node.getParentNode());
//      }
//    }
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
    blockToDraw.clear();
    path.forEach(i -> blockToDraw.add(new BlockPos(i.toVec3())));
  }

  @SubCommand
  public void k() {
    if (AutoMobKiller.getInstance().isRunning()) {
      AutoMobKiller.getInstance().stop();
    } else {
      AutoMobKiller.getInstance().start();
    }
  }

  @SubCommand
  public void b() {
    this.block = new BlockPos((int) mc.thePlayer.posX, (int) mc.thePlayer.posY - 1, (int) mc.thePlayer.posZ);
  }

  @SubCommand
  public void between() {
    if (this.block != null) {
      this.blockToDraw.clear();
      List<BlockPos> bs = BlockUtil.INSTANCE.bresenham(new CalculationContext(MightyMiner.instance),
          new Vec3(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ), new Vec3(this.block));
      this.blockToDraw.addAll(bs);
    }
  }

  @SubCommand
  public void mine(final String t) {
    if (!MithrilMiner.getInstance().isRunning()) {
      int[] p = new int[]{1, 1, 1, 1};
      if (t.equals("t")) {
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
  public void clear() {
    blockToDraw.clear();
    entTodraw = null;
    block = null;
    path = null;
    first = null;
    second = null;
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
  public void go(int go) {
    Multithreading.schedule(() -> {
      try {
        double walkSpeed = mc.thePlayer.getAIMoveSpeed();
        CalculationContext ctx = new CalculationContext(MightyMiner.instance, walkSpeed * 1.3, walkSpeed, walkSpeed * 0.3);
        AStarPathFinder pathfinder = new AStarPathFinder(MathHelper.floor_double(mc.thePlayer.posX),
            MathHelper.ceiling_double_int(mc.thePlayer.posY) - 1, MathHelper.floor_double(mc.thePlayer.posZ),
            new Goal(this.block.getX(), this.block.getY(), this.block.getZ(), ctx), ctx);
        long start = System.nanoTime();
        Path path = pathfinder.calculatePath();
        long end = System.nanoTime();
        if (path == null) {
          LogUtil.send("No Path Found");
          return;
        }
        this.path = path;
        LogUtil.send("Time Took: " + ((end - start) / 1e6));
        blockToDraw.clear();
        blockToDraw.addAll(path.getSmoothedPath());
        if (go == 1) {
          PathExecutor.INSTANCE.start(path);
        } else {
          LogUtil.send("go is not 1. not pathing");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }, 0, TimeUnit.MILLISECONDS);
  }

  @SubCommand(aliases = {"s"})
  public void stop() {
    PathExecutor.INSTANCE.stop();
  }

  @SubscribeEvent
  public void onRender(RenderWorldLastEvent event) {
    if (entTodraw != null) {
      RenderUtil.drawBox(((EntityLivingBase) entTodraw).getEntityBoundingBox(), Color.CYAN);
    }

    if (!blockToDraw.isEmpty()) {
      for (int i = 0; i < blockToDraw.size(); i++) {
        BlockPos curr = blockToDraw.get(i);
        RenderUtil.drawBlockBox(curr, new Color(0, 255, 255, 50));
        if (i + 1 < blockToDraw.size()) {
          BlockPos n = blockToDraw.get(i + 1);
          RenderUtil.drawTracer(
              new Vec3(curr.getX(), curr.getY(), curr.getZ()).addVector(0.5, 1, 0.5),
              new Vec3(n.getX(), n.getY(), n.getZ()).addVector(0.5, 1, 0.5),
              new Color(0, 0, 0, 200)
          );
        }
      }
    }

    if (!points.isEmpty()) {
      points.forEach(it -> RenderUtil.drawPoint(it, new Color(255, 0, 0, 100)));
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
  }

  @SubCommand
  public void trav() {
    double walkSpeed = mc.thePlayer.getAIMoveSpeed();
    CalculationContext ctx = new CalculationContext(MightyMiner.instance, walkSpeed * 1.3, walkSpeed, walkSpeed * 0.3);
    BlockPos pp = new BlockPos(mc.thePlayer.posX, ceil(mc.thePlayer.posY) - 1, mc.thePlayer.posZ);
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
    BlockPos pp = new BlockPos(mc.thePlayer.posX, ceil(mc.thePlayer.posY) - 1, mc.thePlayer.posZ);
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
    BlockPos pp = new BlockPos(mc.thePlayer.posX, ceil(mc.thePlayer.posY) - 1, mc.thePlayer.posZ);
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
    BlockPos pp = new BlockPos(mc.thePlayer.posX, ceil(mc.thePlayer.posY) - 1, mc.thePlayer.posZ);
    MovementResult res = new MovementResult();
    Movement diag = new MovementDiagonal(MightyMiner.instance, pp, pp.add(1, 0, 1));
    diag.calculateCost(ctx, res);
    LogUtil.send("Movement cost: " + res.getCost());
    this.block = res.getDest();
  }
}
