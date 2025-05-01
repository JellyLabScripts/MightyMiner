package com.jelly.mightyminerv2.mixin.client;

import com.jelly.mightyminerv2.macro.MacroManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow
    public EntityPlayerSP thePlayer;

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isPressed()Z", ordinal = 2))
    public boolean isPressed(KeyBinding instance) {
        return instance.isKeyDown() && !MacroManager.getInstance().isRunning();
    }

    @Redirect(method = "setIngameFocus", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;isActive()Z"))
    public boolean isActive() {
        if (MacroManager.getInstance().isRunning()) {
            return true;
        }
        return Display.isActive();
    }
}
