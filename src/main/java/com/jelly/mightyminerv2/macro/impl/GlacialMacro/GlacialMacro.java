package com.jelly.mightyminerv2.macro.impl.GlacialMacro;

import akka.japi.Pair;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.*;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.AutoGetStats;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.impl.MiningSpeedRetrievalTask;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.handler.GameStateHandler;
import com.jelly.mightyminerv2.handler.GraphHandler;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.*;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.MineableBlock;
import com.jelly.mightyminerv2.util.helper.location.SubLocation;
import com.jelly.mightyminerv2.util.helper.route.Route;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
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
    private int miningRetries = 0;
    private static final int MAX_MINING_RETRIES = 3;

    private static final ArrayList<GlaciteVeins> typeToMine = new ArrayList<>();
    private static Pair<GlaciteVeins, RouteWaypoint> currentVein = null;
    private final Map<Pair<GlaciteVeins, RouteWaypoint>, Long> previousVeins = new HashMap<>();

    public int miningSpeed = 0;
    private MiningSpeedRetrievalTask miningSpeedRetrievalTask;

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
        miningRetries = 0;

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
                miningSpeedRetrievalTask = new MiningSpeedRetrievalTask();
                AutoGetStats.getInstance().startTask(miningSpeedRetrievalTask);
                this.initializeState = InitializeState.GETTING_STATS;
                break;
            case GETTING_STATS:
                if (!AutoGetStats.getInstance().hasFinishedAllTasks()) {
                    return;
                }

                if (miningSpeedRetrievalTask.getTaskStatus().isSuccessful()) {
                    this.miningSpeed = miningSpeedRetrievalTask.getResult();
                    this.macroRetries = 0;
                    this.mainState = MainState.MACRO;
                    this.state = State.PATHFINDING;
                    log("MiningSpeed: " + miningSpeed);
                    return;
                }

                String errorMessage = miningSpeedRetrievalTask.getError();
                if (errorMessage == null || errorMessage.isEmpty()) {
                    errorMessage = "Unknown error";
                }

                if (errorMessage.equals("Cannot open SkyBlock Menu")) {
                    if (++this.macroRetries > 3) {
                        this.mainState = MainState.NONE;
                        error("Tried 3 times to open inventory but failed. Stopping");
                    } else {
                        this.initializeState = InitializeState.STARTING;
                        log("Failed to open inventory. Retrying");
                    }
                } else {
                    this.mainState = MainState.NONE;
                    error("Failed To Get Mining Speed Value: " + errorMessage + ". Please contact the developer.");
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
        log("Raw glacite commissions from tablist: " + entrySet.toString());

        for (Map.Entry<GlaciteVeins, Double> entry : entrySet) {
            log("Commission: " + entry.getKey() + " = " + entry.getValue() + "%");
            if (entry.getValue() >= 100.0) {
                log("Skipping " + entry.getKey() + " (already complete)");
                continue;
            }
            typeToMine.add(entry.getKey());
            log("Added " + entry.getKey() + " to typeToMine");
        }

        log("Final typeToMine list: " + typeToMine.toString());

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
                // Debug: Always log what we're trying to mine when entering mining state
                if (miningRetries == 0) {
                    MineableBlock[] blocksToMine = getBlocksToMine();
                    int[] blockPriorities = getBlockPriority();

                    log("=== ENTERING MINING STATE DEBUG ===");
                    log("TypeToMine list: " + typeToMine.toString());
                    log("Blocks array: " + Arrays.toString(blocksToMine));
                    log("Block priorities: " + Arrays.toString(blockPriorities));
                    log("Current vein: " + (currentVein != null ? currentVein.first() : "null"));
                    log("Mining speed: " + miningSpeed);
                    log("Mining tool: " + MightyMinerConfig.miningTool);

                    // Log the actual state IDs that will be searched for
                    for (int i = 0; i < blocksToMine.length; i++) {
                        MineableBlock block = blocksToMine[i];
                        log("Block " + block.name() + " has state IDs: " + block.stateIds + " with priority: " + blockPriorities[i]);
                    }
                    log("=== END MINING STATE DEBUG ===");
                }

                if (miner.getError() == BlockMiner.BlockMinerError.NOT_ENOUGH_BLOCKS) {
                    miner.stop();
                    miningRetries++;

                    if (miningRetries < MAX_MINING_RETRIES) {
                        int waitTime = 3000 + (miningRetries * 1000); // Wait 3s, 4s, 5s
                        log("No blocks to mine. Retrying in " + (waitTime / 1000) + " seconds... (Attempt " + miningRetries + "/" + MAX_MINING_RETRIES + ")");
                        changeState(State.MINING, waitTime);
                        return;
                    } else {
                        log("No more blocks found after " + MAX_MINING_RETRIES + " attempts. Moving to next vein.");
                        miningRetries = 0;
                        changeState(State.PATHFINDING);
                        return;
                    }
                }

                if (!miner.isRunning()) {
                    MineableBlock[] blocksToMine = getBlocksToMine();
                    int[] blockPriorities = getBlockPriority();

                    log("Starting BlockMiner with " + blocksToMine.length + " block types");

                    // Debug: Scan the area to see what blocks are actually present
                    BlockPos playerPos = PlayerUtil.getBlockStandingOn();
                    log("Player position: " + playerPos);
                    log("Scanning area around player for actual blocks:");

                    int foundBlocks = 0;
                    for (int x = -5; x <= 5; x++) {
                        for (int y = -3; y <= 3; y++) {
                            for (int z = -5; z <= 5; z++) {
                                BlockPos scanPos = playerPos.add(x, y, z);
                                int stateId = Block.getStateId(mc.theWorld.getBlockState(scanPos));
                                if (stateId != 0 && stateId != 1) { // Not air or stone
                                    String blockName = mc.theWorld.getBlockState(scanPos).getBlock().getLocalizedName();
                                    log("Found block at " + scanPos + ": " + blockName + " (StateID: " + stateId + ")");
                                    foundBlocks++;
                                    if (foundBlocks >= 10) break; // Limit output
                                }
                            }
                            if (foundBlocks >= 10) break;
                        }
                        if (foundBlocks >= 10) break;
                    }

                    if (foundBlocks == 0) {
                        log("No interesting blocks found in scanning area!");
                    }

                    miner.start(blocksToMine, miningSpeed, BlockMiner.PickaxeAbility.NONE, blockPriorities, MightyMinerConfig.miningTool);
                    return;
                }

                // Reset mining retries when successfully mining
                if (miningRetries > 0) {
                    miningRetries = 0;
                    log("Successfully started mining, reset retry counter");
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
                    Logger.sendLog("The player is already standing on a vein position. Starting mining.");
                    changeState(State.MINING);
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
                    return;
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
        List<MineableBlock> blocksList = new ArrayList<>();

        // Add debug logging to see what we're trying to mine
        log("TypeToMine contains: " + typeToMine.toString());

        // Base ores
        if (typeToMine.contains(GlaciteVeins.UMBER)) {
            blocksList.add(MineableBlock.UMBER);
            log("Added UMBER to blocks to mine");
        }
        if (typeToMine.contains(GlaciteVeins.TUNGSTEN)) {
            blocksList.add(MineableBlock.TUNGSTEN);
            log("Added TUNGSTEN to blocks to mine");
        }
        if (typeToMine.contains(GlaciteVeins.GLACITE)) {
            blocksList.add(MineableBlock.GLACITE);
            log("Added GLACITE to blocks to mine");
        }

        // Gemstones
        if (typeToMine.contains(GlaciteVeins.AMBER)) {
            blocksList.add(MineableBlock.AMBER);
            log("Added AMBER to blocks to mine");
        }
        if (typeToMine.contains(GlaciteVeins.SAPPHIRE)) {
            blocksList.add(MineableBlock.SAPPHIRE);
            log("Added SAPPHIRE to blocks to mine");
        }
        if (typeToMine.contains(GlaciteVeins.AMETHYST)) {
            blocksList.add(MineableBlock.AMETHYST);
            log("Added AMETHYST to blocks to mine");
        }
        if (typeToMine.contains(GlaciteVeins.RUBY)) {
            blocksList.add(MineableBlock.RUBY);
            log("Added RUBY to blocks to mine");
        }
        if (typeToMine.contains(GlaciteVeins.JADE)) {
            blocksList.add(MineableBlock.JADE);
            log("Added JADE to blocks to mine");
        }
        if (typeToMine.contains(GlaciteVeins.AQUAMARINE)) {
            blocksList.add(MineableBlock.AQUAMARINE);
            log("Added AQUAMARINE to blocks to mine");
        }
        if (typeToMine.contains(GlaciteVeins.ONYX)) {
            blocksList.add(MineableBlock.ONYX);
            log("Added ONYX to blocks to mine");
        }
        if (typeToMine.contains(GlaciteVeins.PERIDOT)) {
            blocksList.add(MineableBlock.PERIDOT);
            log("Added PERIDOT to blocks to mine");
        }
        if (typeToMine.contains(GlaciteVeins.CITRINE)) {
            blocksList.add(MineableBlock.CITRINE);
            log("Added CITRINE to blocks to mine");
        }
        if (typeToMine.contains(GlaciteVeins.TOPAZ)) {
            blocksList.add(MineableBlock.TOPAZ);
            log("Added TOPAZ to blocks to mine");
        }

        log("Total blocks to mine: " + blocksList.size());
        return blocksList.toArray(new MineableBlock[0]);
    }

    public int[] getBlockPriority() {
        MineableBlock[] blocksToMine = getBlocksToMine();
        int[] priorities = new int[blocksToMine.length];
        Arrays.fill(priorities, 1); // Set all priorities to 1
        log("Generated block priorities array of length: " + priorities.length + " for blocks: " + Arrays.toString(blocksToMine));
        return priorities;
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
