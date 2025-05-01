package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.Logger;
import lombok.Getter;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class KnockbackFailsafe extends AbstractFailsafe {

    @Getter
    private static final KnockbackFailsafe instance = new KnockbackFailsafe();

    public int getPriority() {
        return 8;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Failsafe getFailsafeType() {
        return Failsafe.KNOCKBACK;
    }


    @Override
    public boolean onPacketReceive(PacketEvent.Received event) {
        if (!(event.packet instanceof S12PacketEntityVelocity)) return false;
        if (((S12PacketEntityVelocity) event.packet).getEntityID() != mc.thePlayer.getEntityId()) return false;
        return ((S12PacketEntityVelocity) event.packet).getMotionY() >= 4000;
    }

    @Override
    public boolean react() {
        MacroManager.getInstance().disable();
        Logger.sendWarning("Knockback has been detected! Disabling macro.");
        return true;
    }
}
