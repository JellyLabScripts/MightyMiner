package com.jelly.mightyminerv2.macro.impl.RouteMiner;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.failsafe.impl.NameMentionFailsafe;
import com.jelly.mightyminerv2.feature.FeatureManager;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.handler.RouteHandler;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.GlaciteVeins;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.states.GlacialMacroState;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.states.NewLobbyState;
import com.jelly.mightyminerv2.macro.impl.RouteMiner.states.GettingStatsState;
import com.jelly.mightyminerv2.macro.impl.RouteMiner.states.RouteMinerMacroState;
import com.jelly.mightyminerv2.util.helper.MineableBlock;
import com.jelly.mightyminerv2.util.helper.route.Route;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RouteMinerMacro extends AbstractMacro {

    @Getter
    private static final RouteMinerMacro instance = new RouteMinerMacro();

    @Setter
    @Getter
    private RouteMinerMacroState currentState;

    @Getter
    @Setter
    private int miningSpeed = 0;

    @Getter
    @Setter
    private BlockMiner.PickaxeAbility pickaxeAbility = BlockMiner.PickaxeAbility.NONE;

    @Override
    public void onEnable() {
        Route route = RouteHandler.getInstance().getSelectedRoute();

        if (route == null || route.isEmpty()) {
            error("Route is empty or null. Please use /rb for more information to setup a route.");
            MacroManager.getInstance().disable();
            return;
        }

        BlockMiner.getInstance().setWaitThreshold(500);

        this.miningSpeed = 0;
        this.pickaxeAbility = BlockMiner.PickaxeAbility.NONE;
        this.currentState = new GettingStatsState();

        log("Route Miner Macro enabled");
    }

    @Override
    public void onDisable() {
        if (currentState != null) {
            currentState.onEnd(this);
        }

        this.miningSpeed = 0;
        currentState = null;
        log("Route Miner Macro disabled");
        FeatureManager.getInstance().disableAll();
    }

    @Override
    public String getName() {
        return "Route Miner";
    }

    @Override
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        if (isTimerRunning() || currentState == null) {
            return;
        }

        RouteMinerMacroState nextState = currentState.onTick(this);
        transitionTo(nextState);
    }

    public void transitionTo(RouteMinerMacroState nextState) {
        if (currentState == nextState || nextState == null) {
            return;
        }

        currentState.onEnd(this);
        currentState = nextState;
        currentState.onStart(this);
    }

    @Override
    public void onPause() {
        FeatureManager.getInstance().pauseAll();
        log("Route Miner Macro Paused");
    }

    @Override
    public void onResume() {
        FeatureManager.getInstance().resumeAll();
        log("Route Miner Macro Resumed");
    }

    public MineableBlock[] getBlocksToMine() {
        List<MineableBlock> blocksList = new ArrayList<>();

        if (MightyMinerConfig.routeMineGemstone) {
            blocksList.add(MineableBlock.AMBER);
            blocksList.add(MineableBlock.AMETHYST);
            blocksList.add(MineableBlock.JADE);
            blocksList.add(MineableBlock.SAPPHIRE);
            blocksList.add(MineableBlock.AQUAMARINE);
            blocksList.add(MineableBlock.ONYX);
            blocksList.add(MineableBlock.CITRINE);
            blocksList.add(MineableBlock.PERIDOT);
            blocksList.add(MineableBlock.JASPER);
        }

        if (MightyMinerConfig.routeMineOre) {
            blocksList.add(MineableBlock.IRON);
            blocksList.add(MineableBlock.REDSTONE);
            blocksList.add(MineableBlock.COAL);
            blocksList.add(MineableBlock.GOLD);
            blocksList.add(MineableBlock.LAPIS);
            blocksList.add(MineableBlock.DIAMOND);
            blocksList.add(MineableBlock.EMERALD);
            blocksList.add(MineableBlock.QUARTZ);
        }

        if (MightyMinerConfig.routeMineTopaz) blocksList.add(MineableBlock.TOPAZ);
        if (MightyMinerConfig.routeMineGlacite) blocksList.add(MineableBlock.GLACITE);
        if (MightyMinerConfig.routeMineUmber) blocksList.add(MineableBlock.UMBER);
        if (MightyMinerConfig.routeMineTungsten) blocksList.add(MineableBlock.TUNGSTEN);

        return blocksList.stream().distinct().toArray(MineableBlock[]::new);
    }

    public int[] getBlockPriority() {
        MineableBlock[] blocksToMine = getBlocksToMine();
        int[] priorities = new int[blocksToMine.length];
        Arrays.fill(priorities, 1);
        return priorities;
    }

    @Override
    public List<String> getNecessaryItems() {
        List<String> items = new ArrayList<>();

        items.add(MightyMinerConfig.miningTool);
        items.add("Aspect of the Void");

        return items;
    }

}
