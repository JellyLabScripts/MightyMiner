package com.jelly.MightyMiner.baritone.autowalk.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

// inspired by cabaletta/baritone
public class InputHandler {

    private static final Map<Input, Boolean> inputForceStateMap = new HashMap<>();

    public static boolean isInputForcedDown(Input input) {
        return input != null && inputForceStateMap.getOrDefault(input, false);
    }
    public static void setInputForceState(Input input, boolean forced) {
        inputForceStateMap.put(input, forced);
    }

    public static void clearAllKeys() {
        inputForceStateMap.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {

        if(Minecraft.getMinecraft().thePlayer == null ||Minecraft.getMinecraft().theWorld == null ||  event.phase != TickEvent.Phase.START)
            return;

        if (inControl()) {
            if (Minecraft.getMinecraft().thePlayer.movementInput.getClass() != BaritoneMovement.class) {
                Minecraft.getMinecraft().thePlayer.movementInput = new BaritoneMovement();
            }
        } else {
            if (Minecraft.getMinecraft().thePlayer.movementInput.getClass() != MovementInputFromOptions.class) {
                Minecraft.getMinecraft().thePlayer.movementInput = new MovementInputFromOptions(Minecraft.getMinecraft().gameSettings);
            }
        }
    }

    private boolean inControl() {
        for (Input input : new Input[]{Input.MOVE_FORWARD, Input.MOVE_BACK, Input.MOVE_LEFT, Input.MOVE_RIGHT, Input.SNEAK}) {
            if (isInputForcedDown(input)) {
                return true;
            }
        }
        return false;
    }

}
