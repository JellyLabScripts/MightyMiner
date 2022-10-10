package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.MineBehaviour;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class AOTVMacro extends Macro {
    AutoMineBaritone baritone = new AutoMineBaritone(getAutoMineConfig());

    enum State{
        NONE,
        Teleporting,
        Mining
    }

    State currentState = State.NONE;

    BlockPos targetCoordinate;
    int targetCoordIndex;
    int rightClickCD;
    Rotation rotation = new Rotation();

    boolean rightClickFlag;

    List<BlockPos> coords = MightyMiner.coords;




    @Override
    protected void onEnable() {
        currentState = State.NONE;


        coords.forEach(System.out::println);
  
        targetCoordIndex = -1;
        for(int i = 0; i < coords.size(); i++){
            if(BlockUtils.getPlayerLoc().down().equals(coords.get(i))){
                if(i == coords.size() - 1)
                    targetCoordIndex = 0;
                  else
                    targetCoordIndex = i + 1;

            }
        }
        if(targetCoordIndex == -1){
            LogUtils.addMessage("You must stand on one of the coordinates to start!");
            onDisable();
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
                rightClickCD--;
                KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);
                rotation.intLockAngle(AngleUtils.getRequiredYawCenter(targetCoordinate), AngleUtils.getRequiredPitchCenter(targetCoordinate),  200);
                if(rightClickFlag && !rotation.rotating && rightClickCD == 0){
                    mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Void");
                    KeybindHandler.onTick(KeybindHandler.keybindUseItem);
                    rightClickFlag = false;
                }
                break;
            case Mining:
                if(!baritone.isEnabled()) {
                    try {
                        baritone.enableBaritoneSingleThread(Blocks.stained_glass, Blocks.stained_glass_pane);
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
                rightClickCD = 10;
                rightClickFlag = true;
                LogUtils.debugLog("Going to coordinates " + targetCoordinate.getX() + " " + targetCoordinate.getY() + " " + targetCoordinate.getZ());
                return;
            case Teleporting:
                if((BlockUtils.getPlayerLoc().down().equals(targetCoordinate)))
                    currentState = State.Mining;
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


