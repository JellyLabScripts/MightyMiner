package com.jelly.mightyminerv2.feature.impl.BlockMiner.states;

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
        boolean hasAbility = miner.getPickaxeAbility() != BlockMiner.PickaxeAbility.NONE;
        boolean isCooledDown = System.currentTimeMillis() - miner.getLastAbilityUse() > 60000;
        boolean isAvailable = miner.getPickaxeAbilityState() == BlockMiner.PickaxeAbilityState.AVAILABLE;

        return hasAbility && (isCooledDown || isAvailable);
    }

    @Override
    public void onEnd(BlockMiner miner) {
        log("Exiting Starting State");
    }

}
