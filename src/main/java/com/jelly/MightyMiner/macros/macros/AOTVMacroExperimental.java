package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.events.BlockChangeEvent;
import com.jelly.MightyMiner.features.FuelFilling;
import com.jelly.MightyMiner.features.MobKiller;
import com.jelly.MightyMiner.gui.AOTVWaypointsGUI;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.*;
import com.jelly.MightyMiner.utils.BlockUtils.AOTVBlockData;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.HypixelUtils.MineUtils;
import com.jelly.MightyMiner.utils.HypixelUtils.SkyblockInfo;
import com.jelly.MightyMiner.utils.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class AOTVMacroExperimental extends Macro {

    private final Minecraft mc = Minecraft.getMinecraft();

    private int currentWaypoint = -1;

    private final Timer stuckTimer = new Timer();
    private final Timer searchingTimer = new Timer();
    private final Timer timeBetweenLastWaypoint = new Timer();
    private final Timer waitForVeinsTimer = new Timer();
    private boolean tooFastTp = false;
    private boolean firstTp = true;

    private boolean killing = false;
    private boolean refueling = false;

    private AutoMineBaritone baritone;

    public enum State {
        MINING,
        WARPING
    }

    private State currentState = State.MINING;
    private final Rotation rotation = new Rotation();

    @Override
    public void onEnable() {
        if (MightyMiner.aotvWaypoints.getSelectedRoute() == null) {
            LogUtils.addMessage("No route selected!");
            this.toggle();
            return;
        }

        ArrayList<AOTVWaypointsGUI.Waypoint> Waypoints = MightyMiner.aotvWaypoints.getSelectedRoute().waypoints;

        BlockPos currentPos = BlockUtils.getPlayerLoc().down();

        int miningTool = PlayerUtils.getItemInHotbarWithBlackList(true, null, "Pickaxe", "Gauntlet", "Drill");

        if (miningTool == -1) {
            LogUtils.addMessage("AOTV Macro (Experimental) - You don't have a mining tool!");
            this.toggle();
            return;
        }

        int voidTool = PlayerUtils.getItemInHotbarWithBlackList(true,null, "Void");

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

        if (MightyMiner.config.killYogs) {
            MightyMiner.mobKiller.Toggle();
            MobKiller.setMobsNames(false, "Yog");
            if (MightyMiner.config.useHyperionUnderPlayer) {
                MobKiller.scanRange = 5;
            } else {
                MobKiller.scanRange = 10;
            }
            MobKiller.isToggled = true;
        }

        searchingTimer.reset();
        timeBetweenLastWaypoint.reset();
        tooFastTp = false;
        baritone = new AutoMineBaritone(getAutoMineConfig());
    }

    @Override
    public void onDisable() {
        currentWaypoint = -1;
        tooFastTp = false;
        firstTp = true;
        stuckTimer.reset();
        searchingTimer.reset();
        timeBetweenLastWaypoint.reset();
        waitForVeinsTimer.reset();
        rotation.reset();
        rotation.completed = true;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
        if (MobKiller.isToggled)
            MightyMiner.mobKiller.Toggle();
        MobKiller.isToggled = false;
    }

    @SubscribeEvent
    public void onBlockChange(BlockChangeEvent event) {
        if (!isEnabled()) return;

        BlockPos pos = event.pos;

        if (event.old.getBlock() == Blocks.cobblestone) {
            if (event.update.getBlock() == Blocks.air) {
                ArrayList<AOTVWaypointsGUI.Waypoint> Waypoints = MightyMiner.aotvWaypoints.getSelectedRoute().waypoints;

                AOTVWaypointsGUI.Waypoint wp = Waypoints.stream().filter(waypoint -> waypoint.x == pos.getX() && waypoint.y == pos.getY() && waypoint.z == pos.getZ()).findFirst().orElse(null);
                if (wp != null) {
                    LogUtils.addMessage("AOTV Macro (Experimental) - Cobblestone at waypoint " + EnumChatFormatting.BOLD + wp.name + EnumChatFormatting.RESET + EnumChatFormatting.RED + " has been destroyed!");

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

        if (PlayerUtils.hasOpenContainer()) return;

        ArrayList<AOTVWaypointsGUI.Waypoint> Waypoints = MightyMiner.aotvWaypoints.getSelectedRoute().waypoints;

        if (tooFastTp && !waitForVeinsTimer.hasReached(10000)) {
            return;
        } else if (tooFastTp && waitForVeinsTimer.hasReached(10000)) {
            tooFastTp = false;
        }

        if (MightyMiner.config.refuelWithAbiphone) {
            if (FuelFilling.isRefueling() && !refueling) {
                refueling = true;
                return;
            } else if (!FuelFilling.isRefueling() && refueling) {
                refueling = false;
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                mc.mouseHelper.grabMouseCursor();
                stuckTimer.reset();
                return;
            }
            if (FuelFilling.isRefueling()) {
                return;
            }
        }


        if (MightyMiner.config.killYogs) {
            if (MobKiller.hasTarget()) {

                if (!killing) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                    killing = true;
                }

                return;
            } else if (killing) {
                killing = false;
                int miningTool = PlayerUtils.getItemInHotbarWithBlackList(true,  null, "Pickaxe", "Gauntlet", "Drill");
                if (miningTool != -1) {
                    mc.thePlayer.inventory.currentItem = miningTool;
                }
            }
        }

        switch (currentState) {

            case MINING: {

                switch (baritone.getState()){
                    case IDLE:
                        BlockPos currentPos = BlockUtils.getPlayerLoc().down();
                        BlockPos waypoint = new BlockPos(Waypoints.get(currentWaypoint).x, Waypoints.get(currentWaypoint).y, Waypoints.get(currentWaypoint).z);

                        if (!currentPos.equals(waypoint)) {
                            if (searchingTimer.hasReached(MightyMiner.config.aotvStuckTimeThreshold)) {
                                LogUtils.addMessage("AOTV Macro (Experimental) - You are not at a valid waypoint!");
                                currentState = State.WARPING;
                                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                            }
                            break;
                        }
                        baritone.mineFor(MineUtils.getGemListBasedOnPriority(MightyMiner.config.aotvGemstoneType));
                        break;
                    case EXECUTING:
                        this.checkMiningSpeedBoost();
                    case FAILED:
                        currentState = State.WARPING;
                        baritone.disableBaritone();
                }

                break;
            }

            case WARPING: {

                int voidTool = PlayerUtils.getItemInHotbarWithBlackList(true, null, "Void");

                if (MightyMiner.config.teleportThreshold > 0) {
                    if (!firstTp && !timeBetweenLastWaypoint.hasReached((long) (MightyMiner.config.teleportThreshold * 1000))) {
                        LogUtils.addMessage("AOTV Macro (Experimental) - You are warping too fast! Probably veins didn't respawn in time. Waiting 10 seconds.");
                        waitForVeinsTimer.reset();
                        tooFastTp = true;
                        return;
                    }
                }

                if (voidTool == -1) {
                    LogUtils.addMessage("AOTV Macro (Experimental) - You don't have an Aspect of the Void!");
                    this.toggle();
                    return;
                }

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);

                mc.thePlayer.inventory.currentItem = voidTool;

                BlockPos waypoint = new BlockPos(Waypoints.get(currentWaypoint).x, Waypoints.get(currentWaypoint).y, Waypoints.get(currentWaypoint).z);
                rotation.initAngleLock(waypoint, MightyMiner.config.aotvWaypointTargetingTime);

                if (AngleUtils.getAngleDifference(AngleUtils.getRequiredYawSide(waypoint), AngleUtils.getRequiredPitchSide(waypoint)) < 0.3) {
                    rotation.reset();
                    rotation.completed = true;
                }

                if (!rotation.completed) return;

                MovingObjectPosition movingObjectPosition = mc.thePlayer.rayTrace(55, 1);

                if (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    if (movingObjectPosition.getBlockPos().equals(waypoint)) {
                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                        LogUtils.addMessage("AOTV Macro (Experimental) - Teleported to waypoint " + currentWaypoint);
                        currentState = State.MINING;
                        searchingTimer.reset();
                        timeBetweenLastWaypoint.reset();
                        if (firstTp) firstTp = false;
                    } else {
                        if (stuckTimer.hasReached(2000) && rotation.completed) {
                            LogUtils.addMessage("AOTV Macro (Experimental) - Path is not cleared. Block: " + movingObjectPosition.getBlockPos().toString() + " is on the way.");
                            this.toggle();
                            break;
                        }
                    }
                } else if (movingObjectPosition != null) {
                    LogUtils.addMessage("AOTV Macro (Experimental) - Something is on the way!");
                    this.toggle();
                }
                break;
            }
        }
    }


    @Override
    public void onLastRender(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if(rotation.rotating)
            rotation.update();
    }

    private BaritoneConfig getAutoMineConfig(){
        return new BaritoneConfig(
                MiningType.STATIC,
                true,
                false,
                false,
                MightyMiner.config.aotvCameraSpeed,
                MightyMiner.config.aotvRestartTimeThreshold, //changed with config
                null,
                null,
                256,
                0

        );
    }
}
