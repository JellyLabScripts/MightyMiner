package com.jelly.MightyMinerV2.Command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.MightyMinerV2.Feature.impl.RouteNavigator;
import com.jelly.MightyMinerV2.Handler.GameStateHandler;
import com.jelly.MightyMinerV2.Handler.RouteHandler;
import com.jelly.MightyMinerV2.Util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
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
        if (instance == null) instance = new OsamaTestCommandNobodyTouchPleaseLoveYou();
        return instance;
    }

    private final Minecraft mc = Minecraft.getMinecraft();

    Entity entTodraw = null;
    List<BlockPos> blockToDraw = new ArrayList<>();

    @Main
    public void main() {
        blockToDraw.clear();
        blockToDraw.addAll(BlockUtil.getValidMithrilPositions(new int[]{1, 1, 1, 1}));
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
            RenderUtil.drawBlockBox(blockToDraw.get(0), new Color(255, 0, 0, 100));
        }
    }
}
