package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.MineBehaviour;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AOTVMacro extends Macro {
    AutoMineBaritone baritone;

    enum State{
        NONE,
        Teleporting,
        Mining
    }



    State currentState = State.NONE;

    BlockPos targetCoordinate;
    int targetCoordIndex;
    int rightClickCD;
    boolean rotationFlag;
    Rotation rotation = new Rotation();

    List<BlockPos> coords;




    @Override
    protected void onEnable() {
        baritone = new AutoMineBaritone(getAutoMineConfig());
        currentState = State.Mining;

        coords = MightyMiner.coordsConfig.getSelectedRoute().valueList();
  
        targetCoordIndex = -1;
        for(int i = 0; i < coords.size(); i++){
            if(BlockUtils.getPlayerLoc().down().equals(coords.get(i))){
                targetCoordIndex = i;
            }
        }

        if(targetCoordIndex == -1){
            LogUtils.addMessage("You must stand on one of the coordinates to start!");
            MacroHandler.disableScript();
            return;
        }
        targetCoordinate = coords.get(targetCoordIndex);
    }

    @Override
    public void onTick(TickEvent.Phase phase) {

        baritone.onTickEvent(phase);

        if(phase != TickEvent.Phase.START) return;

        if(targetCoordIndex == -1) return;

        switch(currentState) {
            case NONE:
                break;
            case Teleporting:

                if(rightClickCD >= 0)
                    rightClickCD--;

                KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);

                if(rotationFlag)
                    rotation.intLockAngle(AngleUtils.getRequiredYawCenter(targetCoordinate), AngleUtils.getRequiredPitchCenter(targetCoordinate),  500);


                if(rightClickCD == -1)
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, false);
                else if(!rotation.rotating && rightClickCD < 2) {
                    rotationFlag = false;
                    rotation.reset();
                    mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Void");
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, true);
                }


                break;
            case Mining:
                useMiningSpeedBoost();
                if(!baritone.isEnabled()) {
                    try {
                        baritone.mineForInSingleThread(Blocks.stained_glass, Blocks.stained_glass_pane);
                    } catch (Exception ignored) {
                        currentState = State.NONE;
                        baritone.disableBaritone();
                        if(targetCoordIndex < coords.size() - 1) {
                            targetCoordinate = coords.get(targetCoordIndex + 1);
                            targetCoordIndex++;
                        }
                        else {
                            targetCoordinate = coords.get(0);
                            targetCoordIndex = 0;
                        }
                    }
                }
        }
        updateState();
    }

    public void updateState(){
        switch(currentState){
            case NONE:
                currentState = State.Teleporting;
                rotationFlag = true;
                rightClickCD = 15;
                LogUtils.debugLog("Going to coordinates " + targetCoordinate.getX() + " " + targetCoordinate.getY() + " " + targetCoordinate.getZ());
                return;
            case Teleporting:
                if((BlockUtils.getPlayerLoc().down().equals(targetCoordinate))) {
                    currentState = State.Mining;
                    KeybindHandler.resetKeybindState();
                }
        }
    }
    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event) {
        baritone.onOverlayRenderEvent(event);
    }


    public static void drawRoutes(List<BlockPos> coords, RenderWorldLastEvent event) {
      /*  if (MightyMiner.config.highlightRouteBlocks) {
            coords.forEach(coord -> RenderUtils.drawBlockBox(coord, MightyMiner.config.routeBlocksColor, 3, event.partialTicks));
        }*/

        if (MightyMiner.config.showRouteLines) {
            DrawUtils.drawCoordsRoute(coords, event);
        }
    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {
        baritone.onRenderEvent();
        if(rotation.rotating)
            rotation.update();

        drawRoutes(coords, event);
    }

    @Override
    protected void onDisable() {
        baritone.disableBaritone();
    }


    private MineBehaviour getAutoMineConfig(){
        return new MineBehaviour(
                AutoMineType.STATIC,
                true,
                false,
                false,
                MightyMiner.config.aotvRotationTime,
                MightyMiner.config.aotvRestartTimeThreshold, //changed with config
                null,
                null,
                256,
                0

        );
    }
}


