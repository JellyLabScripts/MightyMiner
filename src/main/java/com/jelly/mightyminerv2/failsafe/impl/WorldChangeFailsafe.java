package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.feature.impl.AutoWarp;
import com.jelly.mightyminerv2.macro.MacroManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;

public class WorldChangeFailsafe extends AbstractFailsafe {

    @Getter
    private static final WorldChangeFailsafe instance = new WorldChangeFailsafe();
    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public String getName() {
        return "WorldChangeFailsafe";
    }

    @Override
    public Failsafe getFailsafeType() {
        return Failsafe.TELEPORT;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public boolean react() {
        warn("Stopping macro due to world change.");
        MacroManager.getInstance().disable();
        return true;
    }

    @Override
    public boolean onWorldUnload(WorldEvent.Unload event) {
        if (!MacroManager.getInstance().isEnabled()) return false;
        return AutoWarp.getInstance().isDoneWarping();
    }

}
