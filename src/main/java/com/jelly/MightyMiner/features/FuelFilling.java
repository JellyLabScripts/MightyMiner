package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import com.jelly.MightyMiner.utils.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

public class FuelFilling {

    private final Minecraft mc = Minecraft.getMinecraft();
    private int fuel = -1;
    public static Timer waitTimer = new Timer();

    private Macro lastMacro;
    private int drillSlotIndex;

    public enum states {
        NONE,
        WAITING,
        ABIPHONE_RCD,
        ABIPHONE_RCU,
        CALLING,
        REFILLING_DRILL,
        REFILLING_FUEL,
        ACCEPTING,
        CLOSING,
        EXIT
    }

    public static states currentState = states.NONE;

    private void Reset() {
        currentState = states.NONE;
        lastMacro = null;
        waitTimer.reset();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!MightyMiner.config.refuelWithAbiphone || mc.thePlayer == null) return;
        if (currentState != states.NONE) return;
        if (MacroHandler.macros.stream().noneMatch(Macro::isEnabled) && lastMacro == null) {
            Reset();
            return;
        }


        drillSlotIndex = PlayerUtils.getItemInHotbarWithBlackList(true, "Blue Cheese", "Drill");

        if (drillSlotIndex == -1) {
            drillSlotIndex = PlayerUtils.getItemInHotbar(true, "Drill");
            if(drillSlotIndex == -1) {
                drillSlotIndex = 0;
                return;
            }

        }
        ItemStack drill = mc.thePlayer.inventory.mainInventory[drillSlotIndex];

        if(drill == null) return;
        if (!drill.getDisplayName().toLowerCase().contains("drill")) return;
        ArrayList<String> itemLore = PlayerUtils.getItemLore(drill);

        for (String lore: itemLore) {
            try {
                if (lore.contains("Fuel:")) {
                    String[] strings = lore.split("/");
                    if (strings.length != 2) continue;
                    String fuel = strings[0].split(" ")[1];
                    this.fuel = Integer.parseInt(fuel.replace(",", "").trim());
                    break;
                }
            } catch (Exception ignore) {
            }
        }
    }

    @SubscribeEvent
    public void onTickSecond(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (MacroHandler.macros.stream().noneMatch(Macro::isEnabled)  && lastMacro == null) return;
        if (!MightyMiner.config.refuelWithAbiphone || mc.thePlayer == null) return;

        if (!(fuel != -1 && fuel < MightyMiner.config.refuelThreshold)) return;

        switch (currentState) {
            case NONE: {
                LogUtils.addMessage("Low fuel. Refilling now.");
                MacroHandler.macros.forEach(macro -> {
                    if (macro.isEnabled()) {
                        lastMacro = macro;
                        macro.Pause();
                    }
                });
                currentState = states.WAITING;
                waitTimer.reset();
                break;
            }

            case WAITING: {
                if (!waitTimer.hasReached(300)) return;
                int abiphoneIndex = PlayerUtils.getItemInHotbar(true, "Abiphone");
                if (abiphoneIndex == -1) {
                    LogUtils.addMessage("Abiphone not found. Stopping refuel.");
                    currentState = states.NONE;
                    MightyMiner.config.refuelWithAbiphone = false;
                    Reset();
                    return;
                }
                mc.thePlayer.inventory.currentItem = abiphoneIndex;
                currentState = states.ABIPHONE_RCD;
                waitTimer.reset();
                break;
            }

            case ABIPHONE_RCD: {
                if (!waitTimer.hasReached(300)) return;

                KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, true);
                currentState = states.ABIPHONE_RCU;
                waitTimer.reset();
                break;
            }

            case ABIPHONE_RCU: {
                if (!waitTimer.hasReached(100)) return;
                KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, false);
                currentState = states.CALLING;
                waitTimer.reset();
                break;
            }

            case CALLING: {
                if (mc.thePlayer.openContainer instanceof ContainerChest) {
                    ContainerChest abiphone = (ContainerChest) mc.thePlayer.openContainer;
                    IInventory inv = abiphone.getLowerChestInventory();
                    if (!inv.getDisplayName().getFormattedText().toLowerCase().contains("abiphone")) {
                        //Not in abiphone
                        return;
                    }

                    if (!waitTimer.hasReached(500)) return;

                    for (Slot slot : abiphone.inventorySlots) {
                        if (slot.getStack() != null && slot.getStack().getDisplayName() != null) {
                            if (slot.getStack().getDisplayName().toLowerCase().contains("jotraeline")) {
                                mc.playerController.windowClick(abiphone.windowId, slot.slotNumber, 0, 0, mc.thePlayer);
                                currentState = states.REFILLING_DRILL;
                                waitTimer.reset();
                                return;
                            }
                        }
                    }
                    //Not found jotraelina
                    LogUtils.addMessage("You don't have Jotraeline in contacts!");
                    LogUtils.addMessage("Add the npc to abiphone and re-enable the feature in options menu");
                    MightyMiner.config.refuelWithAbiphone = false;
                    Reset();
                }
                break;
            }

            case REFILLING_DRILL: {
                if (mc.thePlayer.openContainer instanceof ContainerChest) {

                    ContainerChest drillAnvil = (ContainerChest) mc.thePlayer.openContainer;
                    IInventory inv = drillAnvil.getLowerChestInventory();
                    if (!inv.getDisplayName().getFormattedText().toLowerCase().contains("drill anvil")) {
                        //Not in drill anvil
                        return;
                    }

                    if (!waitTimer.hasReached(500)) return;

                    if (drillAnvil.getSlot(drillAnvil.inventorySlots.size() - 9 + drillSlotIndex) != null && drillAnvil.getSlot(drillAnvil.inventorySlots.size() - 9 + drillSlotIndex).getStack() == null) {
                        LogUtils.addMessage("Drill slot is empty!");
                        return;
                    }

                    mc.playerController.windowClick(drillAnvil.windowId, drillAnvil.inventorySlots.size() - 9 + drillSlotIndex, 0, 1, mc.thePlayer);

                    waitTimer.reset();
                    currentState = states.REFILLING_FUEL;
                }
                break;
            }

            case REFILLING_FUEL: {
                if (!waitTimer.hasReached(500)) return;

                if (mc.thePlayer.openContainer instanceof ContainerChest) {

                    ContainerChest drillAnvil = (ContainerChest) mc.thePlayer.openContainer;
                    IInventory inv = drillAnvil.getLowerChestInventory();
                    if (!inv.getDisplayName().getFormattedText().toLowerCase().contains("drill anvil")) {
                        //Not in drill anvil
                        return;
                    }

                    String fuelName = "";

                    switch (MightyMiner.config.typeOfFuelIndex) {
                        case 0: {
                            fuelName = "goblin egg";
                            break;
                        }
                        case 1: {
                            fuelName = "biofuel";
                            break;
                        }
                        case 2: {
                            fuelName = "volta";
                            break;
                        }
                        case 3: {
                            fuelName = "oil barrel";
                            break;
                        }
                    }

                    for (Slot slot : drillAnvil.inventorySlots) {
                        if (slot == null || slot.getStack() == null || slot.getStack().getDisplayName() == null || slot.getStack().getDisplayName().trim().equals("")) {
                            continue;
                        }

                        if (slot.getStack().getDisplayName().toLowerCase().contains(fuelName)) {
                            mc.playerController.windowClick(drillAnvil.windowId, slot.slotNumber, 0, 1, mc.thePlayer);
                            currentState = states.ACCEPTING;
                            waitTimer.reset();
                            return;
                        }
                    }

                    LogUtils.addMessage("You have no fuel in inventory!");
                    LogUtils.addMessage("Buy more fuel and re-enable the feature in options menu");
                    MightyMiner.config.refuelWithAbiphone = false;
                    Reset();
                }
            }

            case ACCEPTING: {
                if (!waitTimer.hasReached(500)) return;

                if (mc.thePlayer.openContainer instanceof ContainerChest) {
                    ContainerChest drillAnvil = (ContainerChest) mc.thePlayer.openContainer;
                    IInventory inv = drillAnvil.getLowerChestInventory();
                    if (!inv.getDisplayName().getFormattedText().toLowerCase().contains("drill anvil")) {
                        //Not in drill anvil
                        return;
                    }

                    if (drillAnvil.getSlot(29) != null && drillAnvil.getSlot(29).getStack() != null &&
                    drillAnvil.getSlot(33) != null && drillAnvil.getSlot(33).getStack() != null) {
                        mc.playerController.windowClick(drillAnvil.windowId, 22, 0, 0, mc.thePlayer);
                        waitTimer.reset();
                        currentState = states.CLOSING;
                        return;
                    }
                }
                break;
            }

            case CLOSING: {
                if (!waitTimer.hasReached(500)) return;

                if (mc.thePlayer.openContainer instanceof ContainerChest) {
                    ContainerChest drillAnvil = (ContainerChest) mc.thePlayer.openContainer;
                    IInventory inv = drillAnvil.getLowerChestInventory();
                    if (!inv.getDisplayName().getFormattedText().toLowerCase().contains("drill anvil")) {
                        //Not in drill anvil
                        return;
                    }

                    if (drillAnvil.getSlot(29) != null && drillAnvil.getSlot(29).getStack() == null) {
                        mc.playerController.windowClick(drillAnvil.windowId, 13, 0, 0, mc.thePlayer);
                        currentState = states.EXIT;
                        waitTimer.reset();
                    }
                }
                break;
            }

            case EXIT: {
                if (mc.thePlayer.openContainer instanceof ContainerChest) {
                    ContainerChest drillAnvil = (ContainerChest) mc.thePlayer.openContainer;
                    IInventory inv = drillAnvil.getLowerChestInventory();

                    if (!inv.getDisplayName().getFormattedText().toLowerCase().contains("drill anvil")) {
                        //Not in drill anvil
                        return;
                    }

                    if (!waitTimer.hasReached(500)) return;

                    mc.playerController.windowClick(drillAnvil.windowId, drillAnvil.inventorySlots.size() - 9 + drillSlotIndex, 0, 0, mc.thePlayer);

                    if (mc.thePlayer.openContainer != null) {
                        mc.thePlayer.closeScreen();
                    }

                    mc.thePlayer.inventory.currentItem = drillSlotIndex;

                    if (lastMacro != null)
                        lastMacro.Unpause();
                    lastMacro = null;

                    Reset();
                }
                break;
            }
        }
    }
}
