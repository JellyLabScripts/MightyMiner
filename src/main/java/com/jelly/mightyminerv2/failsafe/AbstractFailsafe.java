package com.jelly.mightyminerv2.failsafe;

import com.jelly.mightyminerv2.event.BlockChangeEvent;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.util.Logger;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public abstract class AbstractFailsafe {

    protected final Minecraft mc = Minecraft.getMinecraft();

    public abstract int getPriority();

    public abstract String getName();

    public abstract Failsafe getFailsafeType();

    public boolean onBlockChange(BlockChangeEvent event) {
        return false;
    }

    public boolean onPacketReceive(PacketEvent.Received event) {
        return false;
    }

    public boolean onTick(TickEvent.ClientTickEvent event) {
        return false;
    }

    public boolean onChat(ClientChatReceivedEvent event) {
        return false;
    }

    public boolean onWorldUnload(WorldEvent.Unload event) {
        return false;
    }

    public boolean onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        return false;
    }

    public abstract boolean react();

    public void resetStates() {}

    protected void log(String message) {
        Logger.sendLog(formatMessage(message));
    }

    protected void send(String message) {
        Logger.sendMessage(formatMessage(message));
    }

    protected void error(String message) {
        Logger.sendError(formatMessage(message));
    }

    protected void warn(String message) {
        Logger.sendWarning(formatMessage(message));
    }

    protected void note(String message) {
        Logger.sendNote(formatMessage(message));
    }

    protected String formatMessage(String message) {
        return "[" + getName() + "] " + message;
    }

    public enum Failsafe {
        BAD_EFFECTS,
        BLOCK_CHANGE,
        DISCONNECT,
        ITEM_CHANGE,
        KNOCKBACK,
        ROTATION,
        TELEPORT,
        BEDROCK_CHECK,
        SLOT_CHANGE
    }
}