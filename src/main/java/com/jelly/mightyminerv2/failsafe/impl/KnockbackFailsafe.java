package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;

public class KnockbackFailsafe extends AbstractFailsafe {

    private static final KnockbackFailsafe instance = new KnockbackFailsafe();
    private Vec3 lastPlayerPos = null;

    public static KnockbackFailsafe getInstance() {
        return instance;
    }

    public int getPriority() {
        return 8;

    }

    @Override
    public String getName() {
        return "";
    }


    public boolean check() {

        Vec3 currentPlayerPos = Minecraft.getMinecraft().thePlayer.getPositionVector();

        if (lastPlayerPos != null) {
            double deltaX = currentPlayerPos.xCoord - lastPlayerPos.xCoord;
            double deltaZ = currentPlayerPos.zCoord - lastPlayerPos.zCoord;
            double knockbackDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

            double knockbackThreshold = 0.4;

            if (knockbackDistance > knockbackThreshold) {
                return true;
            }
        }

        lastPlayerPos = currentPlayerPos;

        return false;
    }

    @Override
    public void react() {
        MacroManager.getInstance().disable();
        Logger.sendWarning("Knockback has been detected! Disabeling macro.");
    }
}
