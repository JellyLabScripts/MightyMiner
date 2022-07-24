package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.baritone.Baritone;
import com.jelly.MightyMiner.baritone.baritones.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.baritones.WalkBaritone;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.LogUtils;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.util.BlockPos;

import java.util.concurrent.ExecutionException;

public class GemstoneMacro extends Macro {
    Baritone baritone = new AutoMineBaritone();
    @Override
    public void onEnable() {
        baritone.enableBaritone(new BlockPos(17, 53, -15));
    }

    @Override
    public void onDisable() {
        baritone.disableBaritone();
    }

    @Override
    public void onTick() {
        baritone.onTickEvent();
    }

    @Override
    public void onLastRender() {
        baritone.onRenderEvent();
    }
}
