package com.jelly.mightyminerv2.feature.impl.BlockMiner.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;

/**
 * StartingState
 * <p>
 * Initial state in the BlockMiner state machine.
 * Acts as an entry point and determines the next appropriate state.
 */
public class StartingState implements BlockMinerState {

    @Override
    public void onStart(BlockMiner miner) {
        log("Entering Starting State");
    }

    @Override
    public BlockMinerState onTick(BlockMiner miner) {
        return canUsePickaxeAbility(miner) ? new ApplyAbilityState() : new ChoosingBlockState();
    }

    private boolean canUsePickaxeAbility(BlockMiner miner) {
        if (System.currentTimeMillis() - miner.getLastAbilityUse() > 120000) {
            miner.setPickaxeAbilityState(BlockMiner.PickaxeAbilityState.AVAILABLE);
        }

        boolean hasAbility = miner.getPickaxeAbility() != BlockMiner.PickaxeAbility.NONE;
        boolean isAvailable = miner.getPickaxeAbilityState() == BlockMiner.PickaxeAbilityState.AVAILABLE;

        return MightyMinerConfig.usePickaxeAbility && hasAbility && isAvailable;
    }

    @Override
    public void onEnd(BlockMiner miner) {
        log("Exiting Starting State");
    }

}
