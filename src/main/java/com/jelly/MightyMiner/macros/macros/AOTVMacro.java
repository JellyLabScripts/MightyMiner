package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.MineBehaviour;
import com.jelly.MightyMiner.baritone.autowalk.WalkBaritone;
import com.jelly.MightyMiner.baritone.autowalk.config.AutowalkConfig;
import com.jelly.MightyMiner.handlers.KeybindHandler;
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

import java.util.ArrayList;
import java.util.List;

public class AOTVMacro extends Macro {
    AutoMineBaritone baritone = new AutoMineBaritone(getAutoMineConfig());

    enum State{
        NONE,
        Teleporting,
      //  Waiting,
        Mining
    }

    State currentState = State.NONE;

    BlockPos targetCoordinate;
    int currentCoordIndex;
    Rotation rotation = new Rotation();

    boolean rightClickFlag;

    List<BlockPos> coords = MightyMiner.coords;



    @Override
    protected void onEnable() {
        currentState = State.NONE;


        coords.forEach(System.out::println);
       currentCoordIndex = 0;
       targetCoordinate = coords.get(currentCoordIndex);
    }

    @Override
    public void onTick(TickEvent.Phase phase) { // assume start on first? ?!

        baritone.onTickEvent(phase);
        if(rotation.rotating){
            KeybindHandler.resetKeybindState();
            return;
        }

        // temp
        LogUtils.addMessage(currentState.toString());


        switch(currentState) {
            case NONE:
                break;
            case Teleporting:
                KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);
                baritone.disableBaritone();
                rotation.intLockAngle(AngleUtils.getRequiredYawCenter(targetCoordinate), AngleUtils.getRequiredPitchCenter(targetCoordinate), 20);
                if(rightClickFlag && !rotation.rotating){
                    mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Void");
                    KeybindHandler.rightClick();
                    LogUtils.addMessage("I've right clicked!");
                    rightClickFlag = false;
                }
                break;
            case Mining:
                if(!baritone.isEnabled()){
                    new Thread(() -> {
                        try {
                            baritone.enableBaritoneInThread(Blocks.stained_glass, Blocks.stained_glass_pane);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();

                }
        }
        updateState();
    }

    public void updateState(){
        switch(currentState){
            case NONE:
                currentState = State.Teleporting;
                rightClickFlag = true;
                return;
            case Teleporting:
                if(BlockUtils.onTheSameXZ(BlockUtils.getPlayerLoc(), targetCoordinate))
                    currentState = State.Mining;
                return;
            case Mining:
                if((BlockUtils.findBlock(6, Blocks.stained_glass_pane, Blocks.stained_glass).isEmpty())){
                    baritone.disableBaritone();
                    currentState = State.NONE;
                    if(currentCoordIndex < coords.size() - 1) {
                        targetCoordinate = coords.get(currentCoordIndex + 1);
                        currentCoordIndex ++;
                    }
                    else {
                        targetCoordinate = coords.get(0);
                        currentCoordIndex = 0;
                    }
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
                100,
                8, //changed with config
                null,
                null,
                256,
                0

        );
    }
}


