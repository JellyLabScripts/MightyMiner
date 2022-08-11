package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.baritone.autowalk.WalkBaritone;
import com.jelly.MightyMiner.macros.Macro;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class CommissionMacro extends Macro {
    WalkBaritone baritone = new WalkBaritone();

    @Override
    protected void onEnable() {
        baritone.onEnable(new BlockPos(2, 64, 2));
    }

    @Override
    public void onTick(TickEvent.Phase phase) {
        baritone.onTickEvent(phase);
    }

    @Override
    public void onLastRender() {
        baritone.onRenderEvent();
    }

    @Override
    protected void onDisable() {
        baritone.disableBaritone();

    }
}
