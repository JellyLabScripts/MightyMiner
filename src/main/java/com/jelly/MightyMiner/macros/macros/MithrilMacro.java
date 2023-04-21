package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.features.FuelFilling;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils.BlockData;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.HypixelUtils.MineUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

public class MithrilMacro extends Macro {

    AutoMineBaritone baritone;

    private ArrayList<ArrayList<IBlockState>> mithPriorityList = new ArrayList<>();

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
        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(3));
        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(0));
        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(1));
        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(2));

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
                false,
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


import cc.polyfrost.oneconfig.libs.checker.units.qual.C;
import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.events.BlockChangeEvent;
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

    public enum MineState {
        START_MINE,
        CHECK_MINE,
        NONE
    }

    private MiningState miningState = MiningState.NONE;

    private MineState mineState = MineState.NONE;

    private final ArrayList<Pair<BlockPos, ArrayList<Vec3>>> allVisibleBlocksWithAllHittablePos = new ArrayList<>();

    private static Pair<BlockPos, Pair<ArrayList<Vec3>, ArrayList<Vec3>>> current = null;
    private static Pair<BlockPos, Pair<ArrayList<Vec3>, ArrayList<Vec3>>> next = null;

    public static Triple<Vec3, Vec3, Vec3> path = null;

    private Pair<Float, Float> rotateTo = null;

    ArrayList<ArrayList<IBlockState>> priorities = new ArrayList<>();

    private final Timer searchCoolDown = new Timer();

    private final Rotation rotation = new Rotation();

    private int pickaxe = 0;

    public static ArrayList<Pair<BlockPos, Pair<ArrayList<Vec3>, ArrayList<Vec3>>>> visibleBlocksWithAllHitPos = new ArrayList<>();

    private boolean brokeBlock = false;

    private boolean brokeNextBlock = false;

    private ArrayList<IBlockState> gray = new ArrayList<>();
    private ArrayList<IBlockState> green = new ArrayList<>();
    private ArrayList<IBlockState> blue = new ArrayList<>();
    private ArrayList<IBlockState> titanium = new ArrayList<>();

    @Override
    protected void onEnable() {
        pickaxe = PlayerUtils.getItemInHotbarWithBlackList(true, null, "Pick", "Drill", "Gauntlet");
        if (pickaxe == -1) {
            LogUtils.debugLog("No Pickaxe");
            MacroHandler.disableScript();
        }

        gray.add(Blocks.wool.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY));
        gray.add(Blocks.stained_hardened_clay.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.CYAN));

        green.add(Blocks.prismarine.getDefaultState().withProperty(BlockPrismarine.VARIANT, BlockPrismarine.EnumType.ROUGH));
        green.add(Blocks.prismarine.getDefaultState().withProperty(BlockPrismarine.VARIANT, BlockPrismarine.EnumType.BRICKS));
        green.add(Blocks.prismarine.getDefaultState().withProperty(BlockPrismarine.VARIANT, BlockPrismarine.EnumType.DARK));

        blue.add(Blocks.wool.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.LIGHT_BLUE));

        titanium.add(Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE_SMOOTH));

        rotation.reset();

        current = null;
        next = null;
        path = null;
        rotateTo = null;

        brokeBlock = false;
        brokeNextBlock = false;

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
                        visibleBlocks.addAll(VectorUtils.getAllVisibleWhitelistedBlocks(priority, false));
                    }
                }

                if (visibleBlocks.size() >= 2) {
                    // Get hit pos
                    visibleBlocksWithAllHitPos.clear();
                    for (BlockPos blockPos: visibleBlocks) {
                        visibleBlocksWithAllHitPos.add(Pair.of(blockPos, VectorUtils.getAllVeryAccurateHits(blockPos)));
                    }

                    // Get player hit pos
                    Vec3 playerHitPos = mc.thePlayer.rayTrace(5, 1).hitVec;

                    // Get current
                    Triple<BlockPos, Float, Pair<ArrayList<Vec3>, ArrayList<Vec3>>> closest = Triple.of(null, 99999.9f, Pair.of(new ArrayList<>(), new ArrayList<>()));
                    for (Pair<BlockPos, Pair<ArrayList<Vec3>, ArrayList<Vec3>>> blockWithAllHitPos: visibleBlocksWithAllHitPos) {
                        blockWithAllHitPos = Pair.of(blockWithAllHitPos.getLeft(), Pair.of(VectorUtils.sortClosestToFurthest(blockWithAllHitPos.getRight().getLeft(), playerHitPos), blockWithAllHitPos.getRight().getRight()));

                        float distance = (float) blockWithAllHitPos.getRight().getLeft().get(0).distanceTo(playerHitPos);
                        if (distance < closest.getMiddle()) {
                            closest = Triple.of(blockWithAllHitPos.getLeft(), distance, blockWithAllHitPos.getRight());
                        }
                    }

                    current = Pair.of(closest.getLeft(), closest.getRight());

                    ArrayList<Vec3> currentPathCandidates = getFromPercentageToPercentage(0.05f, 0.2f, current.getRight().getLeft());
                    int randInt = MathUtils.randomNum(0, Math.max(currentPathCandidates.size() - 1, 0));
                    Vec3 currentPath = currentPathCandidates.get(randInt);

                    // Get Next
                    closest = Triple.of(null, 99999.9f, Pair.of(new ArrayList<>(), new ArrayList<>()));
                    for (Pair<BlockPos, Pair<ArrayList<Vec3>, ArrayList<Vec3>>> blockWithAllHitPos: visibleBlocksWithAllHitPos) {
                        blockWithAllHitPos = Pair.of(blockWithAllHitPos.getLeft(), Pair.of(VectorUtils.sortClosestToFurthest(blockWithAllHitPos.getRight().getLeft(), currentPath), blockWithAllHitPos.getRight().getRight()));

                        float distance = (float) blockWithAllHitPos.getRight().getLeft().get(0).distanceTo(currentPath);
                        if (!blockWithAllHitPos.getLeft().equals((Object) current.getLeft()) && distance < closest.getMiddle()) {
                            closest = Triple.of(blockWithAllHitPos.getLeft(), distance, blockWithAllHitPos.getRight());
                        }


                    }

                    next = Pair.of(closest.getLeft(), closest.getRight());

                    // Get path

                    ArrayList<Vec3> nextPathCandidates = getFirstPercentage(0.1f, next.getRight().getLeft());
                    randInt = MathUtils.randomNum(0, Math.max(nextPathCandidates.size() - 1, 0));
                    Vec3 nextPath = nextPathCandidates.get(randInt);

                    current = Pair.of(current.getLeft(), Pair.of(VectorUtils.sortClosestToFurthest(current.getRight().getLeft(), nextPath), current.getRight().getRight()));
                    ArrayList<Vec3> secondPathCandidates = getFirstPercentage(0.1f, current.getRight().getLeft());
                    randInt = MathUtils.randomNum(0, Math.max(secondPathCandidates.size() - 1, 0));
                    Vec3 secondPath = secondPathCandidates.get(randInt);
                    secondPathCandidates.clear();
                    for (Vec3 hitPos: current.getRight().getRight()) {
                        if (!VectorUtils.anyHitOnVecNotHittable(current.getLeft(), currentPath, hitPos, 5)) {
                            secondPathCandidates.add(hitPos);
                        }
                    }
                    if (secondPathCandidates.size() > 0) {
                        ArrayList<Vec3> hittableSecondPathCandidates = getFromPercentageToPercentage(0.05f, 0.1f, VectorUtils.sortClosestToFurthest(secondPathCandidates, nextPath));
                        randInt = MathUtils.randomNum(0, Math.max(hittableSecondPathCandidates.size() - 1, 0));
                        secondPath = hittableSecondPathCandidates.get(randInt);

                        // Get new next
                        closest = Triple.of(null, 99999.9f, Pair.of(new ArrayList<>(), new ArrayList<>()));
                        for (Pair<BlockPos, Pair<ArrayList<Vec3>, ArrayList<Vec3>>> blockWithAllHitPos: visibleBlocksWithAllHitPos) {
                            blockWithAllHitPos = Pair.of(blockWithAllHitPos.getLeft(), Pair.of(VectorUtils.sortClosestToFurthest(blockWithAllHitPos.getRight().getLeft(), secondPath), blockWithAllHitPos.getRight().getRight()));

                            float distance = (float) blockWithAllHitPos.getRight().getLeft().get(0).distanceTo(secondPath);
                            if (!blockWithAllHitPos.getLeft().equals((Object) current.getLeft()) && distance < closest.getMiddle()) {
                                closest = Triple.of(blockWithAllHitPos.getLeft(), distance, blockWithAllHitPos.getRight());
                            }
                        }

                        next = Pair.of(closest.getLeft(), closest.getRight());

                        nextPathCandidates = getFromPercentageToPercentage(0.05f, 0.1f, next.getRight().getLeft());
                        randInt = MathUtils.randomNum(0, Math.max(nextPathCandidates.size() - 1, 0));
                        nextPath = nextPathCandidates.get(randInt);
                    } else {
                        LogUtils.debugLog("No Path");
                    }

                    path = Triple.of(currentPath, secondPath, nextPath);

                    // Switching to next action
                    miningState = MiningState.ROTATE;
                    rotation.reset();

                } else {
                    LogUtils.debugLog("No Mithril found");
                    searchCoolDown.reset();
                }


                break;
            case ROTATE:
                if (AngleUtils.isDiffLowerThan(VectorUtils.vec3ToRotation(path.getLeft()).getLeft(), VectorUtils.vec3ToRotation(path.getLeft()).getRight(), 0.1f) || rotation.completed) {
                    rotation.reset();

                    // Switching to next action
                    mineState = MineState.START_MINE;
                    miningState = MiningState.MINE;
                }

                if (!rotation.completed) {
                    rotation.initAngleLock(VectorUtils.vec3ToRotation(path.getLeft()).getLeft(), VectorUtils.vec3ToRotation(path.getLeft()).getRight(), 700);
                }

                break;
            case MINE:
                switch (mineState) {
                    case START_MINE:
                        mc.thePlayer.inventory.currentItem = pickaxe;
                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindAttack, true);

                        // Switching to next action
                        mineState = MineState.CHECK_MINE;
                        rotation.reset();
                        break;
                    case CHECK_MINE:
                        if (AngleUtils.isDiffLowerThan(VectorUtils.vec3ToRotation(path.getMiddle()).getLeft(), VectorUtils.vec3ToRotation(path.getMiddle()).getRight(), 0.1f) || rotation.completed) {
                            rotation.reset();
                        }

                        if (!rotation.completed) {
                            IBlockState blockState = BlockUtils.getBlockState(current.getLeft());
                            float blockHardness = gray.contains(blockState) ? 500: green.contains(blockState) ? 800: blue.contains(blockState) ? 1500: 2000;
                            float miningSpeed = 985;
                            float top = blockHardness * 30;
                            float bottom = miningSpeed * 20;
                            rotation.initAngleLock(VectorUtils.vec3ToRotation(path.getMiddle()).getLeft(), VectorUtils.vec3ToRotation(path.getMiddle()).getRight(), (int) (1000 * (top / bottom) + 200));
                        }

                        if (brokeBlock) {
                            KeybindHandler.setKeyBindState(mc.gameSettings.keyBindAttack, false);
                            brokeBlock = false;
                            miningState = MiningState.RESTART;
                            rotation.reset();
                        }
                        break;
                    case NONE:
                        LogUtils.debugLog("Not in a mining State");
                        MacroHandler.disableScript();
                        break;
                }
                break;
            case RESTART:
                if (!searchCoolDown.hasReached(1500)) return;
                if (brokeNextBlock) {
                    brokeNextBlock = false;
                    miningState = MiningState.SETUP;
                } else {
                    // Get all visible blocks
                    if (!searchCoolDown.hasReached(1500)) return;
                    ArrayList<BlockPos> visibleBlocksRestart = new ArrayList<>();
                    for (ArrayList<IBlockState> priority: priorities) {
                        if (visibleBlocksRestart.size() < 2) {
                            visibleBlocksRestart.addAll(VectorUtils.getAllVisibleWhitelistedBlocks(priority, false));
                        }
                    }

                    if (visibleBlocksRestart.size() >= 2) {
                        // Get hit pos
                        visibleBlocksWithAllHitPos.clear();
                        for (BlockPos blockPos: visibleBlocksRestart) {
                            visibleBlocksWithAllHitPos.add(Pair.of(blockPos, VectorUtils.getAllVeryAccurateHits(blockPos)));
                        }
                        current = next;
                        Vec3 currentPath = path.getRight();


                        // Get Next
                        Triple<BlockPos, Float, Pair<ArrayList<Vec3>, ArrayList<Vec3>>> closest = Triple.of(null, 99999.9f, Pair.of(new ArrayList<>(), new ArrayList<>()));
                        for (Pair<BlockPos, Pair<ArrayList<Vec3>, ArrayList<Vec3>>> blockWithAllHitPos: visibleBlocksWithAllHitPos) {
                            blockWithAllHitPos = Pair.of(blockWithAllHitPos.getLeft(), Pair.of(VectorUtils.sortClosestToFurthest(blockWithAllHitPos.getRight().getLeft(), currentPath), blockWithAllHitPos.getRight().getRight()));

                            float distance = (float) blockWithAllHitPos.getRight().getLeft().get(0).distanceTo(currentPath);
                            if (!blockWithAllHitPos.getLeft().equals((Object) current.getLeft()) && distance < closest.getMiddle()) {
                                closest = Triple.of(blockWithAllHitPos.getLeft(), distance, blockWithAllHitPos.getRight());
                            }


                        }

                        next = Pair.of(closest.getLeft(), closest.getRight());

                        // Get path

                        ArrayList<Vec3> nextPathCandidates = getFirstPercentage(0.1f, next.getRight().getLeft());
                        int randInt = MathUtils.randomNum(0, Math.max(nextPathCandidates.size() - 1, 0));
                        Vec3 nextPath = nextPathCandidates.get(randInt);

                        current = Pair.of(current.getLeft(), Pair.of(VectorUtils.sortClosestToFurthest(current.getRight().getLeft(), nextPath), current.getRight().getRight()));
                        ArrayList<Vec3> secondPathCandidates = getFirstPercentage(0.1f, current.getRight().getLeft());
                        randInt = MathUtils.randomNum(0, Math.max(secondPathCandidates.size() - 1, 0));
                        Vec3 secondPath = secondPathCandidates.get(randInt);
                        secondPathCandidates.clear();
                        for (Vec3 hitPos: current.getRight().getRight()) {
                            if (!VectorUtils.anyHitOnVecNotHittable(current.getLeft(), currentPath, hitPos, 5)) {
                                secondPathCandidates.add(hitPos);
                            }
                        }
                        if (secondPathCandidates.size() > 0) {
                            ArrayList<Vec3> hittableSecondPathCandidates = getFromPercentageToPercentage(0.05f, 0.1f, VectorUtils.sortClosestToFurthest(secondPathCandidates, nextPath));
                            randInt = MathUtils.randomNum(0, Math.max(hittableSecondPathCandidates.size() - 1, 0));
                            secondPath = hittableSecondPathCandidates.get(randInt);

                            // Get new next
                            closest = Triple.of(null, 99999.9f, Pair.of(new ArrayList<>(), new ArrayList<>()));
                            for (Pair<BlockPos, Pair<ArrayList<Vec3>, ArrayList<Vec3>>> blockWithAllHitPos: visibleBlocksWithAllHitPos) {
                                blockWithAllHitPos = Pair.of(blockWithAllHitPos.getLeft(), Pair.of(VectorUtils.sortClosestToFurthest(blockWithAllHitPos.getRight().getLeft(), secondPath), blockWithAllHitPos.getRight().getRight()));

                                float distance = (float) blockWithAllHitPos.getRight().getLeft().get(0).distanceTo(secondPath);
                                if (!blockWithAllHitPos.getLeft().equals((Object) current.getLeft()) && distance < closest.getMiddle()) {
                                    closest = Triple.of(blockWithAllHitPos.getLeft(), distance, blockWithAllHitPos.getRight());
                                }
                            }

                            next = Pair.of(closest.getLeft(), closest.getRight());

                            nextPathCandidates = getFromPercentageToPercentage(0.05f, 0.1f, next.getRight().getLeft());
                            randInt = MathUtils.randomNum(0, Math.max(nextPathCandidates.size() - 1, 0));
                            nextPath = nextPathCandidates.get(randInt);
                        } else {
                            LogUtils.debugLog("No Path");
                        }

                        path = Triple.of(currentPath, secondPath, nextPath);

                        // Switching to next action
                        miningState = MiningState.ROTATE;
                        rotation.reset();

                    } else {
                        LogUtils.debugLog("No Mithril found");
                        searchCoolDown.reset();
                    }


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
    public void onBlockChange(BlockChangeEvent event) {
        if (current.getLeft() != null && next.getLeft() != null) {
            if (event.pos.equals(current.getLeft())) {
                brokeBlock = true;
            }
            if (event.pos.equals(next.getLeft())) {
                brokeNextBlock = true;
            }
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

    private ArrayList<Vec3> getFromPercentageToPercentage(float percentageStart, float percentageEnd, ArrayList<Vec3> inputArrayList) {
        ArrayList<Vec3> finalArrayList = new ArrayList<>();
        for (Vec3 arrayElement: inputArrayList) {
            if (inputArrayList.indexOf(arrayElement) > Math.floor(inputArrayList.size() * percentageStart)) {
                finalArrayList.add(arrayElement);
                if (inputArrayList.indexOf(arrayElement) > inputArrayList.size() * percentageEnd) {
                    return finalArrayList;
                }
            }
        }
        return finalArrayList;
    }
}

 */

