package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.*;
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

        public final String questName;

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

        currentQuest = ComissionUtils.determineComm().getKey();


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
                MiningType.STATIC,
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
