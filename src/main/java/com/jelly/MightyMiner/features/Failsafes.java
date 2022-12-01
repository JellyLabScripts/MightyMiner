package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static com.jelly.MightyMiner.handlers.MacroHandler.macros;

public class Failsafes {
    private final Minecraft mc = Minecraft.getMinecraft();

    private void DisableMacros() {
        MobKiller.Disable();

        for (Macro macro : macros) {
            if (macro.isEnabled()) {
                macro.FailSafeDisable();
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (!MightyMiner.config.disableOnWorldChange) return;

        if (macros.stream().noneMatch(Macro::isEnabled)) return;

        LogUtils.addMessage("World changed, disabling macros");
        DisableMacros();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if(!MightyMiner.config.playerFailsafe) return;

        if (macros.stream().noneMatch(Macro::isEnabled)) return;

        if(PlayerUtils.isNearPlayer(MightyMiner.config.playerRad)){
            DisableMacros();
        }
    }
}
