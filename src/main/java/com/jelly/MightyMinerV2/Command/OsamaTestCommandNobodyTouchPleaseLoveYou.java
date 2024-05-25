package com.jelly.MightyMinerV2.Command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.MightyMinerV2.Feature.impl.AutoCommissionClaim;
import com.jelly.MightyMinerV2.Feature.impl.MithrilMiner;
import com.jelly.MightyMinerV2.Feature.impl.RouteNavigator;
import com.jelly.MightyMinerV2.Handler.GameStateHandler;
import com.jelly.MightyMinerV2.Handler.RouteHandler;
import com.jelly.MightyMinerV2.Util.*;
import com.jelly.MightyMinerV2.Util.LogUtil.ELogType;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
    entTodraw = EntityUtil.getCeanna().orElse(null);
    LogUtil.send("Entity Found: " + entTodraw, ELogType.SUCCESS);
    mc.theWorld.playerEntities.forEach(System.out::println);
  }

  private boolean canStandOn(final BlockPos pos) {
    return mc.theWorld.isBlockFullCube(pos)
        && mc.theWorld.isAirBlock(pos.add(0, 1, 0))
        && mc.theWorld.isAirBlock(pos.add(0, 2, 0));
  }

  @SubCommand
  public void mine() {
    if (!MithrilMiner.getInstance().isRunning()) {
      MithrilMiner.getInstance().enable(new int[]{1, 1, 1, 1});
    } else {
      MithrilMiner.getInstance().stop();
    }
  }

  @SubCommand
  public void claim(){
    if(!AutoCommissionClaim.getInstance().isRunning()){
      AutoCommissionClaim.getInstance().start();
    }else {
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
