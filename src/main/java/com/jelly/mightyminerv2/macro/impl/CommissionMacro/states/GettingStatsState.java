package com.jelly.mightyminerv2.macro.impl.CommissionMacro.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.AutoGetStats;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.impl.MiningSpeedRetrievalTask;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.impl.PickaxeAbilityRetrievalTask;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GettingStatsState implements CommissionMacroState {

    private final AutoGetStats autoInventory = AutoGetStats.getInstance();
    private MiningSpeedRetrievalTask miningSpeedRetrievalTask;
    private PickaxeAbilityRetrievalTask pickaxeAbilityRetrievalTask;

    private static final int FALLBACK_MINING_SPEED = 1000;
    private static final Pattern SPEED_RE = Pattern.compile("(?i)Mining\\s*Speed\\D*(\\d[\\d,\\.]*)");

    private enum Phase { SEND_CMD, WAIT_MENU, CLICK_PROFILE, WAIT_PROFILE, CLICK_PICKAXE, WAIT_PICKAXE, SCAN_STATS, DONE }
    private Phase phase = Phase.SEND_CMD;
    private int ticksWaited = 0;

    private int parsedMiningSpeed = FALLBACK_MINING_SPEED;

    @Override
    public void onStart(CommissionMacro macro) {
        miningSpeedRetrievalTask = new MiningSpeedRetrievalTask();
        pickaxeAbilityRetrievalTask = new PickaxeAbilityRetrievalTask();
        AutoGetStats.getInstance().startTask(miningSpeedRetrievalTask);
        AutoGetStats.getInstance().startTask(pickaxeAbilityRetrievalTask);
    }

    @Override
    public CommissionMacroState onTick(CommissionMacro macro) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return this;

        ticksWaited++;

        switch (phase) {
            case SEND_CMD:
                mc.thePlayer.sendChatMessage("/sbmenu");
                ticksWaited = 0;
                phase = Phase.WAIT_MENU;
                return this;

            case WAIT_MENU:
                if (isGuiOpen()) {
                    clickItemByName(mc, "Your SkyBlock Profile");
                    phase = Phase.WAIT_PROFILE;
                    ticksWaited = 0;
                }
                return this;

            case WAIT_PROFILE:
                if (isGuiOpen()) {
                    clickItemByName(mc, "Mining Stats");
                    phase = Phase.WAIT_PICKAXE;
                    ticksWaited = 0;
                }
                return this;

            case WAIT_PICKAXE:
                if (isGuiOpen()) {
                    Integer parsed = readMiningSpeedFromOpenGui(mc);
                    if (parsed != null) {
                        parsedMiningSpeed = parsed;
                    } else {
                        parsedMiningSpeed = FALLBACK_MINING_SPEED;
                    }
                    mc.thePlayer.closeScreen(); // close after retrieving
                    phase = Phase.DONE;
                }
                return this;

            case DONE:
                BlockMiner.PickaxeAbility pickaxeAbility;
                if (pickaxeAbilityRetrievalTask.getError() != null) {
                    pickaxeAbility = BlockMiner.PickaxeAbility.NONE;
                } else {
                    pickaxeAbility = MightyMinerConfig.usePickaxeAbility
                            ? pickaxeAbilityRetrievalTask.getResult()
                            : BlockMiner.PickaxeAbility.NONE;
                }

                macro.setMiningSpeed(parsedMiningSpeed);
                macro.setPickaxeAbility(pickaxeAbility);

                sendChat("Your mining speed is " + parsedMiningSpeed);
                return new StartingState();
        }

        return this;
    }

    @Override
    public void onEnd(CommissionMacro macro) {
        autoInventory.stop();
    }

    // --- helpers ---

    private boolean isGuiOpen() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.currentScreen instanceof GuiContainer;
    }

    private void sendChat(String msg) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§b[MightyMiner]§r " + msg));
        }
    }

    private void clickItemByName(Minecraft mc, String nameFragment) {
        if (!(mc.currentScreen instanceof GuiContainer)) return;

        GuiContainer gui = (GuiContainer) mc.currentScreen;
        Container container = gui.inventorySlots;

        for (Object o : container.inventorySlots) {
            Slot s = (Slot) o;
            ItemStack st = s.getStack();
            if (st == null) continue;

            String name = stripColor(st.getDisplayName());
            if (name != null && name.contains(nameFragment)) {
                mc.playerController.windowClick(container.windowId, s.slotNumber, 0, 0, mc.thePlayer);
                return;
            }
        }
    }

    private Integer readMiningSpeedFromOpenGui(Minecraft mc) {
        if (!(mc.currentScreen instanceof GuiContainer)) {
            return null;
        }

        GuiContainer gui = (GuiContainer) mc.currentScreen;
        Container container = gui.inventorySlots;

        int highest = -1;

        for (Object o : container.inventorySlots) {
            Slot s = (Slot) o;
            ItemStack st = s.getStack();
            if (st == null) continue;

            List<String> lines;
            try {
                lines = st.getTooltip(mc.thePlayer, false);
            } catch (Exception e) {
                continue;
            }

            if (lines == null) continue;

            for (String line : lines) {
                String plain = stripColor(line);
                Matcher m = SPEED_RE.matcher(plain);
                if (m.find()) {
                    String num = m.group(1).replace(",", "");
                    try {
                        int value = Integer.parseInt(num.split("\\.")[0]);
                        if (value > highest) {
                            highest = value;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        return highest > 0 ? highest : null;
    }

    private String stripColor(String s) {
        return s == null ? null : s.replaceAll("§.", "");
    }
}
