package com.jelly.mightyminerv2.Mixin.Client;

import com.jelly.mightyminerv2.Event.BlockChangeEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Chunk.class)
public class MixinChunk {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    public void setBlockState(BlockPos pos, IBlockState state, CallbackInfoReturnable<IBlockState> cir) {
        final IBlockState old = cir.getReturnValue();
        if (old == null || state == old) return;
        MinecraftForge.EVENT_BUS.post(new BlockChangeEvent(pos, old, state));
    }
}