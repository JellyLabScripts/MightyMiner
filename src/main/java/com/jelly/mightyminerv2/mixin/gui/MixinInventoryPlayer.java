package com.jelly.mightyminerv2.mixin.gui;

import com.jelly.mightyminerv2.macro.MacroManager;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer {

  @Inject(method = "changeCurrentItem", at = @At("HEAD"), cancellable = true)
  public void changeCurrentItem(int direction, CallbackInfo ci) {
    if (MacroManager.getInstance().isRunning()) {
      ci.cancel();
    }
  }
}
