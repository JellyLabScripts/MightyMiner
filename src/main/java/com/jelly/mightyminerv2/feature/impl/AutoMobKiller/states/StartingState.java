package com.jelly.mightyminerv2.feature.impl.AutoMobKiller.states;

import com.jelly.mightyminerv2.feature.impl.AutoMobKiller.AutoMobKiller;
import net.minecraft.entity.EntityLivingBase;

public class StartingState implements AutoMobKillerState {

    @Override
    public void onStart(AutoMobKiller mobKiller) {
        log("Entering Starting State");
    }

    @Override
    public AutoMobKillerState onTick(AutoMobKiller mobKiller) {
        EntityLivingBase targetMob = mobKiller.getTargetMob();

        if (targetMob != null)
            mobKiller.getMobQueue().add(targetMob);

        mobKiller.getRecheckTimer().reset();
        return new FindMobState();
    }

    @Override
    public void onEnd(AutoMobKiller mobKiller) {
        log("Exiting Starting State");
    }

}
