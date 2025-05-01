package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.feature.impl.LagDetector;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.Clock;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class TeleportFailsafe extends AbstractFailsafe {

    @Getter
    private static final TeleportFailsafe instance = new TeleportFailsafe();
    private final LagDetector lagDetector = LagDetector.getInstance();
    private final Minecraft mc = Minecraft.getMinecraft();

    private Vec3 originalPosition = null;
    private final Clock triggerCheck = new Clock();
    private boolean potentialTeleportDetected = false;

    @Override
    public String getName() {
        return "TeleportFailsafe";
    }

    @Override
    public Failsafe getFailsafeType() {
        return Failsafe.TELEPORT;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public boolean react() {
        Logger.sendWarning("You have been teleported");
        MacroManager.getInstance().disable();
        reset();
        return true;
    }

    @Override
    public boolean onPacketReceive(PacketEvent.Received event) {
        if (!(event.packet instanceof S08PacketPlayerPosLook)) {
            return false;
        }

        S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook) event.packet;
        Vec3 currentPlayerPos = mc.thePlayer.getPositionVector();
        Vec3 packetPlayerPos = new Vec3(
                packet.getX() + (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.X) ? currentPlayerPos.xCoord : 0),
                packet.getY() + (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y) ? currentPlayerPos.yCoord : 0),
                packet.getZ() + (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Z) ? currentPlayerPos.zCoord : 0)
        );

        double distance = currentPlayerPos.distanceTo(packetPlayerPos);

        if (distance >= 1) {
            final double lastReceivedPacketDistance = currentPlayerPos.distanceTo(lagDetector.getLastPacketPosition());
            final double playerMovementSpeed = mc.thePlayer.getAttributeMap().getAttributeInstanceByName("generic.movementSpeed").getAttributeValue();
            final int ticksSinceLastPacket = (int) Math.ceil(lagDetector.getTimeSinceLastTick() / 50D);
            final double estimatedMovement = playerMovementSpeed * ticksSinceLastPacket;

            if (lastReceivedPacketDistance > 7.5D && Math.abs(lastReceivedPacketDistance - estimatedMovement) < 2) {
                return false;
            }

            if (originalPosition == null) {
                originalPosition = currentPlayerPos;
            }

            potentialTeleportDetected = true;
            triggerCheck.schedule(500);
            return false;
        }

        return false;
    }

    @Override
    public boolean onTick(ClientTickEvent event) {
        if (event.phase != ClientTickEvent.Phase.END) {
            return false;
        }

        if (potentialTeleportDetected && triggerCheck.isScheduled() && triggerCheck.passed()) {
            triggerCheck.reset();
            potentialTeleportDetected = false;

            Vec3 currentPosition = mc.thePlayer.getPositionVector();
            if (originalPosition != null) {
                double totalDisplacement = currentPosition.distanceTo(originalPosition);
                originalPosition = null;
                return totalDisplacement > 1;
            }
        }

        return false;
    }

    public void reset() {
        originalPosition = null;
        potentialTeleportDetected = false;
        triggerCheck.reset();
    }

}