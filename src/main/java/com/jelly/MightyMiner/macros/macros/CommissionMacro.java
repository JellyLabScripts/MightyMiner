package com.jelly.MightyMiner.macros.macros;

import cc.polyfrost.oneconfig.libs.checker.units.qual.C;
import cc.polyfrost.oneconfig.libs.checker.units.qual.K;
import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.config.aotv.AOTVWaypointsStructs;
import com.jelly.MightyMiner.features.FuelFilling;
import com.jelly.MightyMiner.features.MobKiller;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.*;
import com.jelly.MightyMiner.utils.BlockUtils.BlockData;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.HypixelUtils.*;
import com.jelly.MightyMiner.utils.PlayerUtils;
import com.jelly.MightyMiner.utils.Timer;
import com.jelly.MightyMiner.utils.Utils.MathUtils;
import com.jelly.MightyMiner.utils.Utils.ReflectionUtils;
import kotlinx.atomicfu.TraceBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.entity.Entity;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.Sys;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import static com.jelly.MightyMiner.handlers.KeybindHandler.*;

public class CommissionMacro extends Macro {
    private static class Target {
        public EntityLivingBase entity;
        public EntityArmorStand stand;
        public boolean worm;
        public double distance() {
            if (entity != null)
                return Minecraft.getMinecraft().thePlayer.getDistanceToEntity(entity);
            else
                return Minecraft.getMinecraft().thePlayer.getDistanceToEntity(stand);
        }

        public Target(EntityLivingBase entity, EntityArmorStand stand) {
            this.entity = entity;
            this.stand = stand;
        }

        public Target(EntityLivingBase entity, EntityArmorStand stand, boolean worm) {
            this.entity = entity;
            this.stand = stand;
            this.worm = worm;
        }
    }

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
        SETUP,
        OPEN_COMM_MENU,
        IN_COMM_MENU,
        EXIT_COMM_MENU,
        GET_COMMISSION,
        WARP_TO_FORGE,
        AT_FORGE_CHECK,
        NAVIGATING,
        COMM_SETUP,
        COMMITTING,
        NONE
    }

    public enum PigeonState {
        PIGEON,
        NO_PIGEON,
        NONE
    }

    public enum OpenPigeonState {
        HOLD_PIGEON,
        CLICK_PIGEON,
        CHECK_PIGEON,
        NONE
    }

    public enum EmissaryState {
        WARP_TO_EMISSARY,
        ROTATE_TO_EMISSARY,
        CLICK_EMISSARY,
        CHECK_EMISSARY,
        NONE
    }

    public enum WarpToEmissaryState {
        WARP_TO_FORGE,
        AT_FORGE_CHECK,
        GET_WARP_COORDINATES,
        CALCULATE_LOOK,
        LOOK,
        LOOK_CHECK,
        WARP,
        ARRIVE_CHECK,
        NONE
    }

    public enum EmissaryWarpState {
        HOLD_AOTV,
        USE_AOTV,
        NONE
    }

    public enum RotateToEmissaryState {
        FIND_EMISSARY,
        ROTATE,
        NONE
    }

    public enum ClickState {
        CLICK_DOWN,
        CLICK_UP,
        NONE
    }

    public enum NavigatingState {
        GET_WARP_COORDINATES,
        CALCULATE_LOOK,
        LOOK,
        LOOK_CHECK,
        WARP,
        ARRIVE_CHECK,
        NONE
    }

    public enum NavigatingWarpState {
        HOLD_AOTV,
        USE_AOTV,
        NONE
    }

    public enum TypeOfCommission {
        MINING_COMM,
        SLAYING_COMM,
        NONE
    }

    public enum KillingState {
        SEARCHING,
        ATTACKING,
        BLOCKED_VISION,
        KILLED,
        NONE
    }

    public enum MiningState {
        VISIBLE_BLOCKS,
        SEARCH,
        LOOK,
        MINE,
        NONE
    }

    public enum ReWarpState {
        WARP_LOBBY,
        CHECK_LOBBY,
        WARP_SB,
        CHECK_SB,
        WARP_HUB,
        CHECK_HUB,
        WARP_FORGE,
        CHECK_FORGE,
        NONE
    }

    private String currentQuest = null;

    private State comissionState = State.NONE;

    private PigeonState pigeonState = PigeonState.NONE;

    private OpenPigeonState openPigeonState = OpenPigeonState.NONE;

    private EmissaryState emissaryState = EmissaryState.NONE;

    private WarpToEmissaryState warpToEmissaryState = WarpToEmissaryState.NONE;

    private EmissaryWarpState emissaryWarpState = EmissaryWarpState.NONE;

    private RotateToEmissaryState rotateToEmissaryState = RotateToEmissaryState.NONE;

    private ClickState clickState = ClickState.NONE;

    private NavigatingState navigatingState = NavigatingState.NONE;

    private NavigatingWarpState navigatingWarpState = NavigatingWarpState.NONE;

    private TypeOfCommission typeOfCommission = TypeOfCommission.NONE;

    private KillingState killingState = KillingState.NONE;

    private MiningState miningState = MiningState.NONE;

    private ReWarpState reWarpState = ReWarpState.NONE;

    private Rotation rotation = new Rotation();

    private int pigeonSlot;

    private int aotvSlot;

    private int pickaxeSlot;

    private int weaponSlot;

    private final Timer nextActionDelay = new Timer();

    private final Timer lookFailTimer = new Timer();

    private int failedOpeningPigeonCounter = 0;

    private int failedLookingCounter = 0;

    private int failedLookingAtBlockCounter = 0;

    private int emissaryForgeWarpFailCounter = 0;

    private int failedOpeningEmissaryCounter = 0;

    private int emissaryArriveFailCounter = 0;

    private int navigatingArriveFailCounter = 0;



    ArrayList<BlockPos> warpCoordinates = null;

    private BlockPos previousWarpDestination = null;

    private BlockPos currentWarpDestination = null;

    private int warpCoordinateCounter = 0;

    private Pair<Float, Float> yawPitchGoal = null;


    private int lookTimeIncrement = 0;

    private int clickSlot = 0;


    private static String[] mobsNames = {"Ice Walker", "Goblin", "Knifethrower", "Fireslinger"};

    private final CopyOnWriteArrayList<Target> potentialTargets = new CopyOnWriteArrayList<>();

    private int scanRange = 25;

    public static Target target;

    private int stuckAtShootCounter = 0;

    private int blockedVisionCounter = 0;

    private int lookingForNewTargetCounter = 0;

    private final Timer blockedVisionDelay = new Timer();

    private final Timer afterKillDelay = new Timer();

    private final Timer attackDelay = new Timer();

    private final Timer playerNeedsToMove = new Timer();

    private final Timer unpressKey = new Timer();



    private ArrayList<Integer> priorities = new ArrayList<>();

    public static BlockPos chosenBlock = null;

    private Pair<Float, Float> rotateTo = null;

    private Timer searchCoolDown = new Timer();

    private Timer miningFor = new Timer();

    private Timer lookingFor = new Timer();

    private ArrayList<BlockPos> blacklistedBlocks = new ArrayList<>();


    private boolean regenMana = false;

    private Timer manaRegenTimer = new Timer();



    private boolean keyPressed = false;

    private int key = 0;

    private static boolean disableOnLimbo = true;

    private int warpFailCounter = 0;

    private BlockPos lastChosenBlock = null;


    private int occupiedCounter = 0;

    private static boolean isWarping = false;

    private ArrayList<BlockPos> visibleBlocks = new ArrayList<>();

    private AutoMineBaritone baritone;

    private ArrayList<ArrayList<IBlockState>> mithPriorityList = new ArrayList<>();




    @Override
    protected void onEnable() {
        // Initializing variables
        regenMana = false;
        occupiedCounter = 0;
        disableOnLimbo = MightyMiner.config.stopOnLimbo;

        // Resetting states
        isWarping = true;
        reWarpState = ReWarpState.WARP_FORGE;
        nextActionDelay.reset();

        // Check if player has pigeon
        LogUtils.debugLog("Checking if player has Pigeon");
        if (PlayerUtils.getItemInHotbarWithBlackList(true, null, "Royal Pigeon") == -1) {
            LogUtils.debugLog("No Pigeon Mode");
            pigeonState = PigeonState.NO_PIGEON;
            emissaryState = EmissaryState.WARP_TO_EMISSARY;
        } else {
            LogUtils.debugLog("Pigeon Mode");
            pigeonState = PigeonState.PIGEON;
            openPigeonState = OpenPigeonState.CLICK_PIGEON;
            pigeonSlot = PlayerUtils.getItemInHotbar("Royal Pigeon");
        }

        // Check if player has AOTV
        if (PlayerUtils.getItemInHotbarWithBlackList(true, null, "Aspect of the Void") == -1) {
            LogUtils.addMessage("You don't have an Aspect of the Void");
            LogUtils.debugLog("Player does not have Aspect of the Void");
            MacroHandler.disableScript();
            return;
        } else {
            LogUtils.debugLog("Player has Aspect of the Void");
            aotvSlot = PlayerUtils.getItemInHotbar("Aspect of the Void");
        }

        // Check if player has a Pickaxe / Drill / Gauntlet
        if (PlayerUtils.getItemInHotbarWithBlackList(true, null, "Pick", "Gauntlet", "Drill") == -1) {
            LogUtils.addMessage("You don't have a Pickaxe/Gauntlet/Drill");
            LogUtils.debugLog("Player does not have Pickaxe/Gauntlet/Drill");
            MacroHandler.disableScript();
            return;
        } else {
            LogUtils.debugLog("Player has Pickaxe/Gauntlet/Drill");
            pickaxeSlot = PlayerUtils.getItemInHotbar("Pick", "Gauntlet", "Drill");
        }

        // Check if player has a Juju / Terminator / Aurora
        if (PlayerUtils.getItemInHotbarWithBlackList(true, null, "Juju", "Terminator", "Aurora") == -1) {
            LogUtils.addMessage("You don't have a Juju/Terminator/Aurora");
            LogUtils.debugLog("Player does not have Juju/Terminator/Aurora");
            MacroHandler.disableScript();
            return;
        } else {
            LogUtils.debugLog("Player has Juju/Terminator/Aurora");
            weaponSlot = PlayerUtils.getItemInHotbar("Juju", "Terminator", "Aurora");
        }

        // Setting up
        LogUtils.debugLog("Setting up");
        comissionState = State.SETUP;
    }

    @Override
    public void onTick(TickEvent.Phase phase) {
        switch (reWarpState) {
            case WARP_SB:
                if (nextActionDelay.hasReached(2000)) {
                    // Warping to Skyblock
                    LogUtils.debugLog("Warping to Skyblock");
                    mc.thePlayer.sendChatMessage("/skyblock");

                    // Switching to next action
                    nextActionDelay.reset();
                    reWarpState = ReWarpState.CHECK_SB;
                }
                return;
            case CHECK_SB:
                if (nextActionDelay.hasReached(2000)) {
                    // Check if player arrived at skyblock
                    if (BlockUtils.getPlayerLoc().down().equals((Object) new BlockPos(-49, 199, -122))) {
                        // Arrived at Skyblock
                        LogUtils.debugLog("Arrived at Skyblock");

                        // Switching to next action
                        nextActionDelay.reset();
                        comissionState = State.SETUP;

                        // Resetting fail counter
                        warpFailCounter = 0;

                        // Resetting State
                        isWarping = false;
                        reWarpState = ReWarpState.NONE;
                    } else {
                        // Checking warp fail counter
                        if (warpFailCounter < 5) {
                            // Incrementing warp fail counter
                            warpFailCounter++;

                            // Did not arrive at Skyblock
                            LogUtils.debugLog("Did not arrive at Skyblock");

                            // Switching to previous action
                            nextActionDelay.reset();
                            reWarpState = ReWarpState.WARP_SB;
                        } else {
                            // Failed to warp to often
                            LogUtils.debugLog("Failed to warp to often");
                            MacroHandler.disableScript();
                        }
                    }
                }
                return;
            case WARP_HUB:
                if (nextActionDelay.hasReached(2000)) {
                    // Warping to hub
                    LogUtils.debugLog("Warping to hub");
                    mc.thePlayer.sendChatMessage("/hub");

                    // Switching to next action
                    nextActionDelay.reset();
                    reWarpState = ReWarpState.CHECK_HUB;
                }
                return;
            case CHECK_HUB:
                if (nextActionDelay.hasReached(2000)) {
                    // Check if player arrived at hub
                    if (BlockUtils.getPlayerLoc().down().equals((Object) new BlockPos(-3, 69, -70))) {
                        // Arrived at hub
                        LogUtils.debugLog("Arrived at hub");

                        // Resetting fail counter
                        warpFailCounter = 0;

                        // Switching to next action
                        nextActionDelay.reset();
                        isWarping = true;
                        reWarpState = ReWarpState.WARP_FORGE;
                    } else {
                        // Checking warp fail counter
                        if (warpFailCounter < 5) {
                            // Incrementing warp fail counter
                            warpFailCounter++;

                            // Did not arrive at hub
                            LogUtils.debugLog("Did not arrive at hub");

                            // Switching to previous action
                            nextActionDelay.reset();
                            isWarping = true;
                            reWarpState = ReWarpState.WARP_HUB;
                        } else {
                            // Failed to warp to often
                            LogUtils.debugLog("Failed to warp to often");
                            MacroHandler.disableScript();
                        }
                    }
                }
                return;
            case WARP_FORGE:
                if (nextActionDelay.hasReached(2000)) {
                    // Warping to hub
                    LogUtils.debugLog("Warping to forge");
                    mc.thePlayer.sendChatMessage("/warpforge");

                    // Switching to next action
                    nextActionDelay.reset();
                    reWarpState = ReWarpState.CHECK_FORGE;
                }
                return;
            case CHECK_FORGE:
                if (nextActionDelay.hasReached(2000)) {
                    // Check if player arrived at forge
                    if (BlockUtils.getPlayerLoc().down().equals((Object) new BlockPos(0, 148, -69))) {
                        // Arrived at forge
                        LogUtils.debugLog("Arrived at forge");

                        // Switching to next action
                        nextActionDelay.reset();
                        comissionState = State.SETUP;

                        // Resetting fail counter
                        warpFailCounter = 0;

                        // Resetting State
                        isWarping = false;
                        reWarpState = ReWarpState.NONE;
                    } else {
                        // Checking warp fail counter
                        if (warpFailCounter < 5) {
                            // Incrementing warp fail counter
                            warpFailCounter++;

                            // Did not arrive at forge
                            LogUtils.debugLog("Did not arrive at forge");

                            // Switching to previous action
                            nextActionDelay.reset();
                            isWarping = true;
                            reWarpState = ReWarpState.WARP_FORGE;
                        } else {
                            // Failed to warp to often
                            LogUtils.debugLog("Failed to warp to often");
                            MacroHandler.disableScript();
                        }
                    }
                }
                return;
            case NONE:
                break;
        }

        // Goblin raid at forge
        if (goblinRaidAtForge()) {
            LogUtils.debugLog("There is a goblin raid at the forge");
            isWarping = true;
            reWarpState = ReWarpState.WARP_HUB;
        }
        
        // Out of Soulflow
        if (MacroHandler.outOfSoulflow) {
            MacroHandler.outOfSoulflow = false;
            LogUtils.addMessage("Out of Soulflow");
            MacroHandler.disableScript();
        }

        // Limbo
        if (MacroHandler.kickOccurred) {
            LogUtils.debugLog("Kicked to Limbo");
            MacroHandler.kickOccurred = false;
            if (disableOnLimbo) {
                MacroHandler.disableScript();
            } else {
                MacroHandler.disableScript();
                //reWarpState = ReWarpState.WARP_SB;
            }
        }
        // Evacuation
        if (MacroHandler.restartHappening) {
            LogUtils.debugLog("Restart Happening");
            MacroHandler.restartHappening = false;
            isWarping = true;
            nextActionDelay.reset();
            reWarpState = ReWarpState.WARP_HUB;
        }

        // Regen Mana
        if (regenMana) {
            if (nextActionDelay.hasReached(1000)) {
                LogUtils.debugLog("Regenerating Mana");
                nextActionDelay.reset();
            }
            if (manaRegenTimer.hasReached(MightyMiner.config.manaRegenTime * 1000L)) {
                regenMana = false;
            }
            return;
        }

        switch (comissionState) {
            case SETUP:
                if (nextActionDelay.hasReached(1500)) {
                    // Reset all Keybindings
                    KeybindHandler.setKeyBindState(mc.gameSettings.keyBindLeft, false);
                    KeybindHandler.setKeyBindState(mc.gameSettings.keyBindRight, false);
                    KeybindHandler.setKeyBindState(mc.gameSettings.keyBindSneak, false);
                    KeybindHandler.setKeyBindState(mc.gameSettings.keyBindAttack, false);

                    // Resetting States
                    emissaryState = EmissaryState.WARP_TO_EMISSARY;
                    warpToEmissaryState = WarpToEmissaryState.WARP_TO_FORGE;
                    emissaryWarpState = EmissaryWarpState.HOLD_AOTV;
                    rotateToEmissaryState = RotateToEmissaryState.FIND_EMISSARY;
                    clickState = ClickState.CLICK_DOWN;
                    navigatingState = NavigatingState.GET_WARP_COORDINATES;
                    navigatingWarpState = NavigatingWarpState.HOLD_AOTV;
                    typeOfCommission = TypeOfCommission.NONE;
                    killingState = KillingState.SEARCHING;
                    miningState = MiningState.NONE;
                    isWarping = true;
                    regenMana = false;
                    keyPressed = false;
                    warpCoordinates = null;
                    currentWarpDestination = null;
                    previousWarpDestination = null;
                    yawPitchGoal = null;
                    lastChosenBlock = null;
                    warpCoordinateCounter = 0;
                    lookTimeIncrement = 0;
                    clickSlot = 0;
                    key = 0;

                    // Resetting Fail Counters
                    failedOpeningPigeonCounter = 0;
                    failedLookingCounter = 0;
                    failedLookingAtBlockCounter = 0;
                    emissaryForgeWarpFailCounter = 0;
                    failedOpeningEmissaryCounter = 0;
                    emissaryArriveFailCounter = 0;
                    navigatingArriveFailCounter = 0;
                    stuckAtShootCounter = 0;
                    blockedVisionCounter = 0;
                    lookingForNewTargetCounter = 0;
                    occupiedCounter = 0;

                    // Resetting Timers
                    nextActionDelay.reset();

                    // Checking if any commission is done
                    if (ComissionUtils.anyCommissionDone()) {
                        // A commission is done
                        LogUtils.debugLog("A commission is done");

                        // Switching to next action
                        comissionState = State.OPEN_COMM_MENU;
                    } else {
                        // No commission is done
                        LogUtils.debugLog("No commission is done");

                        // Switching to next action
                        comissionState = State.GET_COMMISSION;
                    }
                }
                break;
            case OPEN_COMM_MENU:
                switch (pigeonState) {
                    case PIGEON:
                        switch (openPigeonState) {
                            case HOLD_PIGEON:
                                if (nextActionDelay.hasReached(500)) {
                                    // Making Player Hold Pigeon
                                    LogUtils.debugLog("Making player hold Royal Pigeon");
                                    mc.thePlayer.inventory.currentItem = pigeonSlot;

                                    // Switching to next action
                                    nextActionDelay.reset();
                                    openPigeonState = OpenPigeonState.CLICK_PIGEON;
                                }
                                break;
                            case CLICK_PIGEON:
                                if (nextActionDelay.hasReached(500)) {
                                    // Opening Pigeon
                                    LogUtils.debugLog("Making player open Royal Pigeon");
                                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());

                                    // Switching to next action
                                    nextActionDelay.reset();
                                    openPigeonState = OpenPigeonState.CHECK_PIGEON;
                                }
                                break;
                            case CHECK_PIGEON:
                                if (nextActionDelay.hasReached(800)) {
                                    // Checking if player is in Pigeon
                                    LogUtils.debugLog("Checking if player is in Royal Pigeon");
                                    if (InventoryUtils.getInventoryName() != null && InventoryUtils.getInventoryName().contains("Commissions")) {
                                        // Player is in Pigeon
                                        LogUtils.debugLog("Player is in Royal Pigeon");

                                        // Setup next action
                                        clickSlot = 0;

                                        // Switching to next action
                                        nextActionDelay.reset();
                                        comissionState = State.IN_COMM_MENU;

                                        // Resetting State
                                        openPigeonState = OpenPigeonState.HOLD_PIGEON;
                                    } else {
                                        // Player is not in Pigeon
                                        LogUtils.debugLog("Couldn't open Royal Pigeon");

                                        // Checking how often it failed
                                        if (failedOpeningPigeonCounter > 5) {
                                            LogUtils.debugLog("Failed opening Royal Pigeon to often");

                                            // Switching to no Pigeon Mode
                                            nextActionDelay.reset();
                                            pigeonState = PigeonState.NO_PIGEON;
                                        } else {
                                            // Increasing open Royal Pigeon fail counter
                                            LogUtils.debugLog("Increasing open Royal Pigeon fail counter");
                                            failedOpeningPigeonCounter++;

                                            // Switching back to hold Pigeon action
                                            LogUtils.debugLog("Trying again to open Royal Pigeon");
                                            nextActionDelay.reset();
                                            openPigeonState = OpenPigeonState.HOLD_PIGEON;
                                        }
                                    }
                                }
                                break;
                            case NONE:
                                LogUtils.debugLog("Not in a Open Pigeon State");
                                MacroHandler.disableScript();
                                break;
                        }
                        break;
                    case NO_PIGEON:
                        switch (emissaryState) {
                            case WARP_TO_EMISSARY:
                                switch (warpToEmissaryState) {
                                    case WARP_TO_FORGE:
                                        if (nextActionDelay.hasReached(500)) {
                                            // Warping to Forge
                                            mc.thePlayer.sendChatMessage("/warpforge");

                                            // Switching to next action
                                            nextActionDelay.reset();
                                            warpToEmissaryState = WarpToEmissaryState.AT_FORGE_CHECK;
                                        }
                                        break;
                                    case AT_FORGE_CHECK:
                                        if (nextActionDelay.hasReached(1500)) {
                                            // Check if player arrived at forge
                                            if (BlockUtils.getPlayerLoc().down().equals((Object) new BlockPos(0, 148, -69))) {
                                                // Player arrived at Forge
                                                LogUtils.debugLog("Player arrived at Forge");

                                                // Resetting variables
                                                isWarping = false;

                                                // Sneaking
                                                KeybindHandler.setKeyBindState(mc.gameSettings.keyBindSneak, true);

                                                // Switching to next action
                                                nextActionDelay.reset();
                                                warpToEmissaryState = WarpToEmissaryState.GET_WARP_COORDINATES;
                                            } else {
                                                // Player did not arrive at Forge
                                                LogUtils.debugLog("Player did not arrive at Forge");

                                                // Checking emissary forge warp fail counter
                                                if (emissaryForgeWarpFailCounter > 5) {
                                                    // Failed warping to forge to often
                                                    // Possible Reasons:
                                                    // 1. No Scroll
                                                    // 2. To Laggy

                                                    LogUtils.debugLog("Failed warping to forge to often");
                                                    MacroHandler.disableScript();
                                                } else {
                                                    // Incrementing emissary forge warp fail counter
                                                    emissaryForgeWarpFailCounter++;

                                                    // Trying to warp again
                                                    nextActionDelay.reset();
                                                    isWarping = true;
                                                    warpToEmissaryState = WarpToEmissaryState.WARP_TO_FORGE;
                                                }
                                            }
                                        }
                                        break;
                                    case GET_WARP_COORDINATES:
                                        if (nextActionDelay.hasReached(100)) {
                                            // Resetting Variables
                                            currentWarpDestination = null;
                                            previousWarpDestination = null;
                                            yawPitchGoal = null;
                                            failedLookingCounter = 0;
                                            failedLookingAtBlockCounter = 0;

                                            // Getting random warp Coordinates to the Emissary
                                            LogUtils.debugLog("Getting random warp coordinates");
                                            warpCoordinates = WarpCoordinateUtils.getRandomEmissaryWarpCoordinates();

                                            // Setting first warp destination
                                            warpCoordinateCounter = 0;
                                            LogUtils.debugLog("Setting first warp destination");
                                            previousWarpDestination = new BlockPos(0, 148, -69);
                                            currentWarpDestination = warpCoordinates.get(warpCoordinateCounter);

                                            // Switching to the next action
                                            nextActionDelay.reset();
                                            warpToEmissaryState = WarpToEmissaryState.CALCULATE_LOOK;
                                        }
                                        break;
                                    case CALCULATE_LOOK:
                                        if (nextActionDelay.hasReached(50)) {
                                            // Checking if warp destination is set
                                            if (currentWarpDestination != null) {
                                                // Checking if player can look at Block
                                                LogUtils.debugLog("Checking if player can look at Block");
                                                Vec3 lookVec = VectorUtils.getRandomHittable(currentWarpDestination);
                                                if (lookVec != null) {
                                                    // Can look at Block
                                                    LogUtils.debugLog("Player can look at Block");

                                                    // Getting Yaw / Pitch from look vector
                                                    yawPitchGoal = VectorUtils.vec3ToRotation(lookVec);

                                                    // Setting up Look Variables
                                                    rotation.completed = false;
                                                    lookFailTimer.reset();

                                                    // Switching to next action
                                                    nextActionDelay.reset();
                                                    warpToEmissaryState = WarpToEmissaryState.LOOK;
                                                    lookTimeIncrement = MathUtils.randomNum(0, 100);
                                                    LogUtils.debugLog("Rotating to Yaw / Pitch");
                                                } else {
                                                    // Trying very accurate Hittable
                                                    LogUtils.debugLog("Failed with Random Hittable");
                                                    Vec3 veryAccurateLookVec = VectorUtils.getVeryAccurateHittableHitVec(currentWarpDestination);

                                                    // Checking if player can look at Block
                                                    if (veryAccurateLookVec != null) {
                                                        // Can look at Block
                                                        LogUtils.debugLog("Player can look at Block");

                                                        // Getting Yaw / Pitch from very accurate look vector
                                                        yawPitchGoal = VectorUtils.vec3ToRotation(veryAccurateLookVec);

                                                        // Setting up Look Variables
                                                        rotation.completed = false;
                                                        lookFailTimer.reset();

                                                        // Switching to next action
                                                        nextActionDelay.reset();
                                                        warpToEmissaryState = WarpToEmissaryState.LOOK;
                                                        lookTimeIncrement = MathUtils.randomNum(0, 100);
                                                        LogUtils.debugLog("Rotating to Yaw / Pitch");
                                                    } else {
                                                        // Cannot look at Block
                                                        LogUtils.debugLog("This route is not working (Maybe a Star Sentry event Blocking vision)");
                                                        MacroHandler.disableScript();
                                                    }
                                                }
                                            } else {
                                                LogUtils.debugLog("Failed Setting a warp destination");
                                                MacroHandler.disableScript();
                                            }
                                        }
                                        break;
                                    case LOOK:
                                        if (nextActionDelay.hasReached(50)) {
                                            // Checking if player Failed rotating
                                            if (lookFailTimer.hasReached(1400)) {
                                                // Failed looking at Block
                                                LogUtils.debugLog("Failed looking");

                                                // Checking failed looking counter
                                                if (failedLookingCounter > 5) {
                                                    // Failed to often
                                                    LogUtils.debugLog("Failed looking to often (Warp To Emissary)");

                                                    // Checking if player is still in spot
                                                    if (!BlockUtils.getPlayerLoc().down().equals((Object) currentWarpDestination)) {
                                                        // Fell out of position
                                                        LogUtils.debugLog("Fell out of warp position (Attacked by something)");

                                                        // Re-Warping
                                                        isWarping = true;
                                                        nextActionDelay.reset();
                                                        reWarpState = ReWarpState.WARP_HUB;
                                                    } else {
                                                        // Did not fall out of spot
                                                        LogUtils.debugLog("Probably to low FPS");
                                                        MacroHandler.disableScript();
                                                    }
                                                } else {
                                                    // Incrementing fail Counter
                                                    LogUtils.debugLog("Incrementing failed looking counter");
                                                    failedLookingCounter++;

                                                    // Calculating new look
                                                    nextActionDelay.reset();
                                                    warpToEmissaryState = WarpToEmissaryState.CALCULATE_LOOK;
                                                }
                                                return;
                                            }
                                            // Checking if rotation is finished
                                            if (AngleUtils.isDiffLowerThan(yawPitchGoal.getLeft(), yawPitchGoal.getRight(), 0.1f)) {
                                                rotation.reset();
                                                rotation.completed = true;
                                            }

                                            // Rotating to Yaw / Pitch
                                            if (!rotation.completed) {
                                                rotation.initAngleLock(yawPitchGoal.getLeft(), yawPitchGoal.getRight(), MightyMiner.config.commCameraWaypointSpeed + lookTimeIncrement);
                                            }

                                            if (!rotation.completed) return;

                                            // Completed rotation
                                            LogUtils.debugLog("Rotated to Yaw / Pitch");

                                            // Switching to next action
                                            nextActionDelay.reset();
                                            warpToEmissaryState = WarpToEmissaryState.LOOK_CHECK;
                                        }
                                        break;
                                    case LOOK_CHECK:
                                        if (nextActionDelay.hasReached(50)) {
                                            // Checking if player is looking at wanted block
                                            LogUtils.debugLog("Checking if player is looking at wanted block");
                                            MovingObjectPosition ray = mc.thePlayer.rayTrace(61, 1);

                                            if (ray.getBlockPos().equals((Object) currentWarpDestination)) {
                                                // Player is looking at wanted block
                                                LogUtils.debugLog("Player is looking at wanted block");

                                                // Switching to next action
                                                nextActionDelay.reset();
                                                warpToEmissaryState = WarpToEmissaryState.WARP;
                                            } else {
                                                // Player is not looking at wanted block
                                                LogUtils.debugLog("Player is not looking at wanted block");
                                                // Possible reasons for this
                                                // 1. Looked not accurate enough
                                                // 2. Waypoint out of reach
                                                // (3. Star Sentry event)

                                                // Checking failed looking at block counter
                                                if (failedLookingAtBlockCounter > 5) {
                                                    // Failed looking at block to often
                                                    LogUtils.debugLog("Failed looking at block to often");
                                                    MacroHandler.disableScript();
                                                } else {
                                                    // Incrementing failed looking at block counter
                                                    failedLookingAtBlockCounter++;

                                                    // Calculating new look
                                                    nextActionDelay.reset();
                                                    warpToEmissaryState = WarpToEmissaryState.CALCULATE_LOOK;
                                                }
                                            }
                                        }
                                        break;
                                    case WARP:
                                        switch (emissaryWarpState) {
                                            case HOLD_AOTV:
                                                if (nextActionDelay.hasReached(50)) {
                                                    // Making player hold Aspect of the Void
                                                    LogUtils.debugLog("Making player hold Aspect of the Void");
                                                    mc.thePlayer.inventory.currentItem = aotvSlot;

                                                    // Switching to next action
                                                    nextActionDelay.reset();
                                                    emissaryWarpState = EmissaryWarpState.USE_AOTV;
                                                }
                                                break;
                                            case USE_AOTV:
                                                if (nextActionDelay.hasReached(100)) {
                                                    // Making player use Aspect of the Void
                                                    LogUtils.debugLog("Making player use Aspect of the Void");
                                                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());

                                                    // Switching to next action
                                                    nextActionDelay.reset();
                                                    warpToEmissaryState = WarpToEmissaryState.ARRIVE_CHECK;

                                                    // Resetting State
                                                    emissaryWarpState = EmissaryWarpState.HOLD_AOTV;
                                                }
                                                break;
                                            case NONE:
                                                LogUtils.debugLog("Not in a Emissary Warp State");
                                                MacroHandler.disableScript();
                                                break;
                                        }
                                        break;
                                    case ARRIVE_CHECK:
                                        if (nextActionDelay.hasReached(300)) {
                                            // Checking if player arrived at warp destination
                                            LogUtils.debugLog("Checking if player arrived at warp destination");
                                            if (BlockUtils.getPlayerLoc().down().equals((Object) currentWarpDestination)) {
                                                // Player arrived at warp destination
                                                LogUtils.debugLog("Player arrived at warp destination");

                                                // Incrementing warp point counter
                                                warpCoordinateCounter++;

                                                // Checking if player is at last warp destination
                                                if (warpCoordinateCounter < warpCoordinates.size()) {
                                                    // Setting next warp destination
                                                    LogUtils.debugLog("Setting next warp destination");
                                                    previousWarpDestination = currentWarpDestination;
                                                    currentWarpDestination = warpCoordinates.get(warpCoordinateCounter);

                                                    // Switching to next action
                                                    nextActionDelay.reset();
                                                    warpToEmissaryState = WarpToEmissaryState.CALCULATE_LOOK;
                                                } else {
                                                    // Arrived at last warp destination
                                                    LogUtils.debugLog("Arrived at last warp destination");

                                                    // Checking if player has emissary in range
                                                    if (PlayerUtils.hasEmissaryInRadius(5)) {
                                                        // Player has emissary in radius 5
                                                        LogUtils.debugLog("Player has emissary in radius 5");

                                                        // Un-Sneaking
                                                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindSneak, false);

                                                        // Switching to next action
                                                        nextActionDelay.reset();
                                                        emissaryState = EmissaryState.ROTATE_TO_EMISSARY;

                                                        // Resetting State
                                                        isWarping = true;
                                                        warpToEmissaryState = WarpToEmissaryState.WARP_TO_FORGE;
                                                    } else {
                                                        // There is no emissary in radius 5
                                                        LogUtils.debugLog("There is no emissary in radius 5");
                                                        MacroHandler.disableScript();
                                                    }
                                                }
                                            } else {
                                                // Player didn't arrive at destination
                                                LogUtils.debugLog("Player didn't arrive at destination");

                                                // Checking Arrive Fail Counter
                                                if (navigatingArriveFailCounter > 5) {
                                                    if (BlockUtils.getPlayerLoc().down().equals((Object) previousWarpDestination)) {
                                                        if (mc.thePlayer.rayTrace(61, 1).getBlockPos().equals((Object) currentWarpDestination)) {
                                                            // Enabling mana regen
                                                            regenMana = true;
                                                            manaRegenTimer.reset();

                                                            // Switching to next action
                                                            warpToEmissaryState = WarpToEmissaryState.WARP;
                                                        } else {
                                                            // Switching to the next action
                                                            nextActionDelay.reset();
                                                            warpToEmissaryState = WarpToEmissaryState.CALCULATE_LOOK;
                                                        }
                                                    } else {
                                                        // Switching to next action
                                                        nextActionDelay.reset();
                                                        isWarping = true;
                                                        warpToEmissaryState = WarpToEmissaryState.WARP_TO_FORGE;
                                                    }
                                                } else {
                                                    // Incrementing Arrive Fail Counter
                                                    navigatingArriveFailCounter++;

                                                    // Resetting Timer
                                                    nextActionDelay.reset();
                                                }
                                            }
                                        }
                                        break;
                                    case NONE:
                                        LogUtils.debugLog("Not in a Warp To Emissary State");
                                        MacroHandler.disableScript();
                                        break;
                                }
                                break;
                            case ROTATE_TO_EMISSARY:
                                switch (rotateToEmissaryState) {
                                    case FIND_EMISSARY:
                                        if (nextActionDelay.hasReached(50)) {
                                            // Getting Position of emissary
                                            LogUtils.debugLog("Getting Position of emissary");
                                            Vec3 emissaryVec = PlayerUtils.emissaryVec(4);

                                            // Check if there is an Emissary
                                            if (emissaryVec != null) {
                                                // Emissary found
                                                LogUtils.debugLog("Emissary found");

                                                // Calculating Yaw / Pitch
                                                LogUtils.debugLog("Calculating Yaw / Pitch");
                                                BlockPos emissaryPos = new BlockPos(emissaryVec.addVector(0, -0.5, 0));
                                                yawPitchGoal = Pair.of(AngleUtils.getRequiredYawSide(emissaryPos), AngleUtils.getRequiredPitchSide(emissaryPos));

                                                // Setting up rotation
                                                LogUtils.debugLog("Setting up rotation");
                                                rotation.completed = false;
                                                lookFailTimer.reset();
                                                lookTimeIncrement = MathUtils.randomNum(0, 100);

                                                // Switching to next action
                                                nextActionDelay.reset();
                                                rotateToEmissaryState = RotateToEmissaryState.ROTATE;
                                            } else {
                                                // No Emissary found
                                                LogUtils.debugLog("No Emissary found");
                                                MacroHandler.disableScript();
                                            }
                                        }
                                        break;
                                    case ROTATE:
                                        if (nextActionDelay.hasReached(50)) {
                                            // Checking if player Failed rotating
                                            if (lookFailTimer.hasReached(1400)) {
                                                // Failed looking at Block
                                                LogUtils.debugLog("Failed looking");

                                                // Checking failed looking counter
                                                if (failedLookingCounter > 5) {
                                                    // Failed to often
                                                    LogUtils.debugLog("Failed looking to often (Find Emissary)");

                                                    // Checking if player is still in spot
                                                    if (!BlockUtils.getPlayerLoc().down().equals((Object) currentWarpDestination)) {
                                                        // Fell out of position
                                                        LogUtils.debugLog("Fell out of warp position (Attacked by something)");

                                                        // Re-Warping
                                                        nextActionDelay.reset();
                                                        isWarping = true;
                                                        reWarpState = ReWarpState.WARP_HUB;
                                                    } else {
                                                        // Did not fall out of spot
                                                        LogUtils.debugLog("Probably to low FPS");
                                                        MacroHandler.disableScript();
                                                    }
                                                } else {
                                                    // Incrementing fail Counter
                                                    LogUtils.debugLog("Incrementing failed looking counter");
                                                    failedLookingCounter++;

                                                    // Calculating new look
                                                    nextActionDelay.reset();
                                                    rotateToEmissaryState = RotateToEmissaryState.FIND_EMISSARY;
                                                }
                                                return;
                                            }
                                            // Checking if rotation is finished
                                            if (AngleUtils.isDiffLowerThan(yawPitchGoal.getLeft(), yawPitchGoal.getRight(), 0.01f)) {
                                                rotation.reset();
                                                rotation.completed = true;
                                            }

                                            // Rotating to Yaw / Pitch
                                            if (!rotation.completed) {
                                                rotation.initAngleLock(yawPitchGoal.getLeft(), yawPitchGoal.getRight(), 750 + lookTimeIncrement);
                                            }

                                            if (!rotation.completed) return;

                                            // Completed rotation
                                            LogUtils.debugLog("Rotated to Yaw / Pitch");

                                            // Switching to next action
                                            nextActionDelay.reset();
                                            emissaryState = EmissaryState.CLICK_EMISSARY;

                                            // Resetting State
                                            rotateToEmissaryState = RotateToEmissaryState.FIND_EMISSARY;
                                        }
                                        break;
                                    case NONE:
                                        LogUtils.debugLog("Not in a Rotate To Emissary State");
                                        MacroHandler.disableScript();
                                        break;
                                }
                                break;
                            case CLICK_EMISSARY:
                                switch (clickState) {
                                    case CLICK_DOWN:
                                        if (nextActionDelay.hasReached(100)) {
                                            // Pressing Attack Key
                                            LogUtils.debugLog("Pressing Attack Key");
                                            KeybindHandler.setKeyBindState(mc.gameSettings.keyBindAttack, true);

                                            // Switching to next action
                                            nextActionDelay.reset();
                                            clickState = ClickState.CLICK_UP;
                                        }
                                        break;
                                    case CLICK_UP:
                                        if (nextActionDelay.hasReached(150)) {
                                            // Releasing Attack Key
                                            LogUtils.debugLog("Releasing Attack Key");
                                            KeybindHandler.setKeyBindState(mc.gameSettings.keyBindAttack, false);

                                            // Switching to next action
                                            nextActionDelay.reset();
                                            emissaryState = EmissaryState.CHECK_EMISSARY;

                                            // Resetting State
                                            clickState = ClickState.CLICK_DOWN;
                                        }
                                        break;
                                    case NONE:
                                        LogUtils.debugLog("Not in a Click State");
                                        MacroHandler.disableScript();
                                        break;
                                }
                                break;
                            case CHECK_EMISSARY:
                                if (nextActionDelay.hasReached(500)) {
                                    // Checking if player is in Emissary
                                    LogUtils.debugLog("Checking if player is in Emissary");
                                    if (InventoryUtils.getInventoryName() != null && InventoryUtils.getInventoryName().contains("Commissions")) {
                                        // Player is in Pigeon
                                        LogUtils.debugLog("Player is in Emissary");

                                        // Setup next action
                                        clickSlot = 0;

                                        // Switching to next action
                                        nextActionDelay.reset();
                                        comissionState = State.IN_COMM_MENU;

                                        // Resetting State
                                        emissaryState = EmissaryState.WARP_TO_EMISSARY;
                                    } else {
                                        // Player is not in Emissary
                                        LogUtils.debugLog("Couldn't open Emissary");

                                        // Checking how often it failed
                                        if (failedOpeningEmissaryCounter > 5) {
                                            LogUtils.debugLog("Failed opening Emissary to often");
                                            MacroHandler.disableScript();
                                        } else {
                                            // Checking if a player is blocking emissary
                                            if (PlayerUtils.isNearPlayer(4)) {
                                                nextActionDelay.reset();

                                                // Rewarping
                                                nextActionDelay.reset();
                                                isWarping = true;
                                                reWarpState = ReWarpState.WARP_HUB;
                                                return;
                                            }
                                            // Increasing open Emissary fail counter
                                            LogUtils.debugLog("Increasing open Emissary fail counter");
                                            failedOpeningEmissaryCounter++;

                                            // Switching back to Rotate To Emissary action
                                            LogUtils.debugLog("Trying again to open Emissary");
                                            nextActionDelay.reset();
                                            emissaryState = EmissaryState.ROTATE_TO_EMISSARY;
                                        }
                                    }
                                }
                                break;
                            case NONE:
                                LogUtils.debugLog("Not in a open Pigeon State");
                                MacroHandler.disableScript();
                        }
                        break;
                    case NONE:
                        LogUtils.debugLog("Not in a Pigeon State");
                        MacroHandler.disableScript();
                        break;
                }
                break;
            case IN_COMM_MENU:
                if (nextActionDelay.hasReached(100)) {
                    // Increment Slot Count
                    clickSlot++;

                    // Check Slot Count
                    if (clickSlot < 6) {
                        // Clicking Slot
                        int slot = 10 + clickSlot;
                        LogUtils.debugLog("Clicking Slot: " + slot);
                        InventoryUtils.clickOpenContainerSlot(slot);
                        nextActionDelay.reset();
                    } else {
                        // Done clicking slots
                        LogUtils.debugLog("Done clicking slots");

                        // Switching to next action
                        nextActionDelay.reset();
                        comissionState = State.EXIT_COMM_MENU;
                    }
                }
                break;
            case EXIT_COMM_MENU:
                if (nextActionDelay.hasReached(100)) {
                    // Exiting commission menu
                    LogUtils.debugLog("Exiting commission menu");
                    mc.thePlayer.closeScreen();
                    mc.inGameHasFocus = true;
                    mc.mouseHelper.grabMouseCursor();

                    // Switching to next action
                    nextActionDelay.reset();
                    comissionState = State.GET_COMMISSION;
                }
                break;
            case GET_COMMISSION:
                if (nextActionDelay.hasReached(1000)) {
                    // Determining Commission
                    LogUtils.debugLog("Determining Commission");
                    ComissionType commission = ComissionUtils.determineComm().getKey();

                    if (commission != null) {
                        // Set current quest
                        currentQuest = commission.questName;

                        // Determined commission
                        LogUtils.debugLog("Current Commission: " + currentQuest);

                        // Switching to next action
                        nextActionDelay.reset();
                        isWarping = true;
                        comissionState = State.WARP_TO_FORGE;
                    } else {
                        // Wasn't able to determine commission
                        LogUtils.debugLog("Wasn't able to determine commission");
                        MacroHandler.disableScript();
                    }
                }
                break;
            case WARP_TO_FORGE:
                if (nextActionDelay.hasReached(500)) {
                    // Warping to Forge
                    mc.thePlayer.sendChatMessage("/warpforge");

                    // Switching to next action
                    nextActionDelay.reset();
                    comissionState = State.AT_FORGE_CHECK;
                }
                break;
            case AT_FORGE_CHECK:
                if (nextActionDelay.hasReached(1500)) {
                    // Check if player arrived at forge
                    if (BlockUtils.getPlayerLoc().down().equals((Object) new BlockPos(0, 148, -69))) {
                        // Player arrived at Forge
                        LogUtils.debugLog("Player arrived at Forge");

                        // Resetting variables
                        isWarping = false;

                        // Sneaking
                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindSneak, true);

                        // Switching to next action
                        nextActionDelay.reset();
                        comissionState = State.NAVIGATING;
                    } else {
                        // Player did not arrive at Forge
                        LogUtils.debugLog("Player did not arrive at Forge");

                        // Checking emissary forge warp fail counter
                        if (emissaryForgeWarpFailCounter > 5) {
                            // Failed warping to forge to often
                            // Possible Reasons:
                            // 1. No Scroll
                            // 2. To Laggy

                            LogUtils.debugLog("Failed warping to forge to often");
                            MacroHandler.disableScript();
                        } else {
                            // Incrementing emissary forge warp fail counter
                            emissaryForgeWarpFailCounter++;

                            // Trying to warp again
                            nextActionDelay.reset();
                            isWarping = true;
                            comissionState = State.WARP_TO_FORGE;
                        }
                    }
                }
                break;
            case NAVIGATING:
                switch (navigatingState) {
                    case GET_WARP_COORDINATES:
                        if (nextActionDelay.hasReached(100)) {
                            // Resetting Variables
                            currentWarpDestination = null;
                            previousWarpDestination = null;
                            yawPitchGoal = null;
                            failedLookingCounter = 0;
                            failedLookingAtBlockCounter = 0;

                            // Getting random warp Coordinates to the Emissary
                            LogUtils.debugLog("Getting random warp coordinates");
                            warpCoordinates = WarpCoordinateUtils.getRandomCommissionWarpCoordinates(currentQuest);

                            // Setting first warp destination
                            warpCoordinateCounter = 0;
                            LogUtils.debugLog("Setting first warp destination");
                            previousWarpDestination = new BlockPos(0, 148, -69);
                            currentWarpDestination = warpCoordinates.get(warpCoordinateCounter);

                            // Switching to the next action
                            nextActionDelay.reset();
                            navigatingState = NavigatingState.CALCULATE_LOOK;
                        }
                        break;
                    case CALCULATE_LOOK:
                        if (nextActionDelay.hasReached(50)) {
                            // Checking if warp destination is set
                            if (currentWarpDestination != null) {
                                // Checking if player can look at Block
                                LogUtils.debugLog("Checking if player can look at Block");
                                Vec3 lookVec = VectorUtils.getRandomHittable(currentWarpDestination);
                                if (lookVec != null) {
                                    // Can look at Block
                                    LogUtils.debugLog("Player can look at Block");

                                    // Getting Yaw / Pitch from look vector
                                    yawPitchGoal = VectorUtils.vec3ToRotation(lookVec);

                                    // Setting up Look Variables
                                    rotation.completed = false;
                                    lookFailTimer.reset();

                                    // Switching to next action
                                    nextActionDelay.reset();
                                    navigatingState = NavigatingState.LOOK;
                                    lookTimeIncrement = MathUtils.randomNum(0, 100);
                                    LogUtils.debugLog("Rotating to Yaw / Pitch");
                                } else {
                                    // Trying very accurate Hittable
                                    LogUtils.debugLog("Failed with Random Hittable");
                                    Vec3 veryAccurateLookVec = VectorUtils.getVeryAccurateHittableHitVec(currentWarpDestination);

                                    // Checking if player can look at Block
                                    if (veryAccurateLookVec != null) {
                                        // Can look at Block
                                        LogUtils.debugLog("Player can look at Block");

                                        // Getting Yaw / Pitch from very accurate look vector
                                        yawPitchGoal = VectorUtils.vec3ToRotation(veryAccurateLookVec);

                                        // Setting up Look Variables
                                        rotation.completed = false;
                                        lookFailTimer.reset();

                                        // Switching to next action
                                        nextActionDelay.reset();
                                        navigatingState = NavigatingState.LOOK;
                                        lookTimeIncrement = MathUtils.randomNum(0, 100);
                                        LogUtils.debugLog("Rotating to Yaw / Pitch");
                                    } else {
                                        // Cannot look at Block
                                        LogUtils.debugLog("This route is not working (Maybe a Star Sentry event Blocking vision)");
                                        MacroHandler.disableScript();
                                    }
                                }
                            } else {
                                LogUtils.debugLog("Failed Setting a warp destination");
                                MacroHandler.disableScript();
                            }
                        }
                        break;
                    case LOOK:
                        if (nextActionDelay.hasReached(50)) {
                            // Checking if player Failed rotating
                            if (lookFailTimer.hasReached(1400)) {
                                // Failed looking at Block
                                LogUtils.debugLog("Failed looking");

                                // Checking failed looking counter
                                if (failedLookingCounter > 5) {
                                    // Failed to often
                                    LogUtils.debugLog("Failed looking to often (Navigating)");
                                    // Checking if player is still in spot
                                    if (!BlockUtils.getPlayerLoc().down().equals((Object) currentWarpDestination)) {
                                        // Fell out of position
                                        LogUtils.debugLog("Fell out of warp position (Attacked by something)");

                                        // Re-Warping
                                        nextActionDelay.reset();
                                        isWarping = true;
                                        reWarpState = ReWarpState.WARP_HUB;
                                    } else {
                                        // Did not fall out of spot
                                        LogUtils.debugLog("Probably to low FPS");
                                        MacroHandler.disableScript();
                                    }
                                } else {
                                    // Incrementing fail Counter
                                    LogUtils.debugLog("Incrementing failed looking counter");
                                    failedLookingCounter++;

                                    // Calculating new look
                                    nextActionDelay.reset();
                                    navigatingState = NavigatingState.CALCULATE_LOOK;
                                }
                                return;
                            }
                            // Checking if rotation is finished
                            if (AngleUtils.isDiffLowerThan(yawPitchGoal.getLeft(), yawPitchGoal.getRight(), 0.1f)) {
                                rotation.reset();
                                rotation.completed = true;
                            }

                            // Rotating to Yaw / Pitch
                            if (!rotation.completed) {
                                rotation.initAngleLock(yawPitchGoal.getLeft(), yawPitchGoal.getRight(), MightyMiner.config.commCameraWaypointSpeed + lookTimeIncrement);
                            }

                            if (!rotation.completed) return;

                            // Completed rotation
                            LogUtils.debugLog("Rotated to Yaw / Pitch");

                            // Switching to next action
                            nextActionDelay.reset();
                            navigatingState = NavigatingState.LOOK_CHECK;
                        }
                        break;
                    case LOOK_CHECK:
                        if (nextActionDelay.hasReached(50)) {
                            // Checking if player is looking at wanted block
                            LogUtils.debugLog("Checking if player is looking at wanted block");
                            MovingObjectPosition ray = mc.thePlayer.rayTrace(61, 1);

                            if (ray.getBlockPos().equals((Object) currentWarpDestination)) {
                                // Player is looking at wanted block
                                LogUtils.debugLog("Player is looking at wanted block");

                                // Switching to next action
                                nextActionDelay.reset();
                                navigatingState = NavigatingState.WARP;
                            } else {
                                // Player is not looking at wanted block
                                LogUtils.debugLog("Player is not looking at wanted block");
                                // Possible reasons for this
                                // 1. Looked not accurate enough
                                // 2. Waypoint out of reach
                                // (3. Star Sentry event)

                                // Checking failed looking at block counter
                                if (failedLookingAtBlockCounter > 5) {
                                    // Failed looking at block to often
                                    LogUtils.debugLog("Failed looking at block to often");
                                    MacroHandler.disableScript();
                                } else {
                                    // Incrementing failed looking at block counter
                                    failedLookingAtBlockCounter++;

                                    // Calculating new look
                                    nextActionDelay.reset();
                                    navigatingState = NavigatingState.CALCULATE_LOOK;
                                }
                            }
                        }
                        break;
                    case WARP:
                        switch (navigatingWarpState) {
                            case HOLD_AOTV:
                                if (nextActionDelay.hasReached(50)) {
                                    // Making player hold Aspect of the Void
                                    LogUtils.debugLog("Making player hold Aspect of the Void");
                                    mc.thePlayer.inventory.currentItem = aotvSlot;

                                    // Switching to next action
                                    nextActionDelay.reset();
                                    navigatingWarpState = NavigatingWarpState.USE_AOTV;
                                }
                                break;
                            case USE_AOTV:
                                if (nextActionDelay.hasReached(100)) {
                                    // Making player use Aspect of the Void
                                    LogUtils.debugLog("Making player use Aspect of the Void");
                                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());

                                    // Switching to next action
                                    nextActionDelay.reset();
                                    navigatingState = NavigatingState.ARRIVE_CHECK;

                                    // Resetting State
                                    navigatingWarpState = NavigatingWarpState.HOLD_AOTV;
                                }
                                break;
                            case NONE:
                                LogUtils.debugLog("Not in a Navigating Warp State");
                                MacroHandler.disableScript();
                                break;
                        }
                        break;
                    case ARRIVE_CHECK:
                        if (nextActionDelay.hasReached(300)) {
                            // Checking if player arrived at warp destination
                            LogUtils.debugLog("Checking if player arrived at warp destination");
                            if (BlockUtils.getPlayerLoc().down().equals((Object) currentWarpDestination)) {
                                // Player arrived at warp destination
                                LogUtils.debugLog("Player arrived at warp destination");

                                // Incrementing warp point counter
                                warpCoordinateCounter++;

                                // Checking if player is at last warp destination
                                if (warpCoordinateCounter < warpCoordinates.size()) {
                                    // Setting next warp destination
                                    LogUtils.debugLog("Setting next warp destination");
                                    previousWarpDestination = currentWarpDestination;
                                    currentWarpDestination = warpCoordinates.get(warpCoordinateCounter);

                                    // Switching to next action
                                    nextActionDelay.reset();
                                    navigatingState = NavigatingState.CALCULATE_LOOK;
                                } else {
                                    // Arrived at last warp destination
                                    LogUtils.debugLog("Arrived at last warp destination");

                                    // Switching to next action
                                    comissionState = State.COMM_SETUP;

                                    // Resetting State
                                    navigatingState = NavigatingState.GET_WARP_COORDINATES;
                                }
                            } else {
                                // Player didn't arrive at destination
                                LogUtils.debugLog("Player didn't arrive at destination");

                                // Checking Arrive Fail Counter
                                if (navigatingArriveFailCounter > 5) {
                                    if (BlockUtils.getPlayerLoc().down().equals((Object) previousWarpDestination)) {
                                        if (mc.thePlayer.rayTrace(61, 1).getBlockPos().equals((Object) currentWarpDestination)) {
                                            // Enabling mana regen
                                            regenMana = true;
                                            manaRegenTimer.reset();

                                            // Switching to next action
                                            navigatingState = NavigatingState.WARP;
                                        } else {
                                            // Switching to the next action
                                            nextActionDelay.reset();
                                            navigatingState = NavigatingState.CALCULATE_LOOK;
                                        }
                                    } else {
                                        // Switching to next action
                                        nextActionDelay.reset();
                                        comissionState = State.WARP_TO_FORGE;

                                        //Resetting state
                                        navigatingState = NavigatingState.GET_WARP_COORDINATES;
                                    }
                                } else {
                                    // Incrementing Arrive Fail Counter
                                    navigatingArriveFailCounter++;

                                    // Resetting action Timer
                                    nextActionDelay.reset();
                                }
                            }
                        }
                        break;
                    case NONE:
                        LogUtils.debugLog("Not in a Navigating State");
                        MacroHandler.disableScript();
                        break;
                }
                break;
            case COMM_SETUP:
                if (nextActionDelay.hasReached(1000)) {
                    // Checking commission type
                    if (currentQuest.contains("Mithril")) {
                        // Setting up Mithril Macro
                        LogUtils.debugLog("Setting up Mithril Macro");
                        mc.thePlayer.inventory.currentItem = pickaxeSlot;
                        mithPriorityList.clear();
                        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(3));
                        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(0));
                        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(1));
                        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(2));

                        baritone = new AutoMineBaritone(getMineBehaviour());
                        typeOfCommission = TypeOfCommission.MINING_COMM;
                    } else if (currentQuest.contains("Titanium")) {
                        // Setting up Titanium Macro
                        LogUtils.debugLog("Setting up Titanium Macro");
                        mc.thePlayer.inventory.currentItem = pickaxeSlot;
                        mithPriorityList.clear();
                        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(3));
                        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(0));
                        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(1));
                        mithPriorityList.addAll(MineUtils.getMithrilColorBasedOnPriority(2));

                        baritone = new AutoMineBaritone(getMineBehaviour());
                        typeOfCommission = TypeOfCommission.MINING_COMM;
                    } else if (currentQuest.contains("Slayer")) {
                        // Setting up Slayer Macro
                        mc.thePlayer.inventory.currentItem = weaponSlot;
                        LogUtils.debugLog("Setting up Slayer Macro");
                        occupiedCounter = 0;
                        stuckAtShootCounter = 0;
                        blockedVisionCounter = 0;
                        lookingForNewTargetCounter = 0;
                        typeOfCommission = TypeOfCommission.SLAYING_COMM;
                    } else {
                        // Wasn't able to identify commission type
                        LogUtils.debugLog("Wasn't able to identify commission type");
                        MacroHandler.disableScript();
                        return;
                    }
                    comissionState = State.COMMITTING;
                }
                break;
            case COMMITTING:
                if (typeOfCommission == TypeOfCommission.MINING_COMM) {
                    if (MightyMiner.config.refuelWithAbiphone) {
                        if (FuelFilling.isRefueling()) {
                            return;
                        }
                    }
                    checkMiningSpeedBoost();
                }

                if (nextActionDelay.hasReached(2500)) {
                    // Check if Commission is finished
                    if (finishedCommission()) {
                        // Finished Commission
                        LogUtils.debugLog("Finished Commission");

                        if (baritone != null) baritone.disableBaritone();
                        KeybindHandler.resetKeybindState();

                        // Reset all Keybindings
                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindLeft, false);
                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindRight, false);
                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindSneak, false);
                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindAttack, false);

                        // Switching action to start
                        nextActionDelay.reset();
                        comissionState = State.SETUP;
                    }

                    // Check if player is in Spot
                    if (playerFellOutOfSpot(currentWarpDestination)) {
                        // Player fell out of Spot
                        LogUtils.debugLog("Player fell out of Spot");

                        if (baritone != null) baritone.disableBaritone();
                        KeybindHandler.resetKeybindState();

                        // Reset all Keybindings
                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindLeft, false);
                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindRight, false);
                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindSneak, false);
                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindAttack, false);

                        // Switching action to start
                        nextActionDelay.reset();
                        comissionState = State.SETUP;
                    }

                    // Check if spot is occupied
                    if (playerCountInRadius(5) > 0) {
                        // Spot occupied
                        LogUtils.debugLog("Spot occupied");

                        if (baritone != null) baritone.disableBaritone();
                        KeybindHandler.resetKeybindState();

                        // Incrementing occupied counter
                        occupiedCounter++;

                        // Checking occupied counter
                        if (occupiedCounter > 2) {
                            occupiedCounter = 0;
                            // ReWarp
                            isWarping = true;
                            nextActionDelay.reset();
                            reWarpState = ReWarpState.WARP_HUB;
                            return;
                        }

                        // Reset all Keybindings
                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindLeft, false);
                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindRight, false);
                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindSneak, false);
                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindAttack, false);

                        // Switching action to start
                        nextActionDelay.reset();
                        comissionState = State.SETUP;
                    }

                    // Check if too many players nearby (Slaying)
                    if (typeOfCommission.equals(TypeOfCommission.SLAYING_COMM) && playerCountInRadius(50) > 5) {
                        // Too many players nearby
                        LogUtils.debugLog("Too many players nearby");

                        if (baritone != null) baritone.disableBaritone();
                        KeybindHandler.resetKeybindState();

                        // ReWarp
                        isWarping = true;
                        nextActionDelay.reset();
                        reWarpState = ReWarpState.WARP_HUB;
                        return;
                    }
                    nextActionDelay.reset();
                }

                switch (typeOfCommission) {
                    case MINING_COMM:
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
                        break;
                    case SLAYING_COMM:
                        if (unpressKey.hasReached(250) && keyPressed) {
                            KeybindHandler.setKeyBindState(mc.gameSettings.keyBindRight, false);
                            KeybindHandler.setKeyBindState(mc.gameSettings.keyBindLeft, false);
                            keyPressed = false;

                            unpressKey.reset();
                            playerNeedsToMove.reset();
                        }
                        if (playerNeedsToMove.hasReached(1500)) {
                            KeybindHandler.setKeyBindState(mc.gameSettings.keyBindSneak, true);
                            if (key == 0) {
                                KeybindHandler.setKeyBindState(mc.gameSettings.keyBindLeft, true);
                                keyPressed = true;
                                playerNeedsToMove.reset();
                                unpressKey.reset();
                                key = 1;
                            } else if (key == 1) {
                                KeybindHandler.setKeyBindState(mc.gameSettings.keyBindRight, true);
                                keyPressed = true;
                                playerNeedsToMove.reset();
                                unpressKey.reset();
                                key = 0;
                            }
                            return;
                        }
                        switch (killingState) {
                            case SEARCHING:
                                // Clearing Target list
                                potentialTargets.clear();

                                // Getting all entities
                                List<Entity> entities = mc.theWorld.loadedEntityList.stream().filter(entity -> entity instanceof EntityArmorStand).filter(entity -> mc.thePlayer.getPositionEyes(1).distanceTo(entity.getPositionVector()) <= scanRange).collect(Collectors.toList());

                                // Filtering out players
                                List<Entity> filtered = entities.stream().filter(v -> (!v.getName().contains(mc.thePlayer.getName()) && Arrays.stream(mobsNames).anyMatch(mobsName -> {
                                    String mobsName1 = StringUtils.stripControlCodes(mobsName);
                                    String vName = StringUtils.stripControlCodes(v.getName());
                                    String vCustomNameTag = StringUtils.stripControlCodes(v.getCustomNameTag());
                                    return vName.toLowerCase().contains(mobsName1.toLowerCase()) || vCustomNameTag.toLowerCase().contains(mobsName1.toLowerCase());
                                }))).collect(Collectors.toList());
                                double distance;
                                Target closestTarget = null;

                                // Setting scan radius
                                if (currentQuest.equals("ICE_WALKER_SLAYER")) {
                                    distance = 30;
                                } else {
                                    distance = 20;
                                }

                                // Getting all relevant mobs
                                for (Entity entity : filtered) {
                                    double currentDistance;
                                    EntityArmorStand stand = (EntityArmorStand) entity;
                                    if (stand.getCustomNameTag().contains("Ice Walker") || stand.getCustomNameTag().contains("Goblin") || stand.getCustomNameTag().contains("Knifethrower") || stand.getCustomNameTag().contains("Fireslinger")) {
                                        Target target1 = new Target(null, stand, true);
                                        if (PlayerUtils.entityIsVisible(target1.stand)) {
                                            if ((target1.stand.getPosition().getY() - 10) <= mc.thePlayer.getPositionEyes(1.0f).yCoord) {
                                                if (closestTarget != null) {
                                                    currentDistance = stand.getDistanceToEntity(mc.thePlayer);
                                                    if (currentDistance < distance) {
                                                        distance = currentDistance;
                                                        closestTarget = target1;
                                                    }
                                                } else {
                                                    distance = stand.getDistanceToEntity(mc.thePlayer);
                                                    closestTarget = target1;
                                                }

                                                potentialTargets.add(target1);

                                                continue;
                                            }
                                        }
                                    }

                                    Entity target = NpcUtil.getEntityCuttingOtherEntity(stand, null);

                                    if (target == null) continue;
                                    if (NpcUtil.getEntityHp(stand) <= 0) continue;

                                    boolean visible = PlayerUtils.entityIsVisible(target);

                                    if (!visible) continue;

                                    // Getting closest target
                                    if (target instanceof EntityLivingBase) {

                                        Target target1 = new Target((EntityLivingBase) target, stand);

                                        if (closestTarget != null) {
                                            currentDistance = target.getDistanceToEntity(mc.thePlayer);
                                            if (currentDistance < distance) {
                                                distance = currentDistance;
                                                closestTarget = target1;
                                            }
                                        } else {
                                            distance = target.getDistanceToEntity(mc.thePlayer);
                                            closestTarget = target1;
                                        }

                                        potentialTargets.add(target1);
                                    }
                                }

                                // Switch to next action when there is a target
                                if (closestTarget != null && closestTarget.distance() < scanRange) {
                                    target = closestTarget;
                                    killingState = KillingState.ATTACKING;
                                }
                                break;
                            case ATTACKING:
                                // Reset anti AFK Keys
                                KeybindHandler.setKeyBindState(mc.gameSettings.keyBindLeft, false);
                                KeybindHandler.setKeyBindState(mc.gameSettings.keyBindRight, false);
                                keyPressed = false;

                                // Detecting if entity got Killed
                                if (NpcUtil.getEntityHp(target.stand) <= 0 || target.distance() > scanRange || target.stand == null || (target.entity != null && (target.entity.isDead || target.entity.getHealth() < 0.0))) {
                                    killingState = KillingState.KILLED;
                                    rotation.completed = false;
                                    stuckAtShootCounter = 0;
                                    blockedVisionCounter = 0;
                                    afterKillDelay.reset();
                                    break;
                                }

                                // Making player hold weapon
                                int weapon;
                                weapon = PlayerUtils.getItemInHotbar("Juju", "Terminator", "Bow", "Frozen Scythe", "Glacial Scythe", "Aurora");
                                if (weapon == -1) {
                                    LogUtils.debugLog("No weapon found");
                                    MacroHandler.disableScript();
                                    return;
                                }
                                mc.thePlayer.inventory.currentItem = weapon;

                                // Get entity position
                                BlockPos entityPos = target.stand.getPosition();
                                BlockPos headLevel = new BlockPos(entityPos.getX(), entityPos.getY() - 1, entityPos.getZ());

                                // Look at entity
                                if (!PlayerUtils.entityIsVisible(target.stand)) {
                                    killingState = KillingState.SEARCHING;
                                }
                                float reqYaw = AngleUtils.getRequiredYawSide(headLevel);
                                float reqPitch = AngleUtils.getRequiredPitchSide(headLevel);

                                rotation.initAngleLock(reqYaw, reqPitch, MightyMiner.config.commKillerCameraSpeed);
                                if (AngleUtils.isDiffLowerThan(reqYaw, reqPitch, 1f)) {
                                    rotation.reset();
                                    rotation.completed = true;
                                }

                                if (!rotation.completed) return;

                                // Check if target is visible
                                boolean visible = PlayerUtils.entityIsVisible(target.stand);

                                if (!visible) {
                                    LogUtils.debugLog("Something is blocking target!");
                                    blockedVisionDelay.reset();
                                    killingState = KillingState.BLOCKED_VISION;
                                    rotation.completed = false;
                                } else {
                                    if (attackDelay.hasReached(MightyMiner.config.commKillerAttackDelay)) {
                                        // Shooting target
                                        stuckAtShootCounter ++;
                                        LogUtils.debugLog("Shooting Target");
                                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindUseItem, true);
                                        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindUseItem, false);
                                        attackDelay.reset();
                                    }
                                    if (stuckAtShootCounter > 10) {
                                        // Get new target when missing target
                                        LogUtils.debugLog("Looking for a new Target");
                                        lookingForNewTargetCounter++;
                                        if (lookingForNewTargetCounter > 200) {
                                            KeybindHandler.setKeyBindState(mc.gameSettings.keyBindForward, true);
                                        }
                                        killingState = KillingState.SEARCHING;
                                    }
                                }
                                break;
                            case BLOCKED_VISION:
                                // Detect if entity got Killed
                                if (NpcUtil.getEntityHp(target.stand) <= 0 || target.distance() > MightyMiner.config.commKillerScanRange) {
                                    killingState = KillingState.KILLED;
                                    break;
                                }

                                // Blocked vision Failsafe
                                if (blockedVisionDelay.hasReached(500)) {
                                    blockedVisionCounter ++;
                                    if (blockedVisionCounter < 3) {
                                        KeybindHandler.rightClick();
                                        killingState = KillingState.ATTACKING;
                                    } else {
                                        KeybindHandler.rightClick();
                                        lookingForNewTargetCounter = 0;
                                        killingState = KillingState.SEARCHING;
                                    }
                                    break;
                                }
                                break;
                            case KILLED:
                                // Handle Kill Event
                                if (!afterKillDelay.hasReached(MightyMiner.config.commKillerAttackDelay))
                                    return;
                                lookingForNewTargetCounter = 0;
                                target = null;
                                killingState = KillingState.SEARCHING;
                                break;
                            case NONE:
                                // No Type of Commission State
                                LogUtils.debugLog("No Type of Killing State");
                                MacroHandler.disableScript();
                                break;
                        }
                        break;
                    case NONE:
                        // No Type of Commission State
                        LogUtils.debugLog("No Type of Commission State");
                        MacroHandler.disableScript();
                        break;
                }
                break;
            case NONE:
                LogUtils.debugLog("Not in a Commission State");
                MacroHandler.disableScript();
                break;
        }
    }

    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event) {

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
        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindAttack, false);
        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindLeft, false);
        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindRight, false);
        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindSneak, false);
        KeybindHandler.setKeyBindState(mc.gameSettings.keyBindForward, false);
    }

    public boolean finishedCommission() {
        if (MacroHandler.finishedCommission) {
            MacroHandler.finishedCommission = false;
            return true;
        } else {
            return false;
        }
    }

    public boolean goblinRaidAtForge() {
        for (String s: ScoreboardUtils.getScoreboardLines()) {
            if (s != null && s.contains("Event") && s.toLowerCase().contains("goblin raid")) {
                for (String s2: ScoreboardUtils.getScoreboardLines()) {
                    if (s2.contains("Zone") && s2.toLowerCase().contains("forge")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean playerFellOutOfSpot(BlockPos spot) {
        BlockPos playerPos = BlockUtils.getPlayerLoc().down();
        return Math.abs(playerPos.getX() - spot.getX()) > 3 || Math.abs(playerPos.getY() - spot.getY()) > 3 || Math.abs(playerPos.getZ() - spot.getZ()) > 3;
    }

    public int playerCountInRadius(int radius) {
        int playerCount = 0;
        for(Entity e :  mc.theWorld.getLoadedEntityList()){

            if(!(e instanceof EntityPlayer) || e == mc.thePlayer) continue;

            if(NpcUtil.isNpc(e))
                continue;

            if(e.getDistanceToEntity(mc.thePlayer) <= radius) {
                playerCount++;
            }
        }
        return playerCount;
    }

    public static boolean isWarping() {
        return isWarping;
    }

    private BaritoneConfig getMineBehaviour() {
        return new BaritoneConfig(
                MiningType.STATIC,
                MightyMiner.config.commShiftWhenMine,
                true,
                true,
                MightyMiner.config.commRotationTime,
                MightyMiner.config.commRestartTimeThreshold,
                null,
                null,
                256,
                0
        );
    }

}
