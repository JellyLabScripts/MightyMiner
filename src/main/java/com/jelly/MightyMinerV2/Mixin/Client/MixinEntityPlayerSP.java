package com.jelly.MightyMinerV2.Mixin.Client;

import com.jelly.MightyMinerV2.Event.MotionUpdateEvent;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityPlayerSP.class, priority = Integer.MAX_VALUE)
public class MixinEntityPlayerSP extends AbstractClientPlayer {

    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    public void onUpdateWalkingPlayerPRE(CallbackInfo ci){
        MinecraftForge.EVENT_BUS.post(new MotionUpdateEvent.Pre(this.rotationYaw, this.rotationPitch));
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    public void onUpdateWalkingPlayerPOST(CallbackInfo ci){
        MinecraftForge.EVENT_BUS.post(new MotionUpdateEvent.Post(this.rotationYaw, this.rotationPitch));
    }
}
