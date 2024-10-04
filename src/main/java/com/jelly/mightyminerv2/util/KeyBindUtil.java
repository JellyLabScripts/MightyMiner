package com.jelly.mightyminerv2.util;

import com.google.common.collect.ImmutableMap;
import com.jelly.mightyminerv2.mixin.client.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyBindUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final KeyBinding[] allKeys = {
            mc.gameSettings.keyBindAttack,
            mc.gameSettings.keyBindUseItem,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindJump,
            mc.gameSettings.keyBindSneak,
            mc.gameSettings.keyBindSprint,
    };

    public static final KeyBinding[] allKeys2 = {
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindJump,
    };

    public static void rightClick() {
        ((MinecraftAccessor) mc).rightClick();
    }

    public static void leftClick() {
        ((MinecraftAccessor) mc).leftClick();
    }

    public static void middleClick() {
        ((MinecraftAccessor) mc).middleClick();
    }

    public static void onTick(KeyBinding key) {
        if (mc.currentScreen == null) {
            KeyBinding.onTick(key.getKeyCode());
        }
    }

    public static int getRightClickDelayTimer(){
        return ((MinecraftAccessor) mc).getRightClickDelayTimer();
    }

    public static void resetRightClickDelayTimer(){
        ((MinecraftAccessor) mc).setRightClickDelayTimer(0);
    }

    public static void setKeyBindState(KeyBinding key, boolean pressed) {
        if (pressed) {
            if (mc.currentScreen != null && key != null) {
                Logger.sendNote("Releasing Key. currentScreen isnt null and key isnt null");
                realSetKeyBindState(key, false);
                return;
            }
        }
        realSetKeyBindState(key, pressed);
    }

    private static void realSetKeyBindState(KeyBinding key, boolean pressed) {
        if (key == null) return;
        if (pressed) {
            if (!key.isKeyDown()) {
                KeyBinding.onTick(key.getKeyCode());
                KeyBinding.setKeyBindState(key.getKeyCode(), true);
            }
        } else {
            if (key.isKeyDown()) {
                KeyBinding.setKeyBindState(key.getKeyCode(), false);
            }
        }
    }

    public static void stopMovement() {
        stopMovement(false);
    }

    public static void stopMovement(boolean ignoreAttack) {
        realSetKeyBindState(mc.gameSettings.keyBindForward, false);
        realSetKeyBindState(mc.gameSettings.keyBindBack, false);
        realSetKeyBindState(mc.gameSettings.keyBindRight, false);
        realSetKeyBindState(mc.gameSettings.keyBindLeft, false);
        if (!ignoreAttack) {
            realSetKeyBindState(mc.gameSettings.keyBindAttack, false);
            realSetKeyBindState(mc.gameSettings.keyBindUseItem, false);
        }
        realSetKeyBindState(mc.gameSettings.keyBindSneak, false);
        realSetKeyBindState(mc.gameSettings.keyBindJump, false);
        realSetKeyBindState(mc.gameSettings.keyBindSprint, false);
    }

    public static void holdThese(boolean withAttack, KeyBinding... keyBinding) {
        releaseAllExcept(keyBinding);
        for (KeyBinding key : keyBinding) {
            if (key != null)
                realSetKeyBindState(key, true);
        }
        if (withAttack) {
            realSetKeyBindState(mc.gameSettings.keyBindAttack, true);
        }
    }

    public static void holdThese(KeyBinding... keyBinding) {
        releaseAllExcept(keyBinding);
        for (KeyBinding key : keyBinding) {
            if (key != null)
                realSetKeyBindState(key, true);
        }
    }

    public static void releaseAllExcept(KeyBinding... keyBinding) {
        for (KeyBinding key : allKeys) {
            if (key != null && !contains(keyBinding, key) && key.isKeyDown()) {
                realSetKeyBindState(key, false);
            }
        }
    }

    public static boolean contains(KeyBinding[] keyBinding, KeyBinding key) {
        for (KeyBinding keyBind : keyBinding) {
            if (keyBind != null && keyBind.getKeyCode() == key.getKeyCode())
                return true;
        }
        return false;
    }

    public static boolean areAllKeybindsReleased() {
        for (KeyBinding key : allKeys2) {
            if (key != null && key.isKeyDown())
                return false;
        }
        return true;
    }

    public static KeyBinding[] getHeldKeybinds() {
        KeyBinding[] keybinds = new KeyBinding[allKeys.length];
        int i = 0;
        for (KeyBinding key : allKeys) {
            if (key != null && key.isKeyDown()) {
                keybinds[i] = key;
                i++;
            }
        }
        return keybinds;
    }

    public static final Map<KeyBinding, Integer> keyBindMap = ImmutableMap.of(
            mc.gameSettings.keyBindForward, 0,
            mc.gameSettings.keyBindLeft, 90,
            mc.gameSettings.keyBindBack, 180,
            mc.gameSettings.keyBindRight, -90
    );

    public static List<KeyBinding> getNeededKeyPresses(Vec3 orig, Vec3 dest) {
        List<KeyBinding> keys = new ArrayList<>();

        double[] delta = {orig.xCoord - dest.xCoord, orig.zCoord - dest.zCoord};
        float requiredAngle = (float) (MathHelper.atan2(delta[0], -delta[1]) * (180.0 / Math.PI));

        float angleDifference = AngleUtil.normalizeAngle(requiredAngle - mc.thePlayer.rotationYaw) * -1;

        keyBindMap.forEach((key, yaw) -> {
            if (Math.abs(yaw - angleDifference) < 67.5 || Math.abs(yaw - (angleDifference + 360.0)) < 67.5) {
                keys.add(key);
            }
        });
        return keys;
    }

    public static List<KeyBinding> getNeededKeyPresses(float neededYaw) {
        List<KeyBinding> keys = new ArrayList<>();
        neededYaw = AngleUtil.normalizeAngle(neededYaw - mc.thePlayer.rotationYaw) * -1;
        float finalNeededYaw = neededYaw;
        keyBindMap.forEach((key, yaw) -> {
            if (Math.abs(yaw - finalNeededYaw) < 67.5 || Math.abs(yaw - (finalNeededYaw + 360.0)) < 67.5) {
                keys.add(key);
            }
        });
        return keys;
    }

    public static List<KeyBinding> getOppositeKeys(List<KeyBinding> kbs) {
        List<KeyBinding> keys = new ArrayList<>();
        kbs.forEach(key -> {
            switch (key.getKeyCode()) {
                case 17:
                    keys.add(mc.gameSettings.keyBindBack);
                    break;
                case 30:
                    keys.add(mc.gameSettings.keyBindRight);
                    break;
                case 31:
                    keys.add(mc.gameSettings.keyBindLeft);
                    break;
                case 32:
                    keys.add(mc.gameSettings.keyBindForward);
                    break;
            }
        });
        return keys;
    }

    public static List<KeyBinding> getKeyPressesToDecelerate(Vec3 orig, Vec3 dest) {
        return getOppositeKeys(getNeededKeyPresses(orig, dest));
    }
}
