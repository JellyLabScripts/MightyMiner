package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.MineBehaviour;
import com.jelly.MightyMiner.baritone.automine.pathing.config.PathBehaviour;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
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


    AutoMineBaritone baritone;
    boolean minedNearbyGemstones;


    @Override
    public void onEnable() {
        baritone = new AutoMineBaritone(getPathBehaviour(), getMineBehaviour());
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

    private PathBehaviour getPathBehaviour(){
        return new PathBehaviour(
                blocksForbiddenToMine,
                blocksAllowedToMine,
                MightyMiner.config.gemMaxY,
                MightyMiner.config.gemMinY
        );
    }
    private MineBehaviour getMineBehaviour(){
        return new MineBehaviour(
                false,
                MightyMiner.config.gemRotationTime
        );
    }



}
