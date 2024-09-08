package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EvacuateFailsafe extends AbstractFailsafe {

    private static final EvacuateFailsafe instance = new EvacuateFailsafe();
    private EvacuateState evacuateState = EvacuateState.NONE;

    public static EvacuateFailsafe getInstance() {
        return instance;
    }

    public int getPriority() {
        return 10;

    }


    public boolean check() {
        return evacuateState != EvacuateState.NONE;
    }

    @Override
    public void react() {
        switch (evacuateState) {
            case NONE:
                MacroManager.getInstance().pause();
                evacuateState = EvacuateState.EVACUATE;
                break;
            case EVACUATE:
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/evacuate");
                evacuateState = EvacuateState.TP_BACK;
                break;
            case TP_BACK:
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/skyblock");
                evacuateState = EvacuateState.END;
                break;
            case END:
                MacroManager.getInstance().resume();
                resetStates();
                break;
        }
    }


    public void onTickDetection(TickEvent.ClientTickEvent event) {
        if (!MacroManager.getInstance().isRunning()) return;
        if (Minecraft.getMinecraft().getCurrentServerData() != null && isServerRebooting()) {
            evacuateState = EvacuateState.EVACUATE;
        }
    }


    public void onChatDetection(ClientChatReceivedEvent event) {
        String msg = event.message.getUnformattedText();
        if (msg.startsWith("You can't use this when the server is about to")) {
            evacuateState = EvacuateState.EVACUATE;
        }
    }

    @Override
    public void resetStates() {
        evacuateState = EvacuateState.NONE;
    }

    private boolean isServerRebooting() {
        return false;
    }

    enum EvacuateState {
        NONE,       // No evacuation needed
        EVACUATE,   // Evacuation initiated
        TP_BACK,    // Returning to the main hub/island
        END         // Evacuation complete
    }
}
