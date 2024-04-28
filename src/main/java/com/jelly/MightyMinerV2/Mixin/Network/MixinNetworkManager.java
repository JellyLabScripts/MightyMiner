package com.jelly.MightyMinerV2.Mixin.Network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetAddress;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "createNetworkManagerAndConnect", at = @At("HEAD"), cancellable = true)
    private static void createNetworkManagerAndConnect(InetAddress address, int serverPort, boolean useNativeTransport, CallbackInfoReturnable<NetworkManager> callbackInfoReturnable) {

        final NetworkManager networkManager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);
        Bootstrap bootstrap = new Bootstrap();

        EventLoopGroup eventLoopGroup;

    }
}
