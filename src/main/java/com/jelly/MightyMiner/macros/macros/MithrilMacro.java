package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.features.FuelFilling;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils.BlockData;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.HypixelUtils.MineUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;

import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

public class MithrilMacro extends Macro {

    AutoMineBaritone baritone;

    boolean noMithril;

    @Override
    protected void onEnable() {
        LogUtils.debugLog("Enabled Mithril macro checking if player is near");

        if (MightyMiner.config.playerFailsafe) {
            if (PlayerUtils.isNearPlayer(MightyMiner.config.playerRad)) {
                LogUtils.addMessage("Didnt start macro since therese a player near");
                this.enabled = false;
                onDisable();
                return;
            }
        }

        noMithril = false;
        baritone = new AutoMineBaritone(getMineBehaviour());
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

        if (phase != TickEvent.Phase.START)
            return;

        switch (baritone.getState()) {
            case IDLE: case FAILED:

                ArrayList<BlockData<?>> targets = getHighestPriority();
                if(targets == null) {
                    if(!noMithril) {
                        LogUtils.addMessage("No mithril available, waiting");
                        noMithril = true;
                    }
                    return;
                }

                noMithril = false;
                baritone.mineFor(getHighestPriority());

                break;

        }

        checkMiningSpeedBoost();
    }


    private ArrayList<BlockData<?>> getHighestPriority() {
       for(BlockPos bp : BlockUtils.findBlockInCube(10, null, 0, 256,
               MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority1))) {
           if(BlockUtils.canMineBlock(bp))
               return MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority1);
       }
        for(BlockPos bp : BlockUtils.findBlockInCube(10, null, 0, 256,
                MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority2))) {
            if(BlockUtils.canMineBlock(bp))
                return MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority2);
        }
        for(BlockPos bp : BlockUtils.findBlockInCube(10, null, 0, 256,
                MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority3))) {
            if(BlockUtils.canMineBlock(bp))
                return MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority3);
        }
        for(BlockPos bp : BlockUtils.findBlockInCube(10, null, 0, 256,
                MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority4))) {
            if(BlockUtils.canMineBlock(bp))
                return MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority4);
        }
        return null;

    }

    @Override
    protected void onDisable() {
        if (baritone != null) baritone.disableBaritone();
        KeybindHandler.resetKeybindState();
    }

    private BaritoneConfig getMineBehaviour() {
        return new BaritoneConfig(
                MiningType.STATIC,
                MightyMiner.config.mithShiftWhenMine,
                true,
                false,
                MightyMiner.config.mithRotationTime,
                MightyMiner.config.mithRestartTimeThreshold,
                null,
                null,
                256,
                0
        );
    }
}

