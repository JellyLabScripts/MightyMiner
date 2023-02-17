package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.features.FuelFilling;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.BlockUtils.BlockData;
import com.jelly.MightyMiner.utils.HypixelUtils.MineUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

public class MithrilMacro extends Macro {

    AutoMineBaritone baritone;
    ArrayList<BlockData<EnumDyeColor>> mithPriorityList = new ArrayList<>();

    @Override
    protected void onEnable() {
        LogUtils.debugLog("Enabled Mithril macro checking if player is near");

        if(MightyMiner.config.playerFailsafe) {
            if(PlayerUtils.isNearPlayer(MightyMiner.config.playerRad)){
                LogUtils.addMessage("Didnt start macro since therese a player near");
                this.enabled = false;
                onDisable();
                return;
            }
        }

        mithPriorityList.clear();
        //mithPriorityList.addAll(BlockUtils.addData(new ArrayList<Block>(){{add((Block) Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE));}}));
        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority1));
        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority2));
        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority3));

        baritone = new AutoMineBaritone(getMineBehaviour());
    }

    @Override
    public void FailSafeDisable() {
        if (baritone == null) return;
        MacroHandler.disableScript();
    }


    @Override
    public void onTick(TickEvent.Phase phase) {
        if (!enabled) return;

        if (MightyMiner.config.refuelWithAbiphone) {
            if (FuelFilling.isRefueling()) {
                if (baritone != null && baritone.getState() != AutoMineBaritone.BaritoneState.IDLE) {
                    baritone.disableBaritone();
                }
                return;
            }
        }

        if(phase != TickEvent.Phase.START)
            return;

        switch(baritone.getState()){
            case IDLE: case FAILED:
                baritone.mineFor(mithPriorityList);
                break;
        }

        checkMiningSpeedBoost();
    }






    @Override
    protected void onDisable() {
        if(baritone != null) baritone.disableBaritone();
        KeybindHandler.resetKeybindState();
    }

    private BaritoneConfig getMineBehaviour(){
        return new BaritoneConfig(
                MiningType.STATIC,
                MightyMiner.config.mithShiftWhenMine,
                true,
                true,
                MightyMiner.config.mithRotationTime,
                MightyMiner.config.mithRestartTimeThreshold,
                null,
                null,
               256,
                0
        );
    }
}
