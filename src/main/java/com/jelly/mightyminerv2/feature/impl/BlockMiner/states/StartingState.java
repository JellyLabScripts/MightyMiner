package com.jelly.mightyminerv2.feature.impl.BlockMiner.states;

import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.config.MightyMinerConfig;

/**
 * StartingState
 * 
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
        return MightyMinerConfig.usePickaxeAbility && miner.getPickaxeAbilityState() == BlockMiner.PickaxeAbilityState.AVAILABLE?
                new ApplyAbilityState() : new ChoosingBlockState();
    }

    @Override
    public void onEnd(BlockMiner miner) {
        log("Exiting Starting State");
    }
}
