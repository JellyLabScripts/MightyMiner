package com.jelly.mightyminerv2.mixin.client;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.failsafe.FailsafeManager;
import com.jelly.mightyminerv2.macro.MacroManager;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundPoolEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public class MixinSoundManager {
  @Inject(method = "getNormalizedVolume", at = @At("RETURN"), cancellable = true)
  private void getNormalizedVolume(ISound sound, SoundPoolEntry entry, SoundCategory category, CallbackInfoReturnable<Float> cir) {
    if (MacroManager.getInstance().isRunning() && MightyMinerConfig.muteGame && FailsafeManager.getInstance().emergencyQueue.isEmpty() && !FailsafeManager.getInstance().triggeredFailsafe.isPresent()) {
      cir.setReturnValue(0f);
    }
  }
}
