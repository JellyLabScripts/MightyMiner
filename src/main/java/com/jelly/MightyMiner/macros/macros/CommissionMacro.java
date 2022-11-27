package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.NpcUtil;
import com.jelly.MightyMiner.utils.PlayerUtils;
import com.jelly.MightyMiner.utils.TablistUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class CommissionMacro extends Macro {
    private AutoMineBaritone autoMineBaritone;

    public enum ComissionType {
        MITHRIL_MINER("Mithril Miner"),
        TITANIUM_MINER("Titanium Miner"),
        UPPER_MINES_MITHRIL("Upper Mines Mithril"),
        ROYAL_MINES_MITHRIL("Royal Mines Mithril"),
        LAVA_SPRINGS_MITHRIL("Lava Springs Mithril"),
        CLIFFSIDE_VEINS_MITHRIL("Cliffside Veins Mithril"),
        RAMPARTS_QUARRY_MITHRIL("Rampart's Quarry Mithril"),
        UPPER_MINES_TITANIUM("Upper Mines Titanium"),
        ROYAL_MINES_TITANIUM("Royal Mines Titanium"),
        LAVA_SPRINGS_TITANIUM("Lava Springs Titanium"),
        CLIFFSIDE_VEINS_TITANIUM("Cliffside Veins Titanium"),
        RAMPARTS_QUARRY_TITANIUM("Rampart's Quarry Titanium"),
        GOBLIN_SLAYER("Goblin Slayer"),
        ICE_WALKER_SLAYER("Ice Walker Slayer");

        private final String questName;

        ComissionType(String questName) {
            this.questName = questName;
        }
    }
    public enum State {
        CLICK_PIGEON,
        IN_PIGEON,
        FORGE_WARPING,
        NAVIGATING,
        COMMIT,
        COMMITTING
    }
    public enum WarpState {
        SETUP,
        LOOK,
        WARP
    }

    private final List<Block> allowedBlocks = new ArrayList<>();

    private ComissionType currentQuest = null;

    private State comissionState = State.CLICK_PIGEON;
    private WarpState warpState = WarpState.SETUP;

    private Array[] quests;
    @Override
    protected void onEnable() {
        this.setupMiningBlocks();

        comissionState = State.CLICK_PIGEON;
        warpState = WarpState.SETUP;

        if (PlayerUtils.getItemInHotbar("Pickaxe", "Drill", "Gauntlet", "Juju", "Terminator", "Aspect of the Void", "Royal Pigeon") == -1) {
            LogUtils.addMessage("You dont have items dumbass");
            MacroHandler.disableScript();
            return;
        }

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (PlayerUtils.hasEntityInRadius(3)) {
                if (!NpcUtil.isNpc(entity)){
                    LogUtils.debugLog("no king, emissary dumbass");
                }
            }
        }

    }
    @Override
    public void onTick(TickEvent.Phase phase) {

        List<String> tablist = TablistUtils.getTabList();

        tablist.forEach(s -> {
            switch (StringUtils.stripControlCodes(s)) {
                case "Goblin Slayer":
                    currentQuest = ComissionType.GOBLIN_SLAYER;
                    break;
                case "Ice Walker Slayer":
                    currentQuest = ComissionType.ICE_WALKER_SLAYER;
                    break;
                case "Mithril Miner":
                    currentQuest = ComissionType.MITHRIL_MINER;
                    break;
                case "Titanium Miner":
                    currentQuest = ComissionType.TITANIUM_MINER;
                    break;
                case "Upper Mines Mithril":
                    currentQuest = ComissionType.UPPER_MINES_MITHRIL;
                    break;
                case "Royal Mines Mithril":
                    currentQuest = ComissionType.ROYAL_MINES_MITHRIL;
                    break;
                case "Lava Springs Mithril":
                    currentQuest = ComissionType.LAVA_SPRINGS_MITHRIL;
                    break;
                case "Rampart's Quarry Mithril":
                    currentQuest = ComissionType.RAMPARTS_QUARRY_MITHRIL;
                    break;
                case "Cliffside Veins Mithril":
                    currentQuest = ComissionType.CLIFFSIDE_VEINS_MITHRIL;
                    break;
                case "Upper Mines Titanium":
                    currentQuest = ComissionType.UPPER_MINES_TITANIUM;
                    break;
                case "Royal Mines Titanium":
                    currentQuest = ComissionType.ROYAL_MINES_TITANIUM;
                    break;
                case "Lava Springs Titanium":
                    currentQuest = ComissionType.LAVA_SPRINGS_TITANIUM;
                    break;
                case "Rampart's Quarry Titanium":
                    currentQuest = ComissionType.RAMPARTS_QUARRY_TITANIUM;
                    break;
                case "Cliffside Veins Titanium":
                    currentQuest = ComissionType.CLIFFSIDE_VEINS_TITANIUM;
                    break;
                default:
                    currentQuest = null;
            }
        });



    }

    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event) {
    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {
    }

    @Override
    protected void onDisable() {

    }

    private BaritoneConfig baritoneConfig(){
        return new BaritoneConfig(
                AutoMineType.STATIC,
                true,
                true,
                true,
                350,
                8,
                null,
                allowedBlocks,
                256,
                0

        );
    }

    public void setupMiningBlocks() {
        allowedBlocks.clear();

        allowedBlocks.add(Blocks.prismarine);
        allowedBlocks.add(Blocks.wool);
        allowedBlocks.add(Blocks.stained_hardened_clay);
        allowedBlocks.add(Blocks.stone);
    }

}
