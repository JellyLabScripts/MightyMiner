package com.jelly.mightyminerv2.feature.impl.BlockMiner;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.event.SpawnParticleEvent;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.states.BlockMinerState;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.states.ApplyAbilityState;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.states.StartingState;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.RenderUtil;
import com.jelly.mightyminerv2.util.helper.MineableBlock;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * BlockMiner
 * <p>
 * Main controller class for automatic block mining feature.
 * Implements a state machine pattern to manage different phases of the mining process.
 * Handles mining block selection, breaking, and speed boost management.
 */
public class BlockMiner extends AbstractFeature {

    private static BlockMiner instance;

    public static BlockMiner getInstance() {
        if (instance == null) {
            instance = new BlockMiner();
        }
        return instance;
    }

    private BlockMinerState currentState;

    /**
     * Possible states for pickaxe ability.
     * AVAILABLE: Pickaxe ability can be activated
     * UNAVAILABLE: Pickaxe ability is on cooldown or is currently in effect
     */
    public enum PickaxeAbilityState {
        AVAILABLE, UNAVAILABLE,
    }
    @Getter
    @Setter
    private PickaxeAbilityState pickaxeAbilityState;
    
    @Getter
    @Setter
    private BlockMinerError error = BlockMinerError.NONE;

    public enum BlockMinerError {
        NONE,              // No error
        NOT_ENOUGH_BLOCKS, // Cannot find blocks to mine
        NO_TOOLS_AVAILABLE, // Required mining tool not found in inventory
        NO_POINTS_FOUND,    // Cannot find valid points to target on block
        NO_TARGET_BLOCKS,   // The user did not set any blocks for the miner to mine
        NO_PICKAXE_ABILITY,    // The user cannot use the pickaxe ability
    }

    /** For every pattern (Starting -> Speed) OR (Speed -> Starting), noSpeedBoostFlag adds 1
    * <p> If it detects the pattern Starting -> Speed -> Starting -> Speed (i.e., noSpeedBoostFlag == 4)
    * then NO_SPEED_BOOST is thrown
     */
    private int retryActivatePickaxeAbility;

    /** The map of the state ID of the block -> its priority */
    @Getter
    private Map<Integer, Integer> blockPriority = new HashMap<>();

    /** The BlockPos of current block being mined */
    @Getter
    @Setter
    private BlockPos targetBlockPos;

    /** Type of current block being mined */
    @Getter
    @Setter
    private Block targetBlockType;

    /** Target particle position (for precision miner) */
    @Getter
    @Setter
    private Vec3 targetParticlePos;

    /**  Mining speed modifier (affects block breaking time) */
    @Getter
    @Setter
    private int miningSpeed;

    /**  Pickaxe ability to be used */
    @Getter
    @Setter
    private PickaxeAbility pickaxeAbility;

    /**  Stop the macro automatically if it cannot find blocks within the time limit (in ms) */
    @Getter
    @Setter
    private int waitThreshold;

    @Override
    public String getName() {
        return "BlockMiner";
    }

    /**
     * Starts the BlockMiner with specified parameters. Will continue to mine {@code blocksToMine} until stop() is called
     * 
     * @param blocksToMine Array of mine-able block types to target
     * @param miningSpeed Base mining speed (higher = faster)
     * @param pickaxeAbility Users selected pickaxe ability
     * @param priority Array of priority values for block selection
     * @param miningTool Item name of the tool to use for mining
     */
    public void start(MineableBlock[] blocksToMine, final int miningSpeed, final PickaxeAbility pickaxeAbility, final int[] priority, String miningTool) {
        // Try to hold the specified mining tool if provided
        if (!miningTool.isEmpty() && !InventoryUtil.holdItem(miningTool)) {
            logError(miningTool + " not found in inventory!");
            error = BlockMinerError.NO_TOOLS_AVAILABLE;
            this.stop();
            return;
        }
        
        // Validate blocks to mine
        if (blocksToMine == null || Arrays.stream(priority).allMatch(i -> i == 0)) {
            logError("Target blocks not set!");
            error = BlockMinerError.NO_TARGET_BLOCKS;
            return;
        }
        
        // Build priority mapping for block selection
        for (int i = 0; i < blocksToMine.length; i++) {
            for (int j : blocksToMine[i].stateIds) {
                blockPriority.put(j, priority[i]);
            }
        }
        
        // Initialize parameters
        this.miningSpeed = miningSpeed - 200;  // Base adjustment to mining speed
        this.pickaxeAbility = pickaxeAbility;
        this.enabled = true;
        this.error = BlockMinerError.NONE;
        this.pickaxeAbilityState = PickaxeAbilityState.AVAILABLE;
        this.retryActivatePickaxeAbility = 0;
        targetParticlePos = null;
        
        // Initialize with starting state
        this.currentState = new StartingState();
        this.start();
    }


    @Override
    public void stop() {
        if(currentState != null)
            currentState.onEnd(this);
        super.stop();
        KeyBindUtil.releaseAllExcept();

    }

    @SubscribeEvent
    protected void onTick(TickEvent.ClientTickEvent event) {
        if (!this.enabled || mc.currentScreen != null || event.phase == TickEvent.Phase.END) {
            return;
        }

        if (currentState == null)
            return;

        BlockMinerState nextState = currentState.onTick(this);
        transitionTo(nextState);

        if (retryActivatePickaxeAbility >= 4) {
            setError(BlockMinerError.NO_PICKAXE_ABILITY);
            stop();
        }

    }

    private void transitionTo(BlockMinerState nextState){
        // Skip if no state change
        if (currentState == nextState)
            return;

        if ((currentState instanceof StartingState && nextState instanceof ApplyAbilityState)
                || (currentState instanceof ApplyAbilityState && nextState instanceof StartingState)) {
            retryActivatePickaxeAbility ++;
        }
        else {
            retryActivatePickaxeAbility = 0;
        }

        currentState.onEnd(this);
        currentState = nextState;

        if (currentState == null) {
            log("null state, returning");
            return;
        }

        currentState.onStart(this);
    }

    @SubscribeEvent
    protected void onChat(ClientChatReceivedEvent event) {
        if (event.type != 0) {
            return;
        }
        String message = event.message.getUnformattedText().toLowerCase();

        if (message.contains("is now available!")) {
            pickaxeAbilityState = PickaxeAbilityState.AVAILABLE;
        }
        if (message.contains("you used your") || message.contains("your pickaxe ability is on cooldown for")) {
            pickaxeAbilityState = PickaxeAbilityState.UNAVAILABLE;
        }
    }


    @SubscribeEvent
    public void onParticleSpawned(SpawnParticleEvent event) {
        if (!MightyMinerConfig.precisionMiner
                || event.getParticleTypes() != EnumParticleTypes.CRIT
                || targetBlockPos == null
                || mc.thePlayer.getPositionVector().squareDistanceTo(event.getPos()) >= 64) {

            targetParticlePos = null;
            return;
        }

        Vec3 particlePos = event.getPos();
        double expansion = 0.2;
        AxisAlignedBB expandedBox = new AxisAlignedBB(
                targetBlockPos.getX() - expansion, targetBlockPos.getY() - expansion, targetBlockPos.getZ() - expansion,
                targetBlockPos.getX() + 1 + expansion, targetBlockPos.getY() + 1 + expansion, targetBlockPos.getZ() + 1 + expansion
        );

        if (!expandedBox.isVecInside(particlePos)) return;

        targetParticlePos = particlePos;
    }


    @SubscribeEvent
    protected void onRender(RenderWorldLastEvent event) {
        if (this.targetParticlePos != null) {
            RenderUtil.drawPoint(this.targetParticlePos, new Color(255, 0, 0, 100));
        }
    }

    public enum PickaxeAbility {
        NONE,
        PICKOBULUS,
        MINING_SPEED_BOOST
    }
}
