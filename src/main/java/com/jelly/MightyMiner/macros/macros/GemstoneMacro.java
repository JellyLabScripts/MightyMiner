package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.baritone.Baritone;
import com.jelly.MightyMiner.baritone.baritones.AutoMineBaritone;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.BlockUtils;
import gnu.trove.iterator.TAdvancingIterator;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import tv.twitch.chat.Chat;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class GemstoneMacro extends Macro {


    final List<Block> blocksAllowedToMine = new ArrayList<Block>(){
        {
            add(Blocks.stone);
            add(Blocks.stained_glass_pane);
            add(Blocks.stained_glass);
        }
    };
    final List<Block> blocksForbiddenToMine = new ArrayList<Block>(){
        {
            add(Blocks.dirt);
        }
    };
    Baritone baritone = new AutoMineBaritone(blocksForbiddenToMine, blocksAllowedToMine);
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
    public void onTick(TickEvent.Phase phase) {
        baritone.onTickEvent(phase);

        if(phase != TickEvent.Phase.START)
            return;

        if(!baritone.isEnabled() && !minedNearbyGemstones){
            if(BlockUtils.findBlock(30, Blocks.stained_glass_pane, Blocks.stained_glass) != null) {
                baritone.enableBaritone(BlockUtils.findBlock(30, Blocks.stained_glass_pane, Blocks.stained_glass));
            } else {
                mc.thePlayer.addChatMessage(new ChatComponentText("Can't find any gemstones nearby, maybe try again?"));
                minedNearbyGemstones = true;
                MacroHandler.disableScript();
            }
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
