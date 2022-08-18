package com.jelly.MightyMiner.baritone.autowalk;

import com.jelly.MightyMiner.baritone.autowalk.pathing.config.PathBehaviour;
import com.jelly.MightyMiner.baritone.autowalk.pathing.AStarPathFinder;
import com.jelly.MightyMiner.baritone.logging.Logger;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.baritone.structures.GridEnvironment;
import com.jelly.MightyMiner.baritone.structures.Node;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WalkBaritone{
  //Custom grid example

    //Xxxxxxxxxxxxxx |
    //xxxxxxxxxxxxxx |
    //xxxxxxKxxxxxxx | MaxZ
    //xxxxxxxxxxxxxx |
    //xxxxxxxxxxxxxx |
    //<------------->
    //     MaxX
    // Y is the same as world coordinate

    // program coverts world coordinates to custom grid first
    // custom grid starts takes X as [0, y, 0]
    // player starts on K initially [maxX/2, y, maxZ/2]
    Minecraft mc= Minecraft.getMinecraft();

    LinkedList<BlockPos> blocksToWalk = new LinkedList<>();

    boolean walking;

    Rotation rotation = new Rotation();
    int deltaJumpTick = 0;

    AStarPathFinder pathFinder;



    private void clearBlocksToWalk(){
        blocksToWalk.clear();
        BlockRenderer.renderMap.clear();
    }


    public void onEnable(BlockPos destinationBlock) {

        clearBlocksToWalk();
        pathFinder = new AStarPathFinder(new PathBehaviour());
        new Thread(() -> {
            BlockRenderer.renderMap.put(destinationBlock, Color.RED);
            blocksToWalk = pathFinder.getPath(destinationBlock);
            if(blocksToWalk.isEmpty()){
                Logger.playerLog("Can't find path!");
                return;
            }
            for(BlockPos blockPos : blocksToWalk){
                BlockRenderer.renderMap.put(blockPos, Color.ORANGE);
            }
            walking = true;
        }).start();
    }



    public void disableBaritone() {
        walking = false;
        clearBlocksToWalk();
    }


    public void onOverlayRenderEvent(RenderGameOverlayEvent event){
        if(event.type == RenderGameOverlayEvent.ElementType.TEXT){
            if(blocksToWalk != null){
                if(!blocksToWalk.isEmpty()){
                    for(int i = 0; i < blocksToWalk.size(); i++){
                        mc.fontRendererObj.drawString(blocksToWalk.get(i).toString() , 5, 5 + 10 * i, -1);
                    }
                }
            }
        }
    }

    public void onTickEvent(TickEvent.Phase phase){

        if(phase != TickEvent.Phase.START)
            return;

        if(walking) {
            if (rotation.rotating) {
                KeybindHandler.resetKeybindState();
                return;
            }
          //  KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, blocksToWalk.size() <= 5 || !BlockUtils.isAStraightLine(blocksToWalk.get(blocksToWalk.size() - 1), blocksToWalk.get(blocksToWalk.size() - 3), blocksToWalk.get(blocksToWalk.size() - 5)));
            if (BlockUtils.getPlayerLoc().equals(blocksToWalk.getLast())) {
                BlockRenderer.renderMap.remove(blocksToWalk.getLast());
                blocksToWalk.remove(blocksToWalk.size() - 1);

                if (blocksToWalk.size() == 0) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("Finished baritone"));
                    walking = false;
                    KeybindHandler.resetKeybindState();
                    disableBaritone();
                    return;
                }
            }

            rotation.intLockAngle((AngleUtils.getRequiredYaw(blocksToWalk.getLast())), 0, 500);
            if (blocksToWalk.getLast().getY() > (int) mc.thePlayer.posY) {
                if (rotation.completed) {
                    deltaJumpTick = 3;
                }
            }
            if (deltaJumpTick > 0) {
                deltaJumpTick--;
                KeybindHandler.setKeyBindState(KeybindHandler.keyBindJump, true);
            } else   KeybindHandler.setKeyBindState(KeybindHandler.keyBindJump, false);

            KeybindHandler.setKeyBindState(KeybindHandler.keybindW, !rotation.rotating);
        }

    }

    public void onRenderEvent(){
        if(rotation.rotating)
            rotation.update();
    }

}
