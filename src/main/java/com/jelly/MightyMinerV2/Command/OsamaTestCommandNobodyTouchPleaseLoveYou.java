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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Command(value = "set")
public class OsamaTestCommandNobodyTouchPleaseLoveYou {

  private static OsamaTestCommandNobodyTouchPleaseLoveYou instance;

  public static OsamaTestCommandNobodyTouchPleaseLoveYou getInstance() {
    if (instance == null) {
      instance = new OsamaTestCommandNobodyTouchPleaseLoveYou();
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
  public void pathfind(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
    World world = Minecraft.getMinecraft().theWorld;
    BlockPos start = new BlockPos(x1, y1, z1);
    BlockPos end = new BlockPos(x2, y2, z2);

    List<BlockPos> path = PathfinderUtil.findPath(world, start, end);

    if (path.isEmpty()) {
      LogUtil.send("Could not find path", ELogType.ERROR);
    } else {
      LogUtil.send("Found path: " + path, ELogType.SUCCESS);
      blockToDraw = path;
    }
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
