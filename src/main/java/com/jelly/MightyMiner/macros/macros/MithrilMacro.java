package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.features.FuelFilling;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.BlockUtils.BlockData;
import com.jelly.MightyMiner.utils.HypixelUtils.MineUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

public class MithrilMacro extends Macro {

    AutoMineBaritone baritone;
    ArrayList<ArrayList<IBlockState>> mithPriorityList = new ArrayList<>();

    @Override
    protected void onEnable() {
        LogUtils.debugLog("Enabled Mithril macro checking if player is near");

        if (MightyMiner.config.playerFailsafe) {
            if (PlayerUtils.isNearPlayer(MightyMiner.config.playerRad)) {
                LogUtils.addMessage("Didnt start macro since therese a player near");
                this.enabled = false;
                onDisable();
                return;
            }
        }

        mithPriorityList.clear();
//        mithPriorityList.addAll(BlockUtils.addData(new ArrayList<Block>(){{add((Block) Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE_SMOOTH));}}));
        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority1));
        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority2));
        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority3));
        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority4));

        baritone = new AutoMineBaritone(getMineBehaviour());
    }

    @Override
    public void onTick(TickEvent.Phase phase) {
        if (!enabled) return;

        if (MightyMiner.config.refuelWithAbiphone) {
            if (FuelFilling.isRefueling()) {
                if (baritone != null && baritone.getState() != AutoMineBaritone.BaritoneState.IDLE) {
                    baritone.disableBaritone();
                }
                return;
            }
        }

        if (phase != TickEvent.Phase.START)
            return;

        switch (baritone.getState()) {
            case IDLE: case FAILED:
                baritone.mineFor(mithPriorityList);
                break;



        }

        checkMiningSpeedBoost();
    }


    @Override
    protected void onDisable() {
        if (baritone != null) baritone.disableBaritone();
        KeybindHandler.resetKeybindState();
    }

    private BaritoneConfig getMineBehaviour() {
        return new BaritoneConfig(
                MiningType.STATIC,
                MightyMiner.config.mithShiftWhenMine,
                true,
                true,
                MightyMiner.config.mithRotationTime,
                MightyMiner.config.mithRestartTimeThreshold,
                null,
                null,
                256,
                0
        );
    }
}































































/*
package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.features.FuelFilling;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.*;
import com.jelly.MightyMiner.utils.BlockUtils.BlockData;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.HypixelUtils.MineUtils;
import com.jelly.MightyMiner.utils.Utils.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ForgeBlockStateV1;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.Tessellator;



public class MithrilMacro extends Macro {
    public enum MiningState {
        SETUP,
        ROTATE,
        MINE,
        RESTART,
         NONE
    }

    private MiningState miningState = MiningState.NONE;

    private final ArrayList<Pair<BlockPos, ArrayList<Vec3>>> allVisibleBlocksWithAllHittablePos = new ArrayList<>();

    private static Pair<BlockPos, ArrayList<Vec3>> current = null;
    private static Pair<BlockPos, ArrayList<Vec3>> next = null;

    public static Triple<Vec3, Vec3, Vec3> path = null;

    private Pair<Float, Float> rotateTo = null;

    ArrayList<ArrayList<IBlockState>> priorities = new ArrayList<>();

    private final Timer searchCoolDown = new Timer();

    private final Rotation rotation = new Rotation();

    private int pickaxe = 0;

    public static ArrayList<Pair<BlockPos, ArrayList<Vec3>>> visibleBlocksWithAllHitPos = new ArrayList<>();

    @Override
    protected void onEnable() {
        pickaxe = PlayerUtils.getItemInHotbarWithBlackList(true, null, "Pick", "Drill", "Gauntlet");
        if (pickaxe == -1) {
            LogUtils.debugLog("No Pickaxe");
            MacroHandler.disableScript();
        }

        current = null;
        next = null;
        path = null;
        rotateTo = null;

        ArrayList<IBlockState> grey = new ArrayList<>();
        grey.add(Blocks.wool.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY));
        grey.add(Blocks.stained_hardened_clay.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.CYAN));
        ArrayList<IBlockState> prismarine = new ArrayList<>();
        prismarine.add(Blocks.prismarine.getDefaultState().withProperty(BlockPrismarine.VARIANT, BlockPrismarine.EnumType.ROUGH));
        prismarine.add(Blocks.prismarine.getDefaultState().withProperty(BlockPrismarine.VARIANT, BlockPrismarine.EnumType.DARK));
        prismarine.add(Blocks.prismarine.getDefaultState().withProperty(BlockPrismarine.VARIANT, BlockPrismarine.EnumType.BRICKS));
        ArrayList<IBlockState> lightBlue = new ArrayList<>();
        lightBlue.add(Blocks.wool.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.LIGHT_BLUE));
        ArrayList<IBlockState> titanium = new ArrayList<>();
        titanium.add(Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE_SMOOTH));

        priorities.add(titanium);
        priorities.add(grey);
        priorities.add(prismarine);
        priorities.add(lightBlue);


        miningState = MiningState.SETUP;
    }

    @Override
    public void onTick(TickEvent.Phase phase) {
        switch (miningState) {
            case SETUP:
                // Get all visible blocks
                if (!searchCoolDown.hasReached(1500)) return;
                ArrayList<BlockPos> visibleBlocks = new ArrayList<>();
                for (ArrayList<IBlockState> priority: priorities) {
                    if (visibleBlocks.size() < 2) {
                        visibleBlocks.addAll(VectorUtils.getAllVisibleWhitelistedBlocks(priority));
                    }
                }

                if (visibleBlocks.size() > 2) {
                    // Get hit pos
                    for (BlockPos blockPos: visibleBlocks) {
                        visibleBlocksWithAllHitPos.add(Pair.of(blockPos, VectorUtils.getAllVeryAccurateHittablePosition(blockPos)));
                    }

                    // Get player hit pos
                    Vec3 playerHitPos = mc.thePlayer.rayTrace(5, 1).hitVec;

                    // Get current
                    Triple<BlockPos, Float, ArrayList<Vec3>> closest = Triple.of(null, 99999.9f, new ArrayList<>());
                    for (Pair<BlockPos, ArrayList<Vec3>> blockWithAllHitPos: visibleBlocksWithAllHitPos) {
                        blockWithAllHitPos = Pair.of(blockWithAllHitPos.getLeft(), VectorUtils.sortClosestToFurthest(blockWithAllHitPos.getRight(), playerHitPos));

                        float distance = (float) blockWithAllHitPos.getRight().get(0).distanceTo(playerHitPos);
                        if (distance < closest.getMiddle()) {
                            closest = Triple.of(blockWithAllHitPos.getLeft(), distance, blockWithAllHitPos.getRight());
                        }
                    }

                    current = Pair.of(closest.getLeft(), closest.getRight());

                    ArrayList<Vec3> currentPathCandidates = getFirstPercentage(0.2f, current.getRight());
                    int randInt = MathUtils.randomNum(0, Math.max(currentPathCandidates.size() - 1, 0));
                    Vec3 currentPath = currentPathCandidates.get(randInt);

                    if (true) return;

                    // Get Next
                    closest = Triple.of(null, 99999.9f, new ArrayList<>());
                    for (Pair<BlockPos, ArrayList<Vec3>> blockWithAllHitPos: visibleBlocksWithAllHitPos) {
                        blockWithAllHitPos = Pair.of(blockWithAllHitPos.getLeft(), VectorUtils.sortClosestToFurthest(blockWithAllHitPos.getRight(), currentPath));

                        float distance = (float) blockWithAllHitPos.getRight().get(0).distanceTo(currentPath);
                        if (!closest.getLeft().equals((Object) current.getLeft()) && distance < closest.getMiddle()) {
                            closest = Triple.of(blockWithAllHitPos.getLeft(), distance, blockWithAllHitPos.getRight());
                        }
                    }

                    next = Pair.of(closest.getLeft(), closest.getRight());


                    // Get path

                    ArrayList<Vec3> nextPathCandidates = getFirstPercentage(0.2f, next.getRight());
                    randInt = MathUtils.randomNum(0, Math.max(nextPathCandidates.size() - 1, 0));
                    Vec3 nextPath = nextPathCandidates.get(randInt);

                    current = Pair.of(current.getLeft(), VectorUtils.sortClosestToFurthest(current.getRight(), nextPath));
                    ArrayList<Vec3> secondPathCandidates = getFirstPercentage(0.2f, current.getRight());
                    randInt = MathUtils.randomNum(0, Math.max(secondPathCandidates.size() - 1, 0));
                    Vec3 secondPath = secondPathCandidates.get(randInt);
                    path = Triple.of(currentPath, secondPath, nextPath);
                } else {
                    LogUtils.debugLog("No Mithril found");
                    searchCoolDown.reset();
                }


                break;
            case NONE:
                LogUtils.debugLog("Not in a mining state");
                MacroHandler.disableScript();
                break;
        }
    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (rotation.rotating) {
            rotation.update();
        }
    }


    @Override
    protected void onDisable() {
        miningState = MiningState.NONE;
        path = null;
    }

    public static BlockPos getNext() {
        return next.getLeft();
    }

    public static BlockPos getCurrent() {
        return current.getLeft();
    }

    private ArrayList<Vec3> getFirstPercentage(float percentage, ArrayList<Vec3> inputArrayList) {
        ArrayList<Vec3> finalArrayList = new ArrayList<>();
        for (Vec3 arrayElement: inputArrayList) {
            finalArrayList.add(arrayElement);
            if (inputArrayList.indexOf(arrayElement) > inputArrayList.size() * percentage) {
                return finalArrayList;
            }
        }
        return finalArrayList;
    }
}

 */
