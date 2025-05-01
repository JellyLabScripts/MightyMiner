package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.Angle;
import com.jelly.mightyminerv2.util.helper.Clock;
import lombok.Getter;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class RotationFailsafe extends AbstractFailsafe {

    @Getter
    private static final RotationFailsafe instance = new RotationFailsafe();

    public int getPriority() {
        return 5;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Failsafe getFailsafeType() {
        return Failsafe.ROTATION;
    }


    private final Clock triggerCheck = new Clock();
    private Angle rotationBeforeReacting = null;

    @Override
    public boolean onPacketReceive(PacketEvent.Received event) {
        if (!MacroManager.getInstance().isEnabled()) return false;

        if (event.packet instanceof S08PacketPlayerPosLook) {
            S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook) event.packet;
            double packetYaw = packet.getYaw();
            double packetPitch = packet.getPitch();
            double playerYaw = mc.thePlayer.rotationYaw;
            double playerPitch = mc.thePlayer.rotationPitch;

            float yawDifference = Math.abs((mc.thePlayer.rotationYaw - ((S08PacketPlayerPosLook) event.packet).getYaw()));
            float pitchDifference = Math.abs((mc.thePlayer.rotationPitch - ((S08PacketPlayerPosLook) event.packet).getPitch()));

            if (yawDifference == 360F && pitchDifference == 0F) return false;
                
            if (shouldTriggerCheck(packetYaw, packetPitch)) {
                if (rotationBeforeReacting == null) rotationBeforeReacting = new Angle((float) playerYaw, (float) playerPitch);
            }

            triggerCheck.schedule(500);
        }

        return false;
    }

    @Override
    public boolean onTick(TickEvent.ClientTickEvent event) {
        if (!MacroManager.getInstance().isEnabled()) {
            rotationBeforeReacting = null;
            return false;
        }

        if (triggerCheck.passed() && triggerCheck.isScheduled()) {
            if (rotationBeforeReacting == null) return false;

            if (shouldTriggerCheck(rotationBeforeReacting.getYaw(), rotationBeforeReacting.getPitch())) {
                return true;
            }

            rotationBeforeReacting = null;
            triggerCheck.reset();
        }

        return false;
    }

    private boolean shouldTriggerCheck(double newYaw, double newPitch) {
        double yawDiff = Math.abs(newYaw - mc.thePlayer.rotationYaw) % 360;
        double pitchDiff = Math.abs(newPitch - mc.thePlayer.rotationPitch) % 360;
        return yawDiff >= 10 || pitchDiff >= 10;
    }

    @Override
    public boolean react() {
        MacroManager.getInstance().disable();
        Logger.sendWarning("You`ve got rotated! Disabeling macro.");
        return true;
    }

}
