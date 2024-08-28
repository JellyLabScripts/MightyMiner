package com.jelly.mightyminerv2.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor("timer")
    Timer getTimer();

    @Accessor("leftClickCounter")
    void setLeftClickCounter(int leftClickCounter);

    @Invoker("clickMouse")
    void leftClick();

    @Invoker("rightClickMouse")
    void rightClick();

    @Invoker("middleClickMouse")
    void middleClick();
}
