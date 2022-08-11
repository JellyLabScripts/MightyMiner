package com.jelly.MightyMiner.handlers;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.config.Config;
import com.jelly.MightyMiner.utils.BlockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeybindHandler {
    static Minecraft mc = Minecraft.getMinecraft();
    static int setmode = 0;
    public static int keybindA = mc.gameSettings.keyBindLeft.getKeyCode();
    public static int keybindD = mc.gameSettings.keyBindRight.getKeyCode();
    public static int keybindW = mc.gameSettings.keyBindForward.getKeyCode();
    public static int keybindS = mc.gameSettings.keyBindBack.getKeyCode();
    public static int keybindAttack = mc.gameSettings.keyBindAttack.getKeyCode();
    public static int keybindUseItem = mc.gameSettings.keyBindUseItem.getKeyCode();
    public static int keyBindSpace = mc.gameSettings.keyBindJump.getKeyCode();
    public static int keyBindShift = mc.gameSettings.keyBindSneak.getKeyCode();
    public static int keyBindJump = mc.gameSettings.keyBindJump.getKeyCode();



    static KeyBinding[] macroKeybinds = new KeyBinding[4];

    public static void initializeCustomKeybindings() {
        macroKeybinds[0] = new KeyBinding("Start macro", Keyboard.KEY_F, "MightyMiner");
        macroKeybinds[1] = new KeyBinding("Disable macro", Keyboard.KEY_Z, "MightyMiner");
        macroKeybinds[2] = new KeyBinding("Debug", Keyboard.KEY_H, "MightyMiner");
        macroKeybinds[3] = new KeyBinding("Open GUI", Keyboard.KEY_RSHIFT, "MightyMiner");
        for (KeyBinding customKeyBind : macroKeybinds) {
            ClientRegistry.registerKeyBinding(customKeyBind);
        }
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {

        if(macroKeybinds[0].isKeyDown()){
            MacroHandler.startScript(MightyMiner.config.macroType);
        }
        if(macroKeybinds[1].isKeyDown()){
            MacroHandler.disableScript();
        }
        if(macroKeybinds[2].isKeyDown()){
            System.out.println(BlockUtils.canSeeBlock(new BlockPos(2, 250, 2)));
        }
        if(macroKeybinds[3].isKeyDown()){
            mc.displayGuiScreen(MightyMiner.config.gui());
        }

    }

    public static void setKeyBindState(int keyCode, boolean pressed) {
        if (pressed) {
            if (mc.currentScreen != null) {
                KeyBinding.setKeyBindState(keyCode, false);
                return;
            }
        }
        KeyBinding.setKeyBindState(keyCode, pressed);
    }

    public static void updateKeys(boolean wBool, boolean sBool, boolean aBool, boolean dBool, boolean atkBool, boolean useBool, boolean shiftBool) {
        if (mc.currentScreen != null) {
            resetKeybindState();
            return;
        }
        KeyBinding.setKeyBindState(keybindW, wBool);
        KeyBinding.setKeyBindState(keybindS, sBool);
        KeyBinding.setKeyBindState(keybindA, aBool);
        KeyBinding.setKeyBindState(keybindD, dBool);
        KeyBinding.setKeyBindState(keybindAttack, atkBool);
        KeyBinding.setKeyBindState(keybindUseItem, useBool);
        KeyBinding.setKeyBindState(keyBindShift, shiftBool);
    }

    public static void updateKeys(boolean wBool, boolean sBool, boolean aBool, boolean dBool, boolean atkBool, boolean useBool, boolean shiftBool, boolean jumpBool) {
        if (mc.currentScreen != null) {
            resetKeybindState();
            return;
        }
        KeyBinding.setKeyBindState(keybindW, wBool);
        KeyBinding.setKeyBindState(keybindS, sBool);
        KeyBinding.setKeyBindState(keybindA, aBool);
        KeyBinding.setKeyBindState(keybindD, dBool);
        KeyBinding.setKeyBindState(keybindAttack, atkBool);
        KeyBinding.setKeyBindState(keybindUseItem, useBool);
        KeyBinding.setKeyBindState(keyBindShift, shiftBool);
        KeyBinding.setKeyBindState(keyBindJump, jumpBool);
    }

    public static void updateKeys(boolean wBool, boolean sBool, boolean aBool, boolean dBool, boolean atkBool) {
        if (mc.currentScreen != null) {
            resetKeybindState();
            return;
        }
        KeyBinding.setKeyBindState(keybindW, wBool);
        KeyBinding.setKeyBindState(keybindS, sBool);
        KeyBinding.setKeyBindState(keybindA, aBool);
        KeyBinding.setKeyBindState(keybindD, dBool);
        KeyBinding.setKeyBindState(keybindAttack, atkBool);
    }

    public static void resetKeybindState() {
        KeyBinding.setKeyBindState(keybindA, false);
        KeyBinding.setKeyBindState(keybindS, false);
        KeyBinding.setKeyBindState(keybindW, false);
        KeyBinding.setKeyBindState(keybindD, false);
        KeyBinding.setKeyBindState(keyBindShift, false);
        KeyBinding.setKeyBindState(keyBindJump, false);
        KeyBinding.setKeyBindState(keybindAttack, false);
        KeyBinding.setKeyBindState(keybindUseItem, false);
    }
}
