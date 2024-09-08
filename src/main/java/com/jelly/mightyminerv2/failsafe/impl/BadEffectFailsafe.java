package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.LogUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.Collection;

public class BadEffectFailsafe extends AbstractFailsafe {

    private static final BadEffectFailsafe instance = new BadEffectFailsafe();

    public static BadEffectFailsafe getInstance() {
        return instance;
    }

    public int getPriority() {
        return 7;

    }

    public boolean check() {
        Collection<PotionEffect> activeEffects = Minecraft.getMinecraft().thePlayer.getActivePotionEffects();

        for (PotionEffect effect : activeEffects) {
            if (isBadEffect(effect)) {
                return true;
            }
        }

        return false;
    }

    private boolean isBadEffect(PotionEffect effect) {
        int effectId = effect.getPotionID();
        return effectId == Potion.poison.id ||      // Poison
                effectId == Potion.wither.id ||      // Wither
                effectId == Potion.weakness.id ||    // Weakness
                effectId == Potion.blindness.id ||   // Blindness
                effectId == Potion.hunger.id ||      // Hunger
                effectId == Potion.moveSlowdown.id ||// Slowness
                effectId == Potion.digSlowdown.id;   // Mining Fatigue
    }

    @Override
    public void react() {
        MacroManager.getInstance().disable();
        LogUtil.warn("Bad effect has been detected! Disabeling macro.");
    }
}
