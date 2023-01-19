package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
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

    private AOTVBlockData target = null;
    private AOTVBlockData oldTarget = null;
    private int currentWaypoint = -1;

    private final Timer stuckTimer = new Timer();
    private final Timer searchingTimer = new Timer();
    private final Timer stuckTimer2 = new Timer();
    private final Timer timeBetweenLastWaypoint = new Timer();
    private final Timer waitForVeinsTimer = new Timer();
    private BlockPos blockToIgnoreBecauseOfStuck = null;
    private boolean tooFastTp = false;
    private boolean firstTp = true;

    private final ArrayList<AOTVBlockData> blocksToMine = new ArrayList<>();

    private boolean killing = false;
    private boolean refueling = false;

    public enum State {
        SEARCHING,
        MINING,
        WARPING
    }

    private State currentState = State.SEARCHING;

    private final Rotation rotation = new Rotation();

    @Override
    public void onEnable() {
        blocksToMine.clear();
        if (MightyMiner.aotvWaypoints.getSelectedRoute() == null) {
            LogUtils.addMessage("No route selected!");
            this.toggle();
            return;
        }

        ArrayList<AOTVWaypointsGUI.Waypoint> Waypoints = MightyMiner.aotvWaypoints.getSelectedRoute().waypoints;

        BlockPos currentPos = BlockUtils.getPlayerLoc().down();

        int miningTool = PlayerUtils.getItemInHotbarWithBlackList(true, "Pickaxe", "Gauntlet", "Drill");

        if (miningTool == -1) {
            LogUtils.addMessage("AOTV Macro (Experimental) - You don't have a mining tool!");
            this.toggle();
            return;
        }

        int voidTool = PlayerUtils.getItemInHotbarWithBlackList(true,"Void");

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
    }

    @Override
    public void onDisable() {
        target = null;
        oldTarget = null;
        currentWaypoint = -1;
        tooFastTp = false;
        firstTp = true;
        currentState = State.SEARCHING;
        stuckTimer.reset();
        searchingTimer.reset();
        stuckTimer2.reset();
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
        if (target == null) return;

        BlockPos pos = event.pos;
        IBlockState newBlock = event.update;

        if (pos.equals(target.getPos()) && !newBlock.equals(target.getState())) {
            currentState = State.SEARCHING;
            oldTarget = target;
            target = null;
            rotation.reset();
            rotation.completed = true;
            return;
        }

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
                stuckTimer2.reset();
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
                int miningTool = PlayerUtils.getItemInHotbarWithBlackList(true, "Pickaxe", "Gauntlet", "Drill");
                if (miningTool != -1) {
                    mc.thePlayer.inventory.currentItem = miningTool;
                }
            }
        }

        switch (currentState) {
            case SEARCHING: {

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

                if (blockToIgnoreBecauseOfStuck != null && !stuckTimer2.hasReached(100)) {
                    break;
                }

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);

                if (blocksToMine.isEmpty()) {
                    blocksToMine.addAll(getBlocksToMine());
                }

                target = getClosestGemstone();
                blockToIgnoreBecauseOfStuck = null;

                if (target != null) {
                    currentState = State.MINING;
                    int miningTool = PlayerUtils.getItemInHotbarWithBlackList(true, "Pickaxe", "Gauntlet", "Drill");


                    if (miningTool == -1) {
                        LogUtils.addMessage("AOTV Macro (Experimental) - You don't have a mining tool!");
                        this.toggle();
                        return;
                    }
                    mc.thePlayer.inventory.currentItem = miningTool;
                } else {

                    LogUtils.addMessage("AOTV Macro (Experimental) - No gemstones found! Going to the next waypoint.");
                    if (currentWaypoint == Waypoints.size() - 1) {
                        currentWaypoint = 0;
                    } else {
                        currentWaypoint++;
                    }
                    currentState = State.WARPING;
                }

                stuckTimer.reset();
                break;
            }

            case MINING: {

                if (target == null) {
                    currentState = State.SEARCHING;
                    searchingTimer.reset();
                    break;
                }

                if (mc.thePlayer.getHeldItem() != null && Arrays.stream(new String[]{"Pickaxe", "Gauntlet", "Drill"}).noneMatch(name -> mc.thePlayer.getHeldItem().getDisplayName().contains(name))) {
                    int miningTool = PlayerUtils.getItemInHotbarWithBlackList(true, "Drill", "Pickaxe", "Gauntlet");
                    if (miningTool == -1) {
                        LogUtils.addMessage("AOTV Macro (Experimental) - You don't have a mining tool!");
                        this.toggle();
                        return;
                    }
                    mc.thePlayer.inventory.currentItem = miningTool;
                }

                this.checkMiningSpeedBoost();

                if (rotation.completed)
                    rotation.initAngleLock(target.getRandomVisibilityLine(), MightyMiner.config.aotvCameraSpeed);

                boolean lookingAtTarget = mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.getBlockPos().equals(target.getPos());

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), lookingAtTarget);

                Tuple<Float, Float> rota = AngleUtils.getRotation(target.getRandomVisibilityLine());

                if (stuckTimer.hasReached(MacroHandler.miningSpeedActive ? MightyMiner.config.aotvStuckTimeThreshold / 2 : MightyMiner.config.aotvStuckTimeThreshold) && AngleUtils.getAngleDifference(rota.getFirst(), rota.getSecond()) < 1) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                    LogUtils.addMessage("AOTV Macro (Experimental) - Stuck for " + (MacroHandler.miningSpeedActive ? MightyMiner.config.aotvStuckTimeThreshold / 2f : MightyMiner.config.aotvStuckTimeThreshold) + " ms " + (MacroHandler.miningSpeedActive ? "(Faster stuck check, because of Boost active)" : "") + ", restarting.");
                    stuckTimer.reset();
                    currentState = State.SEARCHING;
                    searchingTimer.reset();
                    blockToIgnoreBecauseOfStuck = target.getPos();
                    oldTarget = null;
                    target = null;
                    stuckTimer2.reset();
                    rotation.completed = true;
                }

                break;
            }

            case WARPING: {

                int voidTool = PlayerUtils.getItemInHotbarWithBlackList(true, "Void");

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

                if (AngleUtils.getAngleDifference(AngleUtils.getRequiredYaw(waypoint), AngleUtils.getRequiredPitch(waypoint)) < 0.3) {
                    rotation.reset();
                    rotation.completed = true;
                }

                if (!rotation.completed) return;

                MovingObjectPosition movingObjectPosition = mc.thePlayer.rayTrace(55, 1);

                if (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    if (movingObjectPosition.getBlockPos().equals(waypoint)) {
                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                        LogUtils.addMessage("AOTV Macro (Experimental) - Teleported to waypoint " + currentWaypoint);
                        blocksToMine.clear();
                        currentState = State.SEARCHING;
                        searchingTimer.reset();
                        timeBetweenLastWaypoint.reset();
                        oldTarget = null;
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

    private ArrayList<AOTVBlockData> getBlocksToMine() {
        float range = 5f;

        ArrayList<AOTVBlockData> blocks = new ArrayList<>();
        Iterable<BlockPos> blockss = BlockPos.getAllInBox(BlockUtils.getPlayerLoc().add(-range, -range, -range), BlockUtils.getPlayerLoc().add(range, range, range));
        for (BlockPos blockPos1 : blockss) {
            ArrayList<Block> blocksToCheck = new ArrayList<Block>() {{
                add(Blocks.stained_glass_pane);
                add(Blocks.stained_glass);
                if (MightyMiner.config.aotvGemstoneType == 8) {
                    add(Blocks.wool);
                    add(Blocks.prismarine);
                    add(Blocks.stained_hardened_clay);
                }
            }};
            if (blocksToCheck.stream().anyMatch(b -> b.equals(mc.theWorld.getBlockState(blockPos1).getBlock()))) {

                if (MightyMiner.config.aotvGemstoneType == 8) {
                    if (!IsThisAGoodMithril(blockPos1)) continue;
                }
                else if (MightyMiner.config.aotvGemstoneType > 0) {
                    if (!IsThisAGoodGemstone(blockPos1)) continue;
                }

                IBlockState bs = mc.theWorld.getBlockState(blockPos1);
                blocks.add(new AOTVBlockData(blockPos1, bs.getBlock(), bs, null));
            }
        }

        return blocks;
    }

    private AOTVBlockData getClosestGemstone() {
        AOTVBlockData blockPos = null;

        double distance = 9999;
        for (AOTVBlockData block : blocksToMine) {
            AOTVBlockData blockPos1 = block;
            double currentDistance;

            if (mc.theWorld.getBlockState(blockPos1.getPos()) == null || mc.theWorld.isAirBlock(blockPos1.getPos())) continue;

            if (blockPos1.getPos().equals(blockToIgnoreBecauseOfStuck)) continue;

            Vec3 vec3 = BlockUtils.getRandomVisibilityLine(blockPos1.getPos());
            if (vec3 == null) continue;

            blockPos1 = new AOTVBlockData(blockPos1.getPos(), blockPos1.getBlock(), blockPos1.getState(), vec3);

            if (oldTarget != null) {
                currentDistance = oldTarget.getPos().distanceSq(blockPos1.getPos());
            } else if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                currentDistance = mc.objectMouseOver.getBlockPos().distanceSq(blockPos1.getPos());
            } else {
                currentDistance = BlockUtils.getPlayerLoc().distanceSq(blockPos1.getPos());
            }
            if (currentDistance < distance) {
                distance = currentDistance;
                blockPos = blockPos1;
            }
        }

        return blockPos;
    }

    private boolean IsThisAGoodMithril(BlockPos blockPos) {

        if (mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.wool) {
            EnumDyeColor color = mc.theWorld.getBlockState(blockPos).getValue(BlockColored.COLOR);
            return (color == EnumDyeColor.LIGHT_BLUE || color == EnumDyeColor.GRAY);
        }

        if (mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.prismarine) {
            return true;
        }

        if (mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.stained_hardened_clay) {
            EnumDyeColor color = mc.theWorld.getBlockState(blockPos).getValue(BlockColored.COLOR);
            return (color == EnumDyeColor.CYAN);
        }

        return false;
    }

    public boolean IsThisAGoodGemstone(BlockPos block) {

        EnumDyeColor color = mc.theWorld.getBlockState(block).getValue(BlockColored.COLOR);

        switch (color) {
            case RED: {
                return MightyMiner.config.aotvGemstoneType == 1;
            }
            case PURPLE: {
                return MightyMiner.config.aotvGemstoneType == 2;
            }
            case LIME: {
                return MightyMiner.config.aotvGemstoneType == 3;
            }
            case BLUE: {
                return MightyMiner.config.aotvGemstoneType == 4;
            }
            case ORANGE: {
                return MightyMiner.config.aotvGemstoneType == 5;
            }
            case YELLOW: {
                return MightyMiner.config.aotvGemstoneType == 6;
            }
            case MAGENTA: {
                return MightyMiner.config.aotvGemstoneType == 7;
            }

            default: {
                LogUtils.addMessage("AOTV Macro (Experimental) - Unknown gemstone color: " + color.getName());
                break;
            }
        }

        return false;
    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if(rotation.rotating)
            rotation.update();

        if (!Objects.equals(SkyblockInfo.map, SkyblockInfo.MAPS.CrystalHollows.map)) return;
        if (MightyMiner.aotvWaypoints.getSelectedRoute() == null) return;
        ArrayList<AOTVWaypointsGUI.Waypoint> Waypoints = MightyMiner.aotvWaypoints.getSelectedRoute().waypoints;
        if (Waypoints == null || Waypoints.isEmpty()) return;

        if (target != null) {
            DrawUtils.drawBlockBox(target.getPos(), new Color(0, 255, 0, 80), 4f);
            DrawUtils.drawMiniBlockBox(target.getRandomVisibilityLine(), new Color(0, 255, 247, 166), 1.5f);
        }
    }
}
