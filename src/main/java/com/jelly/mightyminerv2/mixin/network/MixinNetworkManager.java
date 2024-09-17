package com.jelly.mightyminerv2.mixin.network;

import com.jelly.mightyminerv2.event.PacketEvent;
import io.netty.channel.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
  @Inject(method = "channelRead0*", at = @At("HEAD"))
  private void read(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
    if (packet.getClass().getSimpleName().startsWith("S")) {
      MinecraftForge.EVENT_BUS.post(new PacketEvent.Received(packet));
    } else if (packet.getClass().getSimpleName().startsWith("C")) {
      MinecraftForge.EVENT_BUS.post(new PacketEvent.Sent(packet));
    }
  }
}
