package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.features.FuelFilling;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.*;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.HypixelUtils.MineUtils;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;


public class GemstoneMacro extends Macro {


    final List<Block> blocksAllowedToMine = new ArrayList<Block>(){
        {
            add(Blocks.stone);
            add(Blocks.gold_ore);
            add(Blocks.emerald_ore);
            add(Blocks.redstone_ore);
            add(Blocks.iron_ore);
            add(Blocks.coal_ore);
            add(Blocks.stained_glass_pane);
            add(Blocks.stained_glass);
            add(Blocks.air);
        }
    };

    AutoMineBaritone baritone;
    boolean haveTreasureChest;
    long treasureInitialTime;

    Rotation rotation = new Rotation();

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
        System.out.println("Enabled Gemstone macro checking if player is near");
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
    public void onDisable() {
        baritone.disableBaritone();
    }

    @Override
    public void onTick(TickEvent.Phase phase){
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

        if(phase != TickEvent.Phase.START)
            return;


        if(haveTreasureChest && System.currentTimeMillis() - treasureInitialTime > 7000) {
            haveTreasureChest = false;
        }

        if (MightyMiner.config.refuelWithAbiphone) {
            if (FuelFilling.isRefueling()) {
                if (baritone != null && baritone.getState() != AutoMineBaritone.BaritoneState.IDLE) {
                    baritone.disableBaritone();
                }
                return;
            }
        }


        if(!haveTreasureChest) {
            switch(baritone.getState()){
                case IDLE:
                    baritone.mineFor(MineUtils.getGemListBasedOnPriority(MightyMiner.config.gemGemstoneType));
                    break;
                case FAILED:
                    LogUtils.addMessage("Mined all gemstones nearby, disabling script");
                    MacroHandler.disableScript();
                    break;
            }
        }

        checkMiningSpeedBoost();
    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {
        if(rotation.rotating)
            rotation.update();
    }


    @Override
    public void onMessageReceived(String message){
        if(message.contains("You have successfully picked the lock on this chest")){
            haveTreasureChest = false;
        }
        if(message.contains("You uncovered a treasure chest!") && MightyMiner.config.gemOpenChest){
            LogUtils.debugLog("Found treasure chest!");
            haveTreasureChest = true;
            baritone.disableBaritone();
            treasureInitialTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onPacketReceived(Packet<?> packet){
        if(haveTreasureChest && packet instanceof S2APacketParticles){
            if(((S2APacketParticles) packet).getParticleType() == EnumParticleTypes.CRIT){
                try {
                    BlockPos closetChest = BlockUtils.findBlockInCube(8, null, 0, 256, Blocks.chest, Blocks.trapped_chest).get(0);
                    if(Math.abs((((S2APacketParticles) packet).getXCoordinate()) - closetChest.getX()) < 2 && Math.abs((((S2APacketParticles) packet).getYCoordinate()) - closetChest.getY()) < 2 && Math.abs((((S2APacketParticles) packet).getZCoordinate()) - closetChest.getZ()) < 2) {
                        rotation.initAngleLock(
                                AngleUtils.getRequiredYaw(((S2APacketParticles) packet).getXCoordinate() - closetChest.getX(), ((S2APacketParticles) packet).getZCoordinate() - closetChest.getZ()),
                                AngleUtils.getRequiredPitch(((S2APacketParticles) packet).getXCoordinate() - closetChest.getX(), (((S2APacketParticles) packet).getYCoordinate()) - closetChest.getY(), ((S2APacketParticles) packet).getZCoordinate() - closetChest.getZ()),
                                50);
                    }
                }catch (Exception ignored){}
            }
        }
    }

    private BaritoneConfig getMineBehaviour(){
        return new BaritoneConfig(
                MiningType.DYNAMIC,
                false,
                true,
                false,
                MightyMiner.config.gemRotationTime,
                MightyMiner.config.gemRestartTimeThreshold,
                null,
                blocksAllowedToMine,
                MightyMiner.config.gemMaxY,
                MightyMiner.config.gemMinY
        );
    }



}
