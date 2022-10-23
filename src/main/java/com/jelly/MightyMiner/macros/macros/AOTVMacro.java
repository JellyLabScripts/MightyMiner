package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.MineBehaviour;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

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
        coords.forEach(System.out::println);
  
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

                // LOGICAL ERROR, need to test out
                if(!rotation.rotating && rightClickCD == 1){
                    rotationFlag = false;
                    rotation.reset();
                    mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Void");
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, true);
                } else if(rightClickCD == -1)
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, false);

                break;
            case Mining:
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

    @Override
    public void onLastRender() {
        baritone.onRenderEvent();
        if(rotation.rotating)
            rotation.update();
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


