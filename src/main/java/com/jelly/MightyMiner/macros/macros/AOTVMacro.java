package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.features.MobKiller;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class AOTVMacro extends Macro {
    AutoMineBaritone baritone;

    enum State{
        NONE,
        Teleporting,
        Mining
    }

    private final ArrayList<AutoMineBaritone.BlockData<EnumDyeColor>> gemstonesFilter = new ArrayList<AutoMineBaritone.BlockData<EnumDyeColor>>();

    State currentState = State.NONE;

    BlockPos targetCoordinate;
    int targetCoordIndex;
    int rightClickCD;
    int ticksStuck = 0;
    boolean rotationFlag;
    Rotation rotation = new Rotation();

    List<BlockPos> coords;


    @Override
    public void Pause() {
        paused = true;
        if (baritone != null) {
            baritone.disableBaritone();
        }
        KeybindHandler.resetKeybindState();
    }

    @Override
    public void Unpause() {
        paused = false;
        if (baritone != null) {
            baritone.disableBaritone();
        }
    }

    @Override
    protected void onEnable() {
        if (MightyMiner.config.killYogs) {
            MobKiller.scanRange = MightyMiner.config.yogsRadius;
            MobKiller.setMobsNames(false, "Yog");
            MightyMiner.mobKiller.Enable();
            LogUtils.debugLog("Enabled mob killer");
        }
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

        gemstonesFilter.clear();
        gemstonesFilter.addAll(getBlockDataBasedOnPriority(MightyMiner.config.aotvGemstoneType));

        targetCoordinate = coords.get(targetCoordIndex);
    }

    public ArrayList<AutoMineBaritone.BlockData<EnumDyeColor>> getBlockDataBasedOnPriority(int priority) {
        ArrayList<AutoMineBaritone.BlockData<EnumDyeColor>> filter = new ArrayList<>();
        switch (priority) {
            case 0: {
                filter.add(new AutoMineBaritone.BlockData<EnumDyeColor>(Blocks.stained_glass, null));
                if (MightyMiner.config.aotvMineGemstonePanes) {
                    filter.add(new AutoMineBaritone.BlockData<EnumDyeColor>(Blocks.stained_glass_pane, null));
                }
                break;
            }
            case 1: {
                filter.add(new AutoMineBaritone.BlockData<EnumDyeColor>(Blocks.stained_glass, EnumDyeColor.RED));
                if (MightyMiner.config.aotvMineGemstonePanes) {
                    filter.add(new AutoMineBaritone.BlockData<EnumDyeColor>(Blocks.stained_glass_pane, EnumDyeColor.RED));
                }
                break;
            }
            case 2: {
                filter.add(new AutoMineBaritone.BlockData<EnumDyeColor>(Blocks.stained_glass, EnumDyeColor.PURPLE));
                if (MightyMiner.config.aotvMineGemstonePanes) {
                    filter.add(new AutoMineBaritone.BlockData<EnumDyeColor>(Blocks.stained_glass_pane, EnumDyeColor.PURPLE));
                }
                break;
            }
            case 3: {
                filter.add(new AutoMineBaritone.BlockData<EnumDyeColor>(Blocks.stained_glass, EnumDyeColor.LIME));
                if (MightyMiner.config.aotvMineGemstonePanes) {
                    filter.add(new AutoMineBaritone.BlockData<EnumDyeColor>(Blocks.stained_glass_pane, EnumDyeColor.LIME));
                }
                break;
            }
            case 4: {
                filter.add(new AutoMineBaritone.BlockData<EnumDyeColor>(Blocks.stained_glass, EnumDyeColor.BLUE));
                if (MightyMiner.config.aotvMineGemstonePanes) {
                    filter.add(new AutoMineBaritone.BlockData<EnumDyeColor>(Blocks.stained_glass_pane, EnumDyeColor.BLUE));
                }
                break;
            }
            case 5: {
                filter.add(new AutoMineBaritone.BlockData<EnumDyeColor>(Blocks.stained_glass, EnumDyeColor.ORANGE));
                if (MightyMiner.config.aotvMineGemstonePanes) {
                    filter.add(new AutoMineBaritone.BlockData<EnumDyeColor>(Blocks.stained_glass_pane, EnumDyeColor.ORANGE));
                }
                break;
            }
            case 6: {
                filter.add(new AutoMineBaritone.BlockData<EnumDyeColor>(Blocks.stained_glass, EnumDyeColor.YELLOW));
                if (MightyMiner.config.aotvMineGemstonePanes) {
                    filter.add(new AutoMineBaritone.BlockData<EnumDyeColor>(Blocks.stained_glass_pane, EnumDyeColor.YELLOW));
                }
                break;
            }
            default: {
                LogUtils.addMessage("Invalid priority for gemstone type!");
                break;
            }
        }
        return filter;
    }



    @Override
    public void onTick(TickEvent.Phase phase) {

        if (paused) return;

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
                    rotation.initAngleLock(AngleUtils.getRequiredYawCenter(targetCoordinate), AngleUtils.getRequiredPitchCenter(targetCoordinate),  300);


                if(rightClickCD == -1) {
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, false);
                    if (ticksStuck++ >= 20) {
                        if(!(BlockUtils.getPlayerLoc().down().equals(targetCoordinate))) {
                            LogUtils.addMessage("I'm stuck, trying to teleport again.");
                            rightClickCD = 15;
                            ticksStuck = 0;
                            rotationFlag = true;
                            return;
                        }
                    }
                } else if(!rotation.rotating && rightClickCD == 2) {
                    rotationFlag = false;
                    rotation.reset();

                    MovingObjectPosition rayTraceResult = mc.thePlayer.rayTrace(200, 1);

                    if(rayTraceResult != null && rayTraceResult.getBlockPos() != null && !rayTraceResult.getBlockPos().equals(targetCoordinate)) {
                        LogUtils.addMessage("The path is not cleared or it is set up wrongly, please clear it up before using the script! " + mc.objectMouseOver.getBlockPos() + " " + targetCoordinate);
                        MacroHandler.disableScript();
                        return;
                    }
                    mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Void");
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, true);
                }


                break;
            case Mining:
                useMiningSpeedBoost();

                switch(baritone.getState()){
                    case IDLE:
                        baritone.mineFor(gemstonesFilter);
                        break;
                    case FAILED:
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
                        break;
                }
        }
        updateState();
    }

    public void updateState(){
        switch(currentState){
            case NONE:
                currentState = State.Teleporting;
                rotationFlag = true;
                rightClickCD = 10;
                ticksStuck = 0;
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
    }



    @Override
    public void onLastRender(RenderWorldLastEvent event) {
        if(rotation.rotating)
            rotation.update();



    }

    @Override
    protected void onDisable() {
        baritone.disableBaritone();
        KeybindHandler.resetKeybindState();
        MightyMiner.mobKiller.Disable();
    }


    private BaritoneConfig getAutoMineConfig(){
        return new BaritoneConfig(
                MiningType.STATIC,
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


