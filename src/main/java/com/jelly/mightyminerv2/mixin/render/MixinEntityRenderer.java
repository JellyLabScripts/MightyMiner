package com.jelly.mightyminerv2.mixin.render;

import com.jelly.mightyminerv2.macro.MacroManager;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Redirect(method = "updateCameraAndRender", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;pauseOnLostFocus:Z"))
    private boolean redirectPauseOnLostFocus(GameSettings gameSettings) {
        return gameSettings.pauseOnLostFocus && !MacroManager.getInstance().isRunning();
    }

}
