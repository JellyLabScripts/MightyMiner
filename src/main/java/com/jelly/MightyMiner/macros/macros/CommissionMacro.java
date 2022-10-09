package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.autowalk.WalkBaritone;
import com.jelly.MightyMiner.baritone.autowalk.config.AutowalkConfig;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class CommissionMacro extends Macro {
    WalkBaritone baritone = new WalkBaritone(getAutowalkConfig());

    @Override
    protected void onEnable() {
         baritone.onEnable(new BlockPos(74, 56, -27));
       // LogUtils.addMessage(BlockUtils.getRelativeBlock(0, 0, 1).toString());
    }

    @Override
    public void onTick(TickEvent.Phase phase) {
        baritone.onTickEvent(phase);
    }

    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event) {
        baritone.onOverlayRenderEvent(event);
    }

    @Override
    public void onLastRender() {
        baritone.onLastRender();
    }

    @Override
    protected void onDisable() {
        baritone.disableBaritone();

    }


    private AutowalkConfig getAutowalkConfig(){
        return new AutowalkConfig(
                MightyMiner.config.comBarSafeIndex,
                MightyMiner.config.comBarRotationTime);
    }
}
