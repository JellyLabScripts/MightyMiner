package com.jelly.MightyMiner.mixins.network;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.handlers.MacroHandler;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.*;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.crypto.Mac;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {


    @Inject(method = "handlePlayerListHeaderFooter", at = @At("HEAD"))
    public void handlePlayerListHeaderFooter(S47PacketPlayerListHeaderFooter packetIn, CallbackInfo ci) {
        MacroHandler.gameState.header = packetIn.getHeader().getFormattedText().length() == 0 ? null : packetIn.getHeader();
        MacroHandler.gameState.footer = packetIn.getFooter().getFormattedText().length() == 0 ? null : packetIn.getFooter();
        MacroHandler.gameState.update();

    }

}
