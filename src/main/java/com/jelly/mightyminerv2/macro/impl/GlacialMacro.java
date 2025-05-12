package com.jelly.mightyminerv2.macro.impl;

import akka.japi.Pair;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.*;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.handler.GameStateHandler;
import com.jelly.mightyminerv2.handler.GraphHandler;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.macro.impl.helper.GlaciteVeins;
import com.jelly.mightyminerv2.util.*;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.MineableBlock;
import com.jelly.mightyminerv2.util.helper.location.SubLocation;
import com.jelly.mightyminerv2.util.helper.route.Route;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import lombok.Getter;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.stream.Collectors;

public class GlacialMacro extends AbstractMacro {

    @Getter
    private static final GlacialMacro instance = new GlacialMacro();
    private final BlockMiner miner = BlockMiner.getInstance();

    private MainState mainState = MainState.TELEPORTING;
    private State state = State.PATHFINDING;
    private InitializeState initializeState = InitializeState.STARTING;
    private TeleportState teleportState = TeleportState.STARTING;

    private int macroRetries = 0;
    private int warpRetries = 0;
    
    private static final ArrayList<GlaciteVeins> typeToMine = new ArrayList<>();
    private static Pair<GlaciteVeins, RouteWaypoint> currentVein = null;
    private final Map<Pair<GlaciteVeins, RouteWaypoint>, Long> previousVeins = new HashMap<>();

    public int miningSpeed = 200;
    
    @Override
    public void onEnable() {
        miner.setWaitThreshold(500);
        mainState = MainState.TELEPORTING;
        state = State.PATHFINDING;
        initializeState = InitializeState.STARTING;
        teleportState = TeleportState.STARTING;

        typeToMine.clear();
        currentVein = null;
        previousVeins.clear();

        super.onEnable();
    }

    @Override
    public String getName() {
        return "Glacial Macro";
    }

    @Override
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        if (this.isTimerRunning()) {
            return;
        }

        switch (mainState) {
            case NONE:
                return;
            case TELEPORTING:
                onTeleportState();
                return;
            case INITIALIZATION:
                onInitializeState();
                return;
            case MACRO:
                onMacroState();
        }
    }

    private void onTeleportState() {
        if (GameStateHandler.getInstance().getCurrentSubLocation() == SubLocation.DWARVEN_BASE_CAMP || GameStateHandler.getInstance().getCurrentSubLocation() == SubLocation.GLACITE_TUNNELS) {
            mainState = MainState.INITIALIZATION;
            return;
        }

        switch (teleportState) {
            case STARTING:
                teleportState = (TeleportState.TRIGGERING_AUTOWARP);
                break;

            case TRIGGERING_AUTOWARP:
                AutoWarp.getInstance().start(null, SubLocation.DWARVEN_BASE_CAMP);
                teleportState = (TeleportState.WAITING_FOR_AUTOWARP);
                break;

            case WAITING_FOR_AUTOWARP:
                if (AutoWarp.getInstance().isRunning()) {
                    return;
                }

                log("AutoWarp Ended");

                if (AutoWarp.getInstance().hasSucceeded()) {
                    log("AutoWarp Completed");
                    this.mainState = (MainState.MACRO);
                    return;
                }

                if (++this.warpRetries > 3) {
                    this.mainState = (MainState.NONE);
                    error("Tried to warp 3 times but didn't reach destination. Disabling.");
                } else {
                    log("Something went wrong while warping. Trying to fix!");
                    teleportState = (TeleportState.HANDLING_ERRORS);
                }
                break;

            case HANDLING_ERRORS:
                switch (AutoWarp.getInstance().getFailReason()) {
                    case NONE:
                        throw new IllegalStateException("AutoWarp Failed But FailReason is NONE.");
                    case FAILED_TO_WARP:
                        log("Retrying AutoWarp");
                        teleportState = TeleportState.STARTING;
                        break;
                    case NO_SCROLL:
                        log("No Warp Scroll. Disabling");
                        this.mainState = MainState.NONE;
                        break;
                }
        }
    }

    @Override
    public void onChat(String message) {
        if (message.contains("Commission Completed!") && !message.contains(":")) {
            miner.stop();
            state = State.CLAIMING_COMMISSION;
            lastCommission.schedule(3000);
        }
    }

    private void onInitializeState() {
        switch (initializeState) {
            case STARTING:
                initializeState = InitializeState.CHECKING_STATS;
                break;
            case CHECKING_STATS:
                AutoInventory.getInstance().retrieveSpeedBoost();
                this.initializeState = InitializeState.GETTING_STATS;
                break;
            case GETTING_STATS:
                if (AutoInventory.getInstance().isRunning()) {
                    return;
                }

                if (AutoInventory.getInstance().sbSucceeded()) {
                    int[] sb = AutoInventory.getInstance().getSpeedBoostValues();
                    this.miningSpeed = sb[0];
                    this.macroRetries = 0;
                    this.mainState = MainState.MACRO;
                    this.state = State.PATHFINDING;
                    log("MiningSpeed: " + miningSpeed);
                    return;
                }

                switch (AutoInventory.getInstance().getSbError()) {
                    case NONE:
                        throw new IllegalStateException("AutoInventory#getSbError failed but returned NONE");
                    case CANNOT_OPEN_INV:
                        if (++this.macroRetries > 3) {
                            this.mainState = MainState.NONE;
                            error("Tried 3 times to open inv but failed. Stopping");
                        } else {
                            this.initializeState = InitializeState.STARTING;
                            log("Failed to open inventory. Retrying");
                        }
                        break;
                    case CANNOT_GET_VALUE:
                        this.mainState = MainState.NONE;
                        error("Failed To Get Value. Follow Previous Instruction (If Any) or contact the developer.");
                        break;
                }
                mainState = MainState.MACRO;
                state = State.PATHFINDING;
                break;
        }
    }

    private Clock lastCommission = new Clock();

    private void onMacroState() {
        previousVeins.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > 1000 * 60 * 5);
        typeToMine.clear();

        Set<Map.Entry<GlaciteVeins, Double>> entrySet = TablistUtil.getGlaciteComs().entrySet();
        for (Map.Entry<GlaciteVeins, Double> entry : entrySet) {
            if (entry.getValue() >= 100.0) continue;
            typeToMine.add(entry.getKey());
        }

        if (AutoCommissionClaim.getInstance().isRunning()) return;
        boolean hasGlacialComm = TablistUtil.getGlaciteComs().values().stream().anyMatch(value -> value >= 100.0);
        if (hasGlacialComm && (!lastCommission.isScheduled() || lastCommission.passed())) {
            miner.stop();
            state = State.CLAIMING_COMMISSION;
            lastCommission.schedule(10_000);
        }

        switch (state) {
            case CLAIMING_COMMISSION:
                AutoCommissionClaim.getInstance().start();
                this.state = State.CLAIM_VERIFY;
                return;
            case CLAIM_VERIFY:
                if (AutoCommissionClaim.getInstance().isRunning()) {
                    return;
                }

                if (AutoCommissionClaim.getInstance().succeeded()) {
                    this.state = State.PATHFINDING;
                    return;
                }

                if (++this.macroRetries > 3) {
                    error("Tried three time but kept getting timed out. Disabling");
                    this.mainState = MainState.NONE;
                    return;
                }

                switch (AutoCommissionClaim.getInstance().claimError()) {
                    case NONE:
                        error("AutoCommissionClaim Failed but ClaimError is NONE.");
                        this.mainState = MainState.NONE;
                        return;
                    case TIMEOUT:
                        log("Retrying claim");
                        changeState(State.CLAIMING_COMMISSION, 3000);
                        return;
                }
                return;
            case MINING:
                if (miner.getError() == BlockMiner.BlockMinerError.NOT_ENOUGH_BLOCKS) {
                    miner.stop();
                    changeState(State.PATHFINDING);
                    return;
                }

                if (!miner.isRunning()) {
                    miner.start(getBlocksToMine(), miningSpeed, getBlockPriority(), MightyMinerConfig.miningTool);
                    return;
                }

                break;
            case PATHFINDING:
                if (RouteNavigator.getInstance().isRunning()) {
                    Logger.sendError("Pathfinding is already running and state changed to pathfinding");
                    return;
                }

                if (!InventoryUtil.holdItem("Aspect of the Void")) {
                    Logger.sendError("You need an Aspect of the Void dumbass.");
                    MacroManager.getInstance().disable();
                    return;
                }

                if (ScoreboardUtil.cold >= 50) {
                    KeyBindUtil.releaseAllExcept();
                    AutoWarp.getInstance().start(null, SubLocation.DWARVEN_BASE_CAMP);
                    Logger.sendMessage("Player is getting too cold, warping back.");
                    changeState(State.TELEPORTING);
                    return;
                }

                previousVeins.put(currentVein, System.currentTimeMillis());
                currentVein = findBestVein();

                if (currentVein == null) {
                    Logger.sendError("No vein to mine, ending script.");
                    this.onDisable();
                    return;
                }

                if (currentVein.second().isWithinRange(PlayerUtil.getBlockStandingOn(), 2)) {
                    Logger.sendError("The player is already standing on the next calculated vein pos.");
                    return;
                }

                List<RouteWaypoint> path = GraphHandler.instance.findPathFrom(getName(), PlayerUtil.getBlockStandingOn(), currentVein.second());

                if (path.isEmpty()) {
                    Logger.sendError("Could not find a path to target. Stopping. Start: " + PlayerUtil.getBlockStandingOn() + ", End: " + currentVein.second());
                    this.onDisable();
                    return;
                }

                Logger.sendLog("Starting path to the next vein coordinate");
                RouteNavigator.getInstance().start(new Route(path));
                changeState(State.WAITING_ON_PATHFINDING);
            case WAITING_ON_PATHFINDING:
                if (RouteNavigator.getInstance().isRunning()) return;

                if (
                        Pathfinder.getInstance().completedPathTo(currentVein.second().toBlockPos()) ||
                                (!Pathfinder.getInstance().isRunning() && Pathfinder.getInstance().succeeded()) ||
                                PlayerUtil.getBlockStandingOn().equals(currentVein.second().toBlockPos())
                ) {
                    changeState(State.MINING);
                    Logger.sendLog("Reached final path destination!");
                }

                if (Pathfinder.getInstance().failed()) {
                    Logger.sendError("Failed to pathfind to next vein! Going back to warp base.");
                    Logger.sendError("Failed path: " + PlayerUtil.getBlockStandingOn() + " -> " + currentVein.second().toBlockPos());
                    Logger.sendError("Please report to an administrator!");

                    AutoWarp.getInstance().start(null, SubLocation.DWARVEN_BASE_CAMP);
                    changeState(State.TELEPORTING);
                }

                break;
            case TELEPORTING:
                if (!AutoWarp.getInstance().getFailReason().equals(AutoWarp.Error.NONE)) {
                    Logger.sendError("You haven't unlocked the Glacite Mines, ending script.");
                    return;
                }

                if (ScoreboardUtil.cold != 0) return;
                if (GameStateHandler.getInstance().getCurrentSubLocation().equals(SubLocation.DWARVEN_BASE_CAMP))
                    changeState(State.PATHFINDING);

                break;
        }
    }

    private void changeState(State to, int timeToWait) {
        state = to;
        this.timer.schedule(timeToWait);
    }

    private void changeState(State to) {
        state = to;
    }

    public Pair<GlaciteVeins, RouteWaypoint> findBestVein() {
        List<Pair<GlaciteVeins, RouteWaypoint>> possibleVeins = new ArrayList<>();

        for (GlaciteVeins vein : typeToMine) {
            for (RouteWaypoint waypoint : GlaciteVeins.getVeins(vein)) {
                if (previousVeins.get(new Pair<>(vein, waypoint)) != null) continue;
                possibleVeins.add(new Pair<>(vein, waypoint));
            }
        }

        if (possibleVeins.isEmpty()) return null;

        return possibleVeins.stream()
                .sorted(Comparator.comparingDouble(pair -> GraphHandler.instance.distance(getName(), PlayerUtil.getBlockStandingOn(), pair.second())))
                .collect(Collectors.toList())
                .get(0);
    }

    public MineableBlock[] getBlocksToMine() {
        MineableBlock[] blocksToMine = new MineableBlock[]{};
        List<MineableBlock> blocksList = new ArrayList<>();

        if (typeToMine.contains(GlaciteVeins.UMBER)) blocksList.add(MineableBlock.UMBER);
        if (typeToMine.contains(GlaciteVeins.TUNGSTEN)) blocksList.add(MineableBlock.TUNGSTEN);
        if (typeToMine.contains(GlaciteVeins.GLACITE)) blocksList.add(MineableBlock.GLACITE);

        if (typeToMine.contains(GlaciteVeins.AMBER)) blocksList.add(MineableBlock.AMBER);
        if (typeToMine.contains(GlaciteVeins.SAPPHIRE)) blocksList.add(MineableBlock.SAPPHIRE);
        if (typeToMine.contains(GlaciteVeins.AMETHYST)) blocksList.add(MineableBlock.AMETHYST);
        if (typeToMine.contains(GlaciteVeins.RUBY)) blocksList.add(MineableBlock.RUBY);
        if (typeToMine.contains(GlaciteVeins.JADE)) blocksList.add(MineableBlock.JADE);
        if (typeToMine.contains(GlaciteVeins.AQUAMARINE)) blocksList.add(MineableBlock.AQUAMARINE);
        if (typeToMine.contains(GlaciteVeins.ONYX)) blocksList.add(MineableBlock.ONYX);
        if (typeToMine.contains(GlaciteVeins.PERIDOT)) blocksList.add(MineableBlock.PERIDOT);
        if (typeToMine.contains(GlaciteVeins.CITRINE)) blocksList.add(MineableBlock.CITRINE);

        return blocksList.toArray(blocksToMine);
    }

    public int[] getBlockPriority() {
        return new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
    }

    @Override
    public List<String> getNecessaryItems() {
        List<String> items = new ArrayList<>();
        if (MightyMinerConfig.drillRefuel) {
            items.add("Abiphone");

            String fuel = MightyMinerConfig.refuelMachineFuel == 0 ? "Volta" : "Oil Barrel";

            if (InventoryUtil.getHotbarSlotOfItem(fuel) == -1) {
                Logger.sendError("You need fuel to run this macro, implementation will be added soon for retrieving, ending macro.");
                MacroManager.getInstance().disable();
                return Collections.emptyList();
            }

            items.add(fuel);
        }

        String teleportationItem = "Aspect of the Void";

        if (InventoryUtil.getHotbarSlotOfItem(teleportationItem) == -1) {
            Logger.sendError("You need a AOTV with etherwarp for this macro.");
            MacroManager.getInstance().disable();
            return Collections.emptyList();
        }

        items.add("Royal Pigeon");
        items.add(teleportationItem);
        items.add(MightyMinerConfig.miningTool);
        return items;
    }

    private enum MainState {
        NONE, TELEPORTING, INITIALIZATION, MACRO
    }

    enum TeleportState {
        STARTING, TRIGGERING_AUTOWARP, WAITING_FOR_AUTOWARP, HANDLING_ERRORS
    }

    enum InitializeState {
        STARTING, CHECKING_STATS, GETTING_STATS
    }

    private enum State {
        CLAIMING_COMMISSION, CLAIM_VERIFY, MINING, PATHFINDING, WAITING_ON_PATHFINDING, TELEPORTING
    }

}
