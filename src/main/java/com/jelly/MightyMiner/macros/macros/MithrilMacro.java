package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.MineBehaviour;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.macros.Macro;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class MithrilMacro extends Macro {

    AutoMineBaritone baritone;

    @Override
    protected void onEnable() {
        baritone = new AutoMineBaritone(getMineBehaviour());
    }

    @Override
    public void onTick(TickEvent.Phase phase) {
        baritone.onTickEvent(phase);
        if(phase != TickEvent.Phase.START)
            return;

        if(MightyMiner.config.mithShiftWhenMine)
            KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);

        if(!baritone.isEnabled()){
            baritone.enableBaritone(Blocks.prismarine, Blocks.wool, Blocks.stained_hardened_clay);
        }

    }

    @Override
    public void onLastRender() {
        baritone.onRenderEvent();
    }

    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event) {
        baritone.onOverlayRenderEvent(event);
    }



    @Override
    protected void onDisable() {
        KeybindHandler.resetKeybindState();
        baritone.disableBaritone();
    }


    private MineBehaviour getMineBehaviour(){
        return new MineBehaviour(
                AutoMineType.STATIC,
                MightyMiner.config.mithShiftWhenMine,
                MightyMiner.config.mithRotationTime,
                MightyMiner.config.mithRestartTimeThreshold,
                null,
                null,
               256,
                0
        );
    }
}
