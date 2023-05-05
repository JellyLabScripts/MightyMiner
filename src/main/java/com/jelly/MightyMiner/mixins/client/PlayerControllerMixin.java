package com.jelly.MightyMiner.mixins.client;

import cc.polyfrost.oneconfig.libs.checker.units.qual.A;
import com.jelly.MightyMiner.utils.HypixelUtils.FastMineUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
abstract class PlayerControllerMixin {

    @Shadow @Final private Minecraft mc;

    @Shadow private float curBlockDamageMP;

    @Shadow private float stepSoundTickCounter;

    @Shadow private int blockHitDelay;

    @Inject(method = "onPlayerDamageBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;sendBlockBreakProgress(ILnet/minecraft/util/BlockPos;I)V"))
    private void preBreakBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> cir){
        // Copied from https://github.com/FloppaCoding/FloppaClient
        if (FastMineUtils.shouldPreBreakBlock(this.stepSoundTickCounter, this.curBlockDamageMP)) {
            this.mc.theWorld.setBlockToAir(posBlock);
            // The following is probably not required.
            this.curBlockDamageMP = 0.0F;
            this.stepSoundTickCounter = 0f;

            this.blockHitDelay = 5;
        }
    }
}
