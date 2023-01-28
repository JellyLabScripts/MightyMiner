package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.events.ReceivePacketEvent;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;

import static com.jelly.MightyMiner.handlers.MacroHandler.macros;

public class Failsafes {
    private final Minecraft mc = Minecraft.getMinecraft();

    private static final String[] teleportItems = new String[] {"Void", "Hyperion", "Aspect"};

    private void DisableMacros() {
        if (MobKiller.isToggled) {
            MightyMiner.mobKiller.Toggle();
        }

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

        for (Macro macro : macros) {
            if (macro.isEnabled()) {
                macro.toggle();
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if(!MightyMiner.config.playerFailsafe) return;

        if (macros.stream().noneMatch(Macro::isEnabled)) return;

        if(PlayerUtils.isNearPlayer(MightyMiner.config.playerRad)){
            LogUtils.addMessage("Players detected. Enabling failsafe");
            DisableMacros();
        }
    }

    @SubscribeEvent
    public void onPacket(ReceivePacketEvent event) {
        if (!MightyMiner.config.stopMacrosOnRotationCheck) return;
        if (macros.stream().noneMatch(Macro::isEnabled)) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!(event.packet instanceof S08PacketPlayerPosLook)) return;
        if (mc.thePlayer.getHeldItem() != null && Arrays.stream(teleportItems).anyMatch(i -> mc.thePlayer.getHeldItem().getDisplayName().contains(i))) return;

        DisableMacros();

        LogUtils.addMessage("You've got probably been rotation checked. Disabling macros");
        PlayerUtils.sendPingAlert();
    }
}
