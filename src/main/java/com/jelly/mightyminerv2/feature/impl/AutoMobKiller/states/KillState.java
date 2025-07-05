package com.jelly.mightyminerv2.feature.impl.AutoMobKiller.states;

import com.jelly.mightyminerv2.feature.impl.AutoMobKiller.AutoMobKiller;
import com.jelly.mightyminerv2.feature.impl.Pathfinder;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.Target;
import net.minecraft.client.Minecraft;

public class KillState implements AutoMobKillerState {

    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean hasRotated = false;

    @Override
    public void onStart(AutoMobKiller mobKiller) {
        log("Entering Kill State");
        hasRotated = false;
    }

    @Override
    public AutoMobKillerState onTick(AutoMobKiller mobKiller) {
        if (Pathfinder.getInstance().isRunning()) {
            return this;
        }

        // Initial rotation to mob
        if (!hasRotated) {
            RotationHandler.getInstance().easeTo(new RotationConfiguration(
                new Target(mobKiller.getTargetMob()),
                400L,
                null
            ));

            hasRotated = true;
            return this;
        }

        // Attack as soon as the player is able to hit the entity
        if (mc.objectMouseOver.entityHit != mobKiller.getTargetMob()) {
            if (!Pathfinder.getInstance().isRunning() && !RotationHandler.getInstance().isEnabled()) {
                return new StartingState();
            }

            return this;
        }

        mobKiller.setLastTarget(mobKiller.getTargetMob());
        KeyBindUtil.leftClick();
        RotationHandler.getInstance().stop();
        return new StartingState();
    }

    @Override
    public void onEnd(AutoMobKiller mobKiller) {
        log("Exiting Kill State");
    }

}
