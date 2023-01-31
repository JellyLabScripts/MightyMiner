package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.utils.InventoryUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.world.GameState;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class Autosell {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean enabled;
    private static int originalSlot;

    private static final List<String> itemsToBeSold = new ArrayList<>();

    public static boolean isEnabled() {
        return enabled;
    }

    public static void enable() {

        if (MacroHandler.gameState.cookie == GameState.EffectState.OFF) {
            LogUtils.debugLog("[AutoSell] You need a cookie for auto sell!");
            MightyMiner.config.powAutosell = false;
            disable();
            return;
        }
        addItemsToBeSold();

        LogUtils.debugLog("[AutoSell] Started inventory sell");
        originalSlot = mc.thePlayer.inventory.currentItem;
        enabled = true;
    }

    public static void disable() {
        LogUtils.debugLog("[AutoSell] Finished auto sell");
        mc.thePlayer.closeScreen();
        mc.thePlayer.inventory.currentItem = originalSlot;
        enabled = false;
    }

    @SubscribeEvent
    public final void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END || mc.thePlayer == null || mc.theWorld == null)
            return;


        if (!enabled) return;

        if (mc.currentScreen == null) {
            LogUtils.debugLog("[AutoSell] Opening SB menu");
            mc.thePlayer.inventory.currentItem = 8;
            KeybindHandler.rightClick();
        } else if (InventoryUtils.getInventoryName() != null && InventoryUtils.getInventoryName().contains("SkyBlock Menu")) {
            LogUtils.debugLog("[AutoSell] Detected SB menu, opening trade menu");
            final ItemStack emerald = InventoryUtils.getStackInOpenContainerSlot(22);
            if (emerald != null) {
                LogUtils.debugLog("[AutoSell] Found trade emerald, clicking");
                InventoryUtils.clickOpenContainerSlot(22);
            }
        } else if (InventoryUtils.getInventoryName() != null && InventoryUtils.getInventoryName().contains("Trades")) {
            LogUtils.debugLog("[AutoSell] Detected trade menu, selling item");
            List<Slot> sellList = InventoryUtils.getInventorySlots();
            sellList.removeIf(item -> !shouldSell(item.getStack()));
            System.out.println(sellList.size());
            if (sellList.size() > 0) {
                InventoryUtils.clickOpenContainerSlot(45 + sellList.get(0).slotNumber);
            } else {
                LogUtils.debugLog("[AutoSell] Out of items to sell!");
                disable();
            }
        } else {
            LogUtils.debugLog("[AutoSell] Unknown menu " + InventoryUtils.getInventoryName());
            mc.thePlayer.closeScreen();
        }



    }

    @SubscribeEvent
    public void OnKeyPress(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
            if (enabled) {
                LogUtils.debugLog("[AutoSell] Exiting sell");
                disable();
            }
        }
    }

    private static void addItemsToBeSold(){
        itemsToBeSold.clear();
        if(MightyMiner.config.sellAscensionRope)
            itemsToBeSold.add("Ascension Rope");
        if(MightyMiner.config.sellWishingCompass)
            itemsToBeSold.add("Wishing Compass");
    }
    public static boolean shouldStart(){
        addItemsToBeSold();
        return mc.thePlayer.inventory.getFirstEmptyStack() == -1 && InventoryUtils.isPresentInInventory(itemsToBeSold);
    }
    private static boolean shouldSell(ItemStack itemStack) {
        String name = net.minecraft.util.StringUtils.stripControlCodes(itemStack.getDisplayName());
        name = name.replaceAll("x([0-9])", "");
        return itemsToBeSold.contains(name.trim());
    }


}
