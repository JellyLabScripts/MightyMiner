package com.jelly.mightyminerv2.macro.mithrilmacro;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.FeatureManager;
import com.jelly.mightyminerv2.feature.impl.AutoInventory;
import com.jelly.mightyminerv2.feature.impl.BlockMiner;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.helper.MineableBlock;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class MithrilMacro extends AbstractMacro {
    private static MithrilMacro instance = new MithrilMacro();

    public static MithrilMacro getInstance() {
        return instance;
    }

    private List<String> necessaryItems = new ArrayList<>();
    private int miningSpeed = 0;
    private int miningSpeedBoost = 0;
    private MineableBlock[] blocksToMine = {};
    private int macroRetries = 0;
    private boolean isMining = false;

    private enum State {NONE, START, CHECKING_STATS, GETTING_STATS, MINING, END}
    private MithrilMacro.State state = State.NONE;

    @Override
    public String getName() {
        return "Mithril Macro";
    }

    public List<String> getNecessaryItems() {
        if (this.necessaryItems.isEmpty()) {
            necessaryItems.add(MightyMinerConfig.mithrilMiningTool);
            log("Necessary items initialized: " + necessaryItems);
        }
        return this.necessaryItems;
    }

    @Override
    public void onEnable() {
        this.state = State.START;
        log("Mithril Macro enabled");
    }

    @Override
    public void onDisable() {
        if (isMining) {
            BlockMiner.getInstance().stop();
            isMining = false;
            log("Stopped mining on disable");
        }
        this.changeMacroState(State.END);
        log("Mithril Macro disabled");
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

    private void changeMacroState(State to) {
        log("Changing state from " + this.state + " to " + to);
        this.state = to;
    }

    public void onTick(TickEvent.ClientTickEvent event) {
        log("Current state: " + this.state);
        switch (this.state) {
            case START:
                log("Entering START state");
                setBlocksToMineBasedOnOreType();

                if (this.miningSpeed == 0 && this.miningSpeedBoost == 0) {
                    log("Mining speed and boost not set, checking inventory");
                    if (!InventoryUtil.holdItem(MightyMinerConfig.commMiningTool)) {
                        error("Cannot hold mining tool");
                        this.changeMacroState(State.NONE);
                        return;
                    }
                    this.changeMacroState(State.CHECKING_STATS);
                    log("Transitioning to CHECKING_STATS state");
                } else {
                    log("Mining speed or boost already set, skipping to MINING");
                    this.changeMacroState(State.MINING);
                }
                break;

            case CHECKING_STATS:
                log("Checking stats...");
                AutoInventory.getInstance().retrieveSpeedBoost();
                this.changeMacroState(State.GETTING_STATS);
                break;

            case GETTING_STATS:
                log("Getting stats...");
                if (AutoInventory.getInstance().isRunning()) {
                    return;
                }

                if (AutoInventory.getInstance().sbSucceeded()) {
                    int[] sb = AutoInventory.getInstance().getSpeedBoostValues();
                    this.miningSpeed = sb[0];
                    this.miningSpeedBoost = sb[1];
                    this.macroRetries = 0;
                    log("Successfully retrieved stats - MiningSpeed: " + miningSpeed + ", MiningSpeedBoost: " + miningSpeedBoost);
                    this.changeMacroState(State.MINING);
                } else {
                    log("Failed to retrieve stats, handling error");
                    handleSpeedBoostError();
                }
                break;

            case MINING:
                log("Entering MINING state");
                if (!isMining) {
                    BlockMiner.getInstance().start(
                            blocksToMine,
                            miningSpeed,
                            miningSpeedBoost,
                            determinePriority(),
                            MightyMinerConfig.commMiningTool
                    );
                    isMining = true;
                    log("Started mining with speed: " + miningSpeed + " and boost: " + miningSpeedBoost);

                    if (BlockMiner.getInstance().getMithrilError() == BlockMiner.BlockMinerError.NOT_ENOUGH_BLOCKS) {
                        log("Not enough blocks to mine. Stopping macro.");
                        this.changeMacroState(State.NONE);
                    }
                }
                break;

            case END:
                log("Entering END state");
                this.macroRetries = 0;
                this.miningSpeed = this.miningSpeedBoost = 0;
                this.necessaryItems.clear();
                this.isMining = false;
                this.changeMacroState(State.NONE);
                break;

            default:
                log("Unhandled state: " + this.state);
                break;
        }
    }

    private void handleSpeedBoostError() {
        log("Handling speed boost error...");
        switch (AutoInventory.getInstance().getSbError()) {
            case NONE:
                log("Error: AutoInventory#getSbError failed but returned NONE");
                throw new IllegalStateException("AutoInventory#getSbError failed but returned NONE");
            case CANNOT_OPEN_INV:
                log("Error: Cannot open inventory");
                if (++this.macroRetries > 3) {
                    this.changeMacroState(State.NONE);
                    error("Tried 3 times to open inventory but failed. Stopping");
                } else {
                    log("Retry attempt " + this.macroRetries);
                    this.changeMacroState(State.START);
                    log("Failed to open inventory. Retrying");
                }
                break;
            case CANNOT_GET_VALUE:
                log("Error: Failed to get value. Contact the developer.");
                this.changeMacroState(State.NONE);
                error("Failed To Get Value. Contact the developer.");
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
                log("Invalid ore type selected. No blocks to mine.");
                break;
        }
        log("Blocks to mine: " + java.util.Arrays.toString(blocksToMine));
    }

    private int[] determinePriority() {
        log("Determining priority for ore type: " + MightyMinerConfig.oreType);
        int[] priorities;

        if (MightyMinerConfig.oreType == 7) { // Mithril
            priorities = new int[]{ 
                    MightyMinerConfig.grayMithrilPriority,
                    MightyMinerConfig.greenMithrilPriority,
                    MightyMinerConfig.blueMithrilPriority,
                    MightyMinerConfig.titaniumPriority
            };
            log("Mithril priorities: " + java.util.Arrays.toString(priorities));
        } else {
            // General case priorities, can be customized further if needed
            priorities = new int[]{1, 1, 1, 1};
            log("General priorities: " + java.util.Arrays.toString(priorities));
        }

        return priorities;
    }
}
