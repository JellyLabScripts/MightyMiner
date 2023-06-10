package com.jelly.MightyMiner.features;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.DrawUtils;
import com.jelly.MightyMiner.utils.HypixelUtils.SkyblockInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;

public class Scanner {
    private final ArrayList<BlockPos> waypoints = new ArrayList<>();

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!MightyMiner.config.fairyGrottoESP || mc.theWorld == null || mc.thePlayer == null) return;

        if (SkyblockInfo.onCrystalHollows()) {
            Multithreading.runAsync(() -> handleChunkLoad(event.getChunk()));
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        waypoints.clear();
    }

    private void handleChunkLoad(Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 200; y++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    BlockPos pos = new BlockPos(chunk.xPosition * 16 + x, y, chunk.zPosition * 16 + z);
                    if (block == Blocks.stained_glass && BlockUtils.getBlockState(pos).getValue(BlockColored.COLOR) == EnumDyeColor.MAGENTA) {
                        if (!waypoints.contains(pos)) {
                            waypoints.add(pos);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!MightyMiner.config.fairyGrottoESP || mc.theWorld == null || mc.thePlayer == null) return;

        if (SkyblockInfo.onCrystalHollows()) {
            for (BlockPos blockPos : waypoints) {
                DrawUtils.drawBlockBox(blockPos, new OneColor(Color.MAGENTA), 1);
            }
        }
    }
}
