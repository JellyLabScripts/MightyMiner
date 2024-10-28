package com.jelly.mightyminerv2.macro.mithrilmacro;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.FeatureManager;
import com.jelly.mightyminerv2.feature.impl.AutoInventory;
import com.jelly.mightyminerv2.feature.impl.BlockMiner;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.MineableBlock;
import com.jelly.mightyminerv2.feature.impl.Pathfinder;
import com.jelly.mightyminerv2.handler.RotationHandler;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MithrilMacro extends AbstractMacro {
    private static MithrilMacro instance = new MithrilMacro();

    private static final Vec3 WARP_MINES_BLOCK = new Vec3(-49, 200, -122);
    private static final Vec3 STATION_MASTER_LOCATION = new Vec3(39, 200, -87);
    private static final Vec3 LOWER_MINES_LOCATION = new Vec3(84, 113, 132);

    private enum State {
        NONE, INITIALIZATION, CHECKING_STATS, GETTING_STATS, MINING,
        PATHFINDING, WAITING_ON_PATHFINDING, END
    }

    private enum NavigationState {
        CHECK_POSITION,
        WARP_TO_MINES,
        WAIT_FOR_WARP,
        GO_TO_STATION_MASTER,
        WAIT_FOR_PATHFINDER,
        LOOK_AT_STATION_MASTER,
        OPEN_STATION_MASTER,
        CLICK_LOWER_MINES,
        CLOSE_MASTER_GUI,
        WAIT_FOR_CART,
        LOOK_AT_CART,
        RIGHT_CLICK_CART,
        WAIT_FOR_TELEPORT,
        AOTV_TO_POSITION
    }

    private State currentState = State.NONE;
    private NavigationState navigationState = NavigationState.CHECK_POSITION;
    private List<String> necessaryItems = new ArrayList<>();
    private int miningSpeed = 0;
    private int miningSpeedBoost = 0;
    private MineableBlock[] blocksToMine = {};
    private int macroRetries = 0;
    private boolean isMining = false;
    private final RotationHandler rotationHandler = RotationHandler.getInstance();

    public static MithrilMacro getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return "Mithril Macro";
    }

    @Override
    public List<String> getNecessaryItems() {
        if (necessaryItems.isEmpty()) {
            necessaryItems.add(MightyMinerConfig.mithrilMiningTool);
            log("Necessary items initialized: " + necessaryItems);
        }
        return necessaryItems;
    }

    @Override
    public void onEnable() {
        log("Enabling Mithril Macro");
        resetVariables();
        this.currentState = State.INITIALIZATION;
    }

    @Override
    public void onDisable() {
        log("Disabling Mithril Macro");
        if (isMining) {
            BlockMiner.getInstance().stop();
            isMining = false;
        }
        resetVariables();
    }

    @Override
    public void onPause() {
        FeatureManager.getInstance().pauseAll();
        log("Mithril Macro paused");
    }

    @Override
    public void onResume() {
        FeatureManager.getInstance().resumeAll();
        log("Mithril Macro resumed");
    }

    private void resetVariables() {
        navigationState = NavigationState.CHECK_POSITION;
        currentState = State.NONE;
        macroRetries = 0;
        miningSpeed = 0;
        miningSpeedBoost = 0;
        necessaryItems.clear();
        isMining = false;
    }

    public void onTick(TickEvent.ClientTickEvent event) {
        if (timer.isScheduled() && !timer.passed()) return;

        log("Current state: " + currentState);
        switch (currentState) {
            case INITIALIZATION:
                handleInitializationState();
                break;
            case CHECKING_STATS:
                handleCheckingStatsState();
                break;
            case GETTING_STATS:
                handleGettingStatsState();
                break;
            case MINING:
                handleMiningState();
                break;
            case PATHFINDING:
                handlePathfindingState();
                break;
            case WAITING_ON_PATHFINDING:
                handleWaitingOnPathfindingState();
                break;
            case END:
                handleEndState();
                break;
        }
    }

    private void handleInitializationState() {
        log("Handling initialization state");
        setBlocksToMineBasedOnOreType();

        if (miningSpeed == 0 && miningSpeedBoost == 0) {
            if (!InventoryUtil.holdItem(MightyMinerConfig.mithrilMiningTool)) {
                disable("Cannot hold mining tool");
                return;
            }
            changeState(State.CHECKING_STATS);
        } else {
            changeState(State.MINING);
        }
    }

    private void handleCheckingStatsState() {
        log("Checking mining stats");
        AutoInventory.getInstance().retrieveSpeedBoost();
        changeState(State.GETTING_STATS);
    }

    private void handleGettingStatsState() {
        if (AutoInventory.getInstance().isRunning()) return;

        if (AutoInventory.getInstance().sbSucceeded()) {
            int[] sb = AutoInventory.getInstance().getSpeedBoostValues();
            miningSpeed = sb[0];
            miningSpeedBoost = sb[1];
            macroRetries = 0;
            log("Retrieved stats - Speed: " + miningSpeed + ", Boost: " + miningSpeedBoost);
            changeState(State.MINING);
        } else {
            handleSpeedBoostError();
        }
    }

    private void handleMiningState() {
        if (!checkValidLocation()) {
            changeState(State.INITIALIZATION);
            return;
        }

        if (BlockMiner.getInstance().getMithrilError() == BlockMiner.BlockMinerError.NOT_ENOUGH_BLOCKS) {
            changeState(State.PATHFINDING);
            return;
        }

        if (!isMining) {
            startMining();
        }
    }

    private void handlePathfindingState() {
        if (!Pathfinder.getInstance().isRunning()) {
            movePosition();
            changeState(State.WAITING_ON_PATHFINDING);
        }
    }

    private void handleWaitingOnPathfindingState() {
        if (!Pathfinder.getInstance().isRunning()) {
            changeState(State.MINING);
        }
    }

    private void handleEndState() {
        macroRetries = 0;
        miningSpeed = 0;
        miningSpeedBoost = 0;
        necessaryItems.clear();
        isMining = false;
        changeState(State.NONE);
    }

    private void startMining() {
        BlockMiner.getInstance().start(
                blocksToMine,
                miningSpeed,
                miningSpeedBoost,
                determinePriority(),
                MightyMinerConfig.mithrilMiningTool
        );
        isMining = true;
        log("Started mining with speed: " + miningSpeed + ", boost: " + miningSpeedBoost);

        if (BlockMiner.getInstance().getMithrilError() == BlockMiner.BlockMinerError.NOT_ENOUGH_BLOCKS) {
            log("Not enough blocks to mine. Stopping macro.");
            changeState(State.NONE);
        }
    }

    private void handleSpeedBoostError() {
        log("Handling speed boost error");
        switch (AutoInventory.getInstance().getSbError()) {
            case NONE:
                throw new IllegalStateException("AutoInventory#getSbError failed but returned NONE");
            case CANNOT_OPEN_INV:
                if (++macroRetries > 3) {
                    disable("Failed to open inventory after 3 attempts");
                } else {
                    changeState(State.INITIALIZATION);
                }
                break;
            case CANNOT_GET_VALUE:
                disable("Failed to get value. Contact the developer.");
                break;
        }
    }

    private void setBlocksToMineBasedOnOreType() {
        log("Setting blocks to mine based on ore type: " + MightyMinerConfig.oreType);
        switch (MightyMinerConfig.oreType) {
            case 0:
                blocksToMine = new MineableBlock[]{MineableBlock.DIAMOND};
                break;
            case 1:
                blocksToMine = new MineableBlock[]{MineableBlock.EMERALD};
                break;
            case 2:
                blocksToMine = new MineableBlock[]{MineableBlock.REDSTONE};
                break;
            case 3:
                blocksToMine = new MineableBlock[]{MineableBlock.LAPIS};
                break;
            case 4:
                blocksToMine = new MineableBlock[]{MineableBlock.GOLD};
                break;
            case 5:
                blocksToMine = new MineableBlock[]{MineableBlock.IRON};
                break;
            case 6:
                blocksToMine = new MineableBlock[]{MineableBlock.COAL};
                break;
            case 7:
                blocksToMine = new MineableBlock[]{
                        MineableBlock.GRAY_MITHRIL,
                        MineableBlock.GREEN_MITHRIL,
                        MineableBlock.BLUE_MITHRIL,
                        MineableBlock.TITANIUM
                };
                break;
            default:
                blocksToMine = new MineableBlock[]{};
                log("Invalid ore type selected");
                break;
        }
        log("Blocks to mine: " + Arrays.toString(blocksToMine));
    }

    private int[] determinePriority() {
        log("Determining mining priorities");
        if (MightyMinerConfig.oreType == 7) {
            return new int[]{
                    MightyMinerConfig.grayMithrilPriority,
                    MightyMinerConfig.greenMithrilPriority,
                    MightyMinerConfig.blueMithrilPriority,
                    MightyMinerConfig.titaniumPriority
            };
        }
        return new int[]{1, 1, 1, 1};
    }

    @Nullable
    private Entity getMinecart() {
        return mc.theWorld.getLoadedEntityList()
                .stream()
                .filter(entity -> entity instanceof EntityMinecart)
                .min(Comparator.comparingDouble(entity ->
                        entity.getDistanceSqToCenter(mc.thePlayer.getPosition())))
                .orElse(null);
    }

    @Nullable
    private Entity getStationMaster() {
        return mc.theWorld.getLoadedEntityList()
                .stream()
                .filter(this::isStationMaster)
                .min(Comparator.comparingDouble(entity ->
                        entity.getDistanceSqToCenter(mc.thePlayer.getPosition())))
                .orElse(null);
    }

    private boolean isStationMaster(Entity entity) {
        if (!(entity instanceof EntityOtherPlayerMP)) return false;
        EntityOtherPlayerMP player = (EntityOtherPlayerMP) entity;
        return player.getGameProfile().getProperties().containsKey("textures") &&
                player.getGameProfile().getProperties().get("textures")
                        .stream()
                        .anyMatch(p -> p.getValue().contains("ewogICJ0aW1lc3RhbXAiIDogMTYwODMxMzM3MzE3MywKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjVmMzFjMWMyMTVhNTdlOTM3ZmQ3NWFiMzU3ODJmODVlYzI0MmExYjFmOTUwYTI2YTQyYmI1ZTBhYTVjYmVkYSIKICAgIH0KICB9Cn0="));
    }

    private void movePosition() {
        // Didnt implement it currently
        log("Moving to next mining position");
    }

    private boolean checkValidLocation() {
        // Same as above
        return true;
    }

    private void changeState(State newState) {
        log("Changing state from " + currentState + " to " + newState);
        currentState = newState;
    }

    private void disable(String reason) {
        Logger.sendError("[Mithril Macro] " + reason);
        this.onDisable();
    }
}