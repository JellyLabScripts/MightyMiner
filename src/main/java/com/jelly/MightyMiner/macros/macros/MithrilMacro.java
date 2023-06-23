package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.features.Failsafes;
import com.jelly.MightyMiner.features.FuelFilling;
import com.jelly.MightyMiner.features.PingAlert;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.mixins.accessors.RenderGlobalAccessor;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.*;
import com.jelly.MightyMiner.utils.BlockUtils.BlockData;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.HypixelUtils.MineUtils;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MithrilMacro extends Macro {

    AutoMineBaritone baritone;

    boolean noMithril;

    private int staffCheckCounter = 0;

    private boolean inStaffCheck = false;

    public enum StaffCheckState {
        FIND,
        LOOK,
        CLICK
    }

    private StaffCheckState staffCheckState = StaffCheckState.FIND;

    private List<BlockPos> validBlocks = new ArrayList<>();

    private int clickCounter = 0;

    private ArrayList<BlockPos> blacklistStaff = new ArrayList<>();

    private int checkIteration = 0;

    private int noDmgCount = 0;

    private final Timer restartTimer = new Timer();

    public static boolean restarting = false;

    private int totalCheckCounter = 0;

    private final Rotation rotation = new Rotation();

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
    protected void onEnable() {
        MacroHandler.miningSpeedActive = false;
        LogUtils.debugLog("Enabled Mithril macro checking if player is near");
        staffCheckCounter = 0;
        clickCounter = 0;
        checkIteration = 0;
        inStaffCheck = false;
        totalCheckCounter = 0;
        restarting = false;
        staffCheckState = StaffCheckState.FIND;

        if (MightyMiner.config.playerFailsafe) {
            if (PlayerUtils.isNearPlayer(MightyMiner.config.playerRad)) {
                LogUtils.addMessage("Didnt start macro since therese a player near");
                this.enabled = false;
                onDisable();
                return;
            }
        }

        noMithril = false;
        baritone = new AutoMineBaritone(getMineBehaviour());

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
    public void onTick(TickEvent.Phase phase) {
        if (!enabled) return;

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

        if (restartTimer.hasReached(6000) && restarting) {
            inStaffCheck = false;
            staffCheckCounter = 0;
            checkIteration = 0;
            clickCounter = 0;
            restarting = false;
            totalCheckCounter++;
            return;
        }
        if (restarting) return;

        if (inStaffCheck) {

            switch (staffCheckState) {
                case FIND:
                    validBlocks.clear();
                    ArrayList<BlockData<?>> gray = MineUtils.getMithrilColorBasedOnPriority(0);
                    ArrayList<BlockData<?>> green = MineUtils.getMithrilColorBasedOnPriority(1);
                    ArrayList<BlockData<?>> blue = MineUtils.getMithrilColorBasedOnPriority(2);
                    ArrayList<BlockData<?>> titanium = MineUtils.getMithrilColorBasedOnPriority(3);
                    ArrayList<BlockData<?>> all = new ArrayList<>();
                    all.addAll(gray);
                    all.addAll(green);
                    all.addAll(blue);
                    all.addAll(titanium);
                    for (BlockPos blockPos: BlockUtils.findBlockInCube(10, blacklistStaff, 0, 256, all)) {
                        if (BlockUtils.canMineBlock(blockPos)) {
                            validBlocks.add(blockPos);
                        }
                    }

                    if (validBlocks.size() > 0 && totalCheckCounter < 1) {
                        rotation.reset();
                        staffCheckState = StaffCheckState.LOOK;
                    } else {
                        Failsafes.fakeMovement(false);
                        return;
                    }
                    break;
                case LOOK:
                    Vec3 lookVec = BlockUtils.getClosetVisibilityLine(validBlocks.get(0));
                    Pair<Float, Float> rotateTo = VectorUtils.vec3ToRotation(lookVec);

                    if (AngleUtils.isDiffLowerThan(rotateTo.getLeft(), rotateTo.getRight(), 1f)) {
                        staffCheckState = StaffCheckState.CLICK;
                        clickCounter = 0;
                        blacklistStaff.clear();
                        blacklistStaff.add(validBlocks.get(0));
                        rotation.reset();
                        return;
                    }

                    rotation.initAngleLock(rotateTo.getLeft(), rotateTo.getRight(), 400);
                    break;
                case CLICK:
                    if (clickCounter > 2) {
                        if (checkIteration > 2) {
                            restartTimer.reset();
                            restarting = true;
                            return;
                        } else {
                            checkIteration++;
                            staffCheckState = StaffCheckState.FIND;
                        }
                    } else {
                        KeybindHandler.leftClick();
                        clickCounter++;
                    }
                    break;
            }
        }
        if (inStaffCheck) return;

        MovingObjectPosition ray2 = mc.thePlayer.rayTrace(5, 1);
        if (ray2 != null) {
            if (BlockUtils.getBlockDamage(ray2.getBlockPos()) > 0) {
                noDmgCount = 0;
            } else {
                noDmgCount ++;
            }
        }

        if (staffCheckCounter > 2) {
            LogUtils.addMessage("Unbreakable block staff check or wrong stuck time threshold");
            PingAlert.sendPingAlert();
            staffCheckCounter = 0;
            inStaffCheck = true;
            return;
        }

        if (MightyMiner.config.refuelWithAbiphone) {
            if (FuelFilling.isRefueling()) {
                staffCheckState = StaffCheckState.FIND;
                if (baritone != null && baritone.getState() != AutoMineBaritone.BaritoneState.IDLE) {
                    baritone.disableBaritone();
                }
                return;
            }
        }

        if (phase != TickEvent.Phase.START)
            return;

        switch (baritone.getState()) {
            case IDLE:
                baritone.mineFor(getPriorityList());
                break;
            case FAILED:
                MovingObjectPosition ray = mc.thePlayer.rayTrace(5, 1);
                if (ray != null && baritone.getCurrentBlockPos() != null && ray.getBlockPos().equals(baritone.getCurrentBlockPos()) && noDmgCount > MightyMiner.config.mithRestartTimeThreshold * 20) {
                    LogUtils.debugLog("Maybe unbreakable block failsafe");
                    staffCheckCounter++;

                    if (staffCheckCounter > 2) {
                        return;
                    }
                }
                baritone.mineFor(getPriorityList());
                break;

        }

        checkMiningSpeedBoost();
    }

    private ArrayList<ArrayList<BlockData<?>>> getPriorityList() {
        ArrayList<ArrayList<BlockData<?>>> priorityList = new ArrayList<>();

        priorityList.add(MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority1));
        priorityList.add(MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority2));
        priorityList.add(MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority3));
        priorityList.add(MineUtils.getMithrilColorBasedOnPriority(MightyMiner.config.mithPriority4));

        return priorityList;
    }

    @Override
    protected void onDisable() {
        if (baritone != null) baritone.disableBaritone();
        KeybindHandler.resetKeybindState();
    }

    public void onLastRender(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (rotation.rotating) {
            rotation.update();
        }
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

