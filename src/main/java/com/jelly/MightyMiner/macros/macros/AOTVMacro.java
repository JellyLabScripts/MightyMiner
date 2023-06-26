package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.events.BlockChangeEvent;
import com.jelly.MightyMiner.features.FuelFilling;
import com.jelly.MightyMiner.features.MobKiller;
import com.jelly.MightyMiner.config.aotv.AOTVWaypointsStructs;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.*;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.HypixelUtils.MineUtils;
import com.jelly.MightyMiner.utils.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

public class AOTVMacro extends Macro {

    private final Minecraft mc = Minecraft.getMinecraft();

    private int currentWaypoint = -1;

    private final Timer tpStuckTimer = new Timer();
    private final Timer timeBetweenLastWaypoint = new Timer();
    private final Timer waitForVeinsTimer = new Timer();
    private boolean tooFastTp = false;
    private boolean firstTp = true;
    private boolean tping = false;

    private boolean killing = false;
    private boolean refueling = false;

    private AutoMineBaritone baritone;

    public enum State {
        MINING,
        WARPING
    }

    private State currentState = State.MINING;
    private final Rotation rotation = new Rotation();
    private static final Timer waitAfterKillTimer = new Timer();

    private boolean reScan = true;

    private enum MiningSpeed {
        OPEN_SB_MENU,
        CHECK_SB_MENU,
        GET_LORE,
        NONE
    }

    private enum UseItem {
        OPEN_INVENTORY,
        CHECK_INVENTORY,
        USE_ITEM,
        NONE
    }

    private MiningSpeed miningSpeed = MiningSpeed.NONE;

    private UseItem useItem = UseItem.NONE;

    private final Timer miningSpeedDelay = new Timer();

    private int pickaxe = -1;


    @Override
    public void onEnable() {
        MacroHandler.miningSpeedActive = false;
        if (MightyMiner.aotvWaypoints.getSelectedRoute() == null) {
            LogUtils.addMessage("No route selected!");
            this.toggle();
            return;
        }

        ArrayList<AOTVWaypointsStructs.Waypoint> Waypoints = MightyMiner.aotvWaypoints.getSelectedRoute().waypoints;

        BlockPos currentPos = BlockUtils.getPlayerLoc().down();

        int miningTool = PlayerUtils.getItemInHotbarWithBlackList(true, null, "Pickaxe", "Gauntlet", "Drill");

        if (miningTool == -1) {
            LogUtils.addMessage("AOTV Macro (Experimental) - You don't have a mining tool!");
            this.toggle();
            return;
        }

        int voidTool = PlayerUtils.getItemInHotbarWithBlackList(true, null, "Void");

        if (voidTool == -1) {
            LogUtils.addMessage("AOTV Macro (Experimental) - You don't have a Aspect of the Void!");
            this.toggle();
            return;
        }

        for (int i = 0; i < Waypoints.size(); i++) {
            BlockPos waypoint = new BlockPos(Waypoints.get(i).x, Waypoints.get(i).y, Waypoints.get(i).z);
            if (waypoint.equals(currentPos)) {
                currentWaypoint = i;
                break;
            }
        }

        if (currentWaypoint == -1) {
            LogUtils.addMessage("AOTV Macro (Experimental) - You are not at a valid waypoint!");
            this.toggle();
            return;
        }

        if (MightyMiner.config.aotvKillYogs) {
            MightyMiner.mobKiller.toggle();
            MobKiller.setMobsNames(false, "Yog");
            if (MightyMiner.config.useHyperionUnderPlayer) {
                MobKiller.scanRange = Math.min(5, MightyMiner.config.mobKillerScanRange);
            } else {
                MobKiller.scanRange = MightyMiner.config.mobKillerScanRange;
            }
            MobKiller.isToggled = true;
        }

        timeBetweenLastWaypoint.reset();
        tooFastTp = false;
        firstTp = true;
        reScan = true;
        tping = false;
        baritone = new AutoMineBaritone(getAutoMineConfig());
        currentState = State.MINING;
        waitAfterKillTimer.reset();

        pickaxe = PlayerUtils.getItemInHotbar(false, "Pick", "Gauntlet", "Drill");
        if (pickaxe == -1) {
            LogUtils.debugLog("No Pickaxe");
            MacroHandler.disableScript();
            return;
        }

        if (MightyMiner.config.fastMine) {
            miningSpeed = MiningSpeed.OPEN_SB_MENU;
            useItem = UseItem.OPEN_INVENTORY;
            miningSpeedDelay.reset();
        } else {
            miningSpeed = MiningSpeed.NONE;
        }
    }

    @Override
    public void onDisable() {
        currentWaypoint = -1;
        tpStuckTimer.reset();
        timeBetweenLastWaypoint.reset();
        waitForVeinsTimer.reset();
        rotation.reset();
        rotation.completed = true;
        KeybindHandler.resetKeybindState();
        if (baritone != null) // Jelly, stop making the same mistake over and over 💀
            baritone.disableBaritone();
        if (MobKiller.isToggled)
            MightyMiner.mobKiller.toggle();
        MobKiller.isToggled = false;
    }

    @SubscribeEvent
    public void onBlockChange(BlockChangeEvent event) {
        if (!isEnabled()) return;

        BlockPos pos = event.pos;

        if (event.old.getBlock() == Blocks.cobblestone) {
            if (event.update.getBlock() == Blocks.air) {
                ArrayList<AOTVWaypointsStructs.Waypoint> Waypoints = MightyMiner.aotvWaypoints.getSelectedRoute().waypoints;

                AOTVWaypointsStructs.Waypoint wp = Waypoints.stream().filter(waypoint -> waypoint.x == pos.getX() && waypoint.y == pos.getY() && waypoint.z == pos.getZ()).findFirst().orElse(null);
                if (wp != null) {
                    LogUtils.addMessage("AOTV Macro - Cobblestone at waypoint " + EnumChatFormatting.BOLD + wp.name + EnumChatFormatting.RESET + EnumChatFormatting.RED + " has been destroyed!");

                    if (MightyMiner.config.stopIfCobblestoneDestroyed) {
                        this.toggle();
                        PlayerUtils.sendPingAlert();
                    }
                }
            }
        }
    }


    @Override
    public void onTick(TickEvent.Phase phase) {
        if (phase == TickEvent.Phase.END) return;
        if (!this.isEnabled()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (MightyMiner.aotvWaypoints.getSelectedRoute() == null) return;

        switch (miningSpeed) {
            case OPEN_SB_MENU:
                switch (useItem) {
                    case OPEN_INVENTORY:
                        if (miningSpeedDelay.hasReached(100)) {
                            mc.thePlayer.inventory.currentItem = pickaxe;

                            InventoryUtils.openInventory();

                            miningSpeedDelay.reset();
                            useItem = UseItem.CHECK_INVENTORY;
                        }
                        break;
                    case CHECK_INVENTORY:
                        if (miningSpeedDelay.hasReached(500)) {
                            if (mc.currentScreen instanceof GuiInventory) {
                                useItem = UseItem.USE_ITEM;
                            } else {
                                useItem = UseItem.OPEN_INVENTORY;
                            }

                            miningSpeedDelay.reset();
                        }
                        break;
                    case USE_ITEM:
                        if (miningSpeedDelay.hasReached(300)) {
                            InventoryUtils.clickOpenContainerSlot(44);

                            miningSpeedDelay.reset();
                            miningSpeed = MiningSpeed.CHECK_SB_MENU;
                            useItem = UseItem.OPEN_INVENTORY;
                        }
                        break;
                    case NONE:
                        LogUtils.debugLog("Not in a use item state");
                        MacroHandler.disableScript();
                        break;
                }
                break;
            case CHECK_SB_MENU:
                if (miningSpeedDelay.hasReached(500)) {
                    if (InventoryUtils.getInventoryName() != null && InventoryUtils.getInventoryName().contains("SkyBlock Menu")) {
                        miningSpeedDelay.reset();
                        miningSpeed = MiningSpeed.GET_LORE;
                    } else {
                        miningSpeedDelay.reset();
                        useItem = UseItem.OPEN_INVENTORY;
                        miningSpeed = MiningSpeed.OPEN_SB_MENU;
                    }
                }
                break;
            case GET_LORE:
                if (miningSpeedDelay.hasReached(100)) {
                    ItemStack itemStack = InventoryUtils.getStackInOpenContainerSlot(13);

                    if (itemStack != null) {
                        NBTTagList lore = InventoryUtils.getLore(itemStack);
                        if (lore != null) {
                            for (int i = 0; i < lore.tagCount(); i++) {
                                if (lore.get(i).toString().contains("Mining Speed")) {
                                    MacroHandler.miningSpeed = Integer.parseInt(lore.get(i).toString().substring(19).replaceAll("[\\D]", ""));

                                    mc.thePlayer.closeScreen();

                                    miningSpeed = MiningSpeed.NONE;
                                    return;
                                }
                            }
                        } else {
                            LogUtils.debugLog("No item lore");
                            MacroHandler.disableScript();
                            return;
                        }
                    } else {
                        LogUtils.debugLog("No item stack");
                        MacroHandler.disableScript();
                        return;
                    }
                }
                break;
            case NONE:
        }

        if (miningSpeed != MiningSpeed.NONE) return;




        ArrayList<AOTVWaypointsStructs.Waypoint> Waypoints = MightyMiner.aotvWaypoints.getSelectedRoute().waypoints;

        if (tooFastTp && !waitForVeinsTimer.hasReached(10000)) {
            return;
        } else if (tooFastTp && waitForVeinsTimer.hasReached(10000)) {
            tooFastTp = false;
        }

        if (MightyMiner.config.refuelWithAbiphone) {
            if (FuelFilling.isRefueling() && !refueling) {
                if (baritone != null && baritone.getState() != AutoMineBaritone.BaritoneState.IDLE) {
                    baritone.disableBaritone();
                }
                refueling = true;
                return;
            } else if (!FuelFilling.isRefueling() && refueling) {
                refueling = false;
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                mc.mouseHelper.grabMouseCursor();
                tpStuckTimer.reset();
                waitAfterKillTimer.reset();
                timeBetweenLastWaypoint.reset();
                tooFastTp = false;
                firstTp = true;
                reScan = true;
                tping = false;
                currentState = State.MINING;
                return;
            } else if (!FuelFilling.isRefueling()) {
                if (!waitAfterKillTimer.hasReached(500)) return;
            }
            if (FuelFilling.isRefueling()) {
                if (baritone != null && baritone.getState() != AutoMineBaritone.BaritoneState.IDLE) {
                    baritone.disableBaritone();
                }
                return;
            }
        }


        if (MightyMiner.config.aotvKillYogs) {
            if (MobKiller.hasTarget()) {

                if (baritone != null && baritone.getState() != AutoMineBaritone.BaritoneState.IDLE) {
                    baritone.disableBaritone();
                }

                if (!killing) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                    killing = true;
                }

                return;
            } else if (killing) {
                killing = false;
                int miningTool = PlayerUtils.getItemInHotbarWithBlackList(true, null, "Pickaxe", "Gauntlet", "Drill");
                if (miningTool != -1) {
                    mc.thePlayer.inventory.currentItem = miningTool;
                }
                baritone.disableBaritone();
                LogUtils.addMessage("AOTV Macro - Yog has been killed!");
                waitAfterKillTimer.reset();
            } else {
                if (!waitAfterKillTimer.hasReached(500)) return;
            }
        }

        switch (currentState) {

            case MINING: {

                switch (baritone.getState()) {
                    case IDLE:
                        baritone.mineFor(MineUtils.getGemListBasedOnPriority());
                        break;
                    case EXECUTING:
                        reScan = true;
                        this.checkMiningSpeedBoost();
                        break;
                    case FAILED:
                        if (reScan) {
                            baritone.mineFor(MineUtils.getGemListBasedOnPriority());
                            reScan = false;
                            return;
                        }
                        LogUtils.addMessage("No gemstones left. Teleporting to next vein");
                        if (currentWaypoint == Waypoints.size() - 1) {
                            currentWaypoint = 0;
                        } else {
                            currentWaypoint++;
                        }
                        tpStuckTimer.reset();
                        currentState = State.WARPING;
                        baritone.disableBaritone();
                        reScan = true;
                        break;
                }

                break;
            }

            case WARPING: {
                BlockPos waypoint = new BlockPos(Waypoints.get(currentWaypoint).x, Waypoints.get(currentWaypoint).y, Waypoints.get(currentWaypoint).z);

                if(tping){
                    if(BlockUtils.getPlayerLoc().down().equals(waypoint)){
                        tping = false;
                        currentState = State.MINING;
                    } else if (!BlockUtils.getPlayerLoc().down().equals(waypoint) && tpStuckTimer.hasReached(2500)) {
                        LogUtils.addMessage("AOTV Macro - You are not at a valid waypoint!");
                        tping = false;
                        tpStuckTimer.reset();
                    }
                    return;
                }


                if (MightyMiner.config.aotvTeleportThreshold > 0) {
                    if (!firstTp && !timeBetweenLastWaypoint.hasReached((long) (MightyMiner.config.aotvTeleportThreshold * 1000))) {
                        LogUtils.addMessage("AOTV Macro - You are warping too fast! Probably veins didn't respawn in time. Waiting 10 seconds.");
                        waitForVeinsTimer.reset();
                        tooFastTp = true;
                        return;
                    }
                }



                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbarWithBlackList(true, null, "Void");

                if (!rotation.rotating)
                    rotation.initAngleLock(waypoint, MightyMiner.config.aotvCameraWaypointSpeed);

                if (AngleUtils.isDiffLowerThan(AngleUtils.getRequiredYawSide(waypoint), AngleUtils.getRequiredPitchSide(waypoint), 0.1f)) {
                    rotation.reset();
                    rotation.completed = true;
                }

                if (!rotation.completed) return;

                MovingObjectPosition movingObjectPosition = mc.thePlayer.rayTrace(55, 1);

                if (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    if (movingObjectPosition.getBlockPos().equals(waypoint)) {
                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                        LogUtils.addMessage("AOTV Macro - Teleported to waypoint " + currentWaypoint);
                        tping = true;
                        timeBetweenLastWaypoint.reset();
                        tpStuckTimer.reset();
                        if (firstTp) firstTp = false;
                    } else {
                        if (tpStuckTimer.hasReached(2000) && rotation.completed) {
                            LogUtils.addMessage("AOTV Macro - Path is not cleared. Block: " + movingObjectPosition.getBlockPos().toString() + " is on the way.");
                            this.toggle();
                            break;
                        }
                    }
                } else if (movingObjectPosition != null) {
                    LogUtils.addMessage("AOTV Macro - Something is on the way!");
                    this.toggle();
                }
                break;
            }
        }
    }


    @Override
    public void onLastRender(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (rotation.rotating)
            rotation.update();
    }

    private BaritoneConfig getAutoMineConfig() {
        return new BaritoneConfig(
                MiningType.STATIC,
                false,
                false,
                true,
                MightyMiner.config.aotvCameraSpeed,
                MightyMiner.config.aotvRestartTimeThreshold, //changed with config
                null,
                null,
                256,
                0

        );
    }
}


