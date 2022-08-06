package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;


public class GemstoneMacro extends Macro {


    final List<Block> blocksAllowedToMine = new ArrayList<Block>(){
        {
            add(Blocks.stone);
            add(Blocks.gold_ore);
            add(Blocks.emerald_ore);
            add(Blocks.redstone_ore);
            add(Blocks.iron_ore);
            add(Blocks.coal_ore);
            add(Blocks.stained_glass_pane);
            add(Blocks.stained_glass);
        }
    };
    final List<Block> blocksForbiddenToMine = new ArrayList<Block>(){
        {
            add(Blocks.dirt);
        }
    };

    AutoMineBaritone baritone = new AutoMineBaritone(blocksForbiddenToMine, blocksAllowedToMine);
    boolean minedNearbyGemstones;


    @Override
    public void onEnable() {
        minedNearbyGemstones = false;
    }

    @Override
    public void onDisable() {
        baritone.disableBaritone();
    }

    @Override
    public void onTick(TickEvent.Phase phase){
        baritone.onTickEvent(phase);

        if(phase != TickEvent.Phase.START)
            return;

        if(!baritone.isEnabled() && !minedNearbyGemstones && PlayerUtils.hasStoppedMoving()){
            baritone.enableBaritone(Blocks.stained_glass_pane, Blocks.stained_glass);
        }
    }

    @Override
    public void onLastRender() {
        baritone.onRenderEvent();
    }

    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event){
        baritone.onOverlayRenderEvent(event);
    }



}
