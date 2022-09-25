package com.jelly.MightyMiner.baritone.autowalk;

import com.jelly.MightyMiner.baritone.autowalk.movement.Input;
import com.jelly.MightyMiner.baritone.autowalk.movement.InputHandler;
import com.jelly.MightyMiner.baritone.autowalk.movement.Moves;
import com.jelly.MightyMiner.baritone.autowalk.pathing.config.PathBehaviour;
import com.jelly.MightyMiner.baritone.autowalk.pathing.AStarPathFinder;
import com.jelly.MightyMiner.baritone.logging.Logger;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.collections4.map.LinkedMap;

import java.awt.*;

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

    LinkedMap<BlockPos, Moves>  blocksToWalk = new LinkedMap<>();

    boolean walking;

    Rotation rotation = new Rotation();


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
            for(BlockPos blockPos : blocksToWalk.keySet()){
                BlockRenderer.renderMap.put(blockPos, Color.ORANGE);
            }
            walking = true;
        }).start();
    }



    public void disableBaritone() {
        walking = false;
        InputHandler.clearAllKeys();
        clearBlocksToWalk();
        Logger.playerLog("Baritone disabled");
    }


    public void onOverlayRenderEvent(RenderGameOverlayEvent event){
        if(event.type == RenderGameOverlayEvent.ElementType.TEXT){
            if(blocksToWalk != null){
                if(!blocksToWalk.isEmpty()){
                    for(int i = 0; i < blocksToWalk.size(); i++){
                        if(blocksToWalk.get(blocksToWalk.get(i)) != null)
                            mc.fontRendererObj.drawString(blocksToWalk.get(i) + " " + blocksToWalk.get(blocksToWalk.get(i)).toString(), 5, 5 + 10 * i, -1);
                    }
                }
            }
        }
    }

    Moves lastMove;
    boolean jumpFlag;

    public void onTickEvent(TickEvent.Phase phase){

        if(phase != TickEvent.Phase.START)
            return;

        if(walking) {

            if(blocksToWalk.isEmpty()){
                disableBaritone();
                return;
            }


            if(BlockUtils.onTheSameXZ(BlockUtils.getPlayerLoc(), blocksToWalk.lastKey())){
                BlockRenderer.renderMap.remove(blocksToWalk.lastKey());
                lastMove = blocksToWalk.remove(blocksToWalk.lastKey());

                if(blocksToWalk.isEmpty() || BlockUtils.getPlayerLoc().equals(blocksToWalk.firstKey())) {
                    disableBaritone();
                    return;
                }
            }


            if(lastMove != null && lastMove.dy > 0 && !jumpFlag && mc.thePlayer.posY - mc.thePlayer.lastTickPosY == 0) {
                jumpFlag = true;
            }

            rotation.intLockAngle(AngleUtils.getRequiredYaw(blocksToWalk.lastKey()), 0, 1);

            InputHandler.setInputForceState(Input.JUMP, jumpFlag);
            InputHandler.setInputForceState(Input.MOVE_FORWARD, !rotation.rotating);

            jumpFlag = false;
        }

    }

    public void onRenderEvent(){
        if(rotation.rotating)
            rotation.update();
    }

}
