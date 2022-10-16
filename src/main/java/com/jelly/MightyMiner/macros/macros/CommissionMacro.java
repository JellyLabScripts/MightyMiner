package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.MineBehaviour;
import com.jelly.MightyMiner.baritone.autowalk.WalkBaritone;
import com.jelly.MightyMiner.baritone.autowalk.config.AutowalkConfig;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class CommissionMacro extends Macro {
    WalkBaritone baritone = new WalkBaritone(getAutowalkConfig());
    AutoMineBaritone mineBaritone = new AutoMineBaritone(getMineBehaviour());

    @Override
    protected void onEnable() {
        mineBaritone.enableBaritone(new BlockPos(5, 1, -9));
       // LogUtils.addMessage(BlockUtils.getRelativeBlock(0, 0, 1).toString());
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
    public void onLastRender() {
        mineBaritone.onRenderEvent();
    }

    @Override
    protected void onDisable() {
        mineBaritone.disableBaritone();

    }


    private AutowalkConfig getAutowalkConfig(){
        return new AutowalkConfig(
                MightyMiner.config.comBarSafeIndex,
                MightyMiner.config.comBarRotationTime);
    }
    private MineBehaviour getMineBehaviour(){
        return new MineBehaviour(
                AutoMineType.DYNAMIC,
                false,
                true,
                false,
                50,
                8,
                new ArrayList<Block>(){
                    {
                        add(Blocks.chest);
                        add(Blocks.trapped_chest);
                    }
                },
                null,
                256,
                0
        );
    }
}
