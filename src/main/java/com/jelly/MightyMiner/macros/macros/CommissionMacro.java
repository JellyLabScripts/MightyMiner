package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathFinderBehaviour;
import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.ScoreboardUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class CommissionMacro extends Macro {
    private AutoMineBaritone autoMineBaritone;
    public enum Quest {
        ROYAL_MINES,
        LAVA_SPRINGS,
        UPPER_MINES,
        FAR_RESERVE,
        RAMPART_QUARRY,
        CLIFFSIDE_VEINS,
        NONE
    }
    public enum State {
        GETTING_COMMISION,
        SEARCHING,
        TELEPORTING,
        MINING,
        NONE
    }

    private final List<Block> allowedBlocks = new ArrayList<>();
    @Override
    protected void onEnable() {
        allowedBlocks.clear();

        allowedBlocks.add(Blocks.prismarine);
        allowedBlocks.add(Blocks.wool);
        allowedBlocks.add(Blocks.stained_hardened_clay);
        allowedBlocks.add(Blocks.stone);

        List<String> scoreboardLines = ScoreboardUtils.getScoreboardLines();

        scoreboardLines.stream()
                .forEach(s -> {
                    if (s.equals(""))
                });
    }
    @Override
    public void onTick(TickEvent.Phase phase) {
        mineBaritone.onTickEvent(phase);
    }

    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event) {
        mineBaritone.onOverlayRenderEvent(event);
    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {
        mineBaritone.onRenderEvent();
    }

    @Override
    protected void onDisable() {
        mineBaritone.disableBaritone();

    }

    private BaritoneConfig baritoneConfig(){
        return new BaritoneConfig(
                AutoMineType.STATIC,
                true,
                true,
                true,
                350,
                8,
                null,

        );
    }
}
