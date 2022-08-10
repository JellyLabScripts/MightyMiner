package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import rosegoldaddons.Main;

import java.util.ArrayList;
import java.util.List;


public class PowderMacro extends Macro {

    List<Block> blocksAllowedToMine = new ArrayList<>();
    boolean haveTreasureChest;
    long treasureInitialTime;

    float playerYaw;

    Rotation rotation = new Rotation();


    @Override
    public void onEnable() {

        treasureInitialTime = System.currentTimeMillis();
        playerYaw = AngleUtils.getClosest();
        blocksAllowedToMine.clear();
        blocksAllowedToMine.add(Blocks.stone);
        blocksAllowedToMine.add(Blocks.air);
        blocksAllowedToMine.add(Blocks.coal_ore);
        blocksAllowedToMine.add(Blocks.iron_ore);
        blocksAllowedToMine.add(Blocks.emerald_ore);
        blocksAllowedToMine.add(Blocks.gold_ore);
        blocksAllowedToMine.add(Blocks.redstone_ore);
        blocksAllowedToMine.add(Blocks.diamond_ore);
        blocksAllowedToMine.add(Blocks.prismarine);
        blocksAllowedToMine.add(Blocks.wool);
        blocksAllowedToMine.add(Blocks.chest);
        blocksAllowedToMine.add(Blocks.trapped_chest);

        if(MightyMiner.config.powMineGemstone){
            blocksAllowedToMine.add(Blocks.stained_glass_pane);
            blocksAllowedToMine.add(Blocks.stained_glass);
        }


    }

    @Override
    public void onDisable() {
        Main.autoHardStone = false;
        KeybindHandler.resetKeybindState();
    }

    @Override
    public void onTick(TickEvent.Phase phase){
        if(phase != TickEvent.Phase.START)
            return;

        if(MightyMiner.config.powStoneAura)
            Main.autoHardStone = !haveTreasureChest;

        if(rotation.rotating){
            KeybindHandler.resetKeybindState();
            return;
        }



        if(haveTreasureChest && System.currentTimeMillis() - treasureInitialTime > 7000) {
            LogUtils.debugLog("Completed treasure due to timeout");
            haveTreasureChest = false;
        }

        if(!haveTreasureChest){
            rotation.intLockAngle(playerYaw, 60, 500);
            KeybindHandler.setKeyBindState(KeybindHandler.keybindW, true);
            if(mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && mc.objectMouseOver.getBlockPos().getY() >= (int)mc.thePlayer.posY)
                KeybindHandler.setKeyBindState(KeybindHandler.keybindAttack, true);

            if(!blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(0, 0, 1)) || !blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(0, 1, 1)))
                playerYaw = AngleUtils.get360RotationYaw(playerYaw + 90);

        } else
            KeybindHandler.resetKeybindState();

    }

    @Override
    public void onLastRender() {
        if(rotation.rotating)
            rotation.update();
    }

    @Override
    public void onMessageReceived(String message){
        if(message.contains("You have successfully picked the lock on this chest")){
            haveTreasureChest = false;
            LogUtils.debugLog("Completed treasure");
        }
        if(message.contains("You uncovered a treasure chest!")){
            haveTreasureChest = true;
            Main.autoHardStone = false;
            treasureInitialTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onPacketReceived(Packet<?> packet){
        if(haveTreasureChest && packet instanceof S2APacketParticles){
            if(((S2APacketParticles) packet).getParticleType() == EnumParticleTypes.CRIT){
                try {
                    LogUtils.debugLog("Found packet");
                    BlockPos closetChest = BlockUtils.findBlock(16, Blocks.chest, Blocks.trapped_chest).get(0);
                    LogUtils.debugLog("Found chest");
                    if(Math.abs((((S2APacketParticles) packet).getXCoordinate()) - closetChest.getX()) < 2 && Math.abs((((S2APacketParticles) packet).getYCoordinate()) - closetChest.getY()) < 2 && Math.abs((((S2APacketParticles) packet).getZCoordinate()) - closetChest.getZ()) < 2) {
                        rotation.intLockAngle(
                                AngleUtils.getRequiredYaw(((S2APacketParticles) packet).getXCoordinate() - mc.thePlayer.posX, ((S2APacketParticles) packet).getZCoordinate() - mc.thePlayer.posZ),
                                AngleUtils.getRequiredPitch(((S2APacketParticles) packet).getXCoordinate() - mc.thePlayer.posX, (((S2APacketParticles) packet).getYCoordinate()) - (mc.thePlayer.posY + 1.62d), ((S2APacketParticles) packet).getZCoordinate() - mc.thePlayer.posZ),
                                50);
                    }
                }catch (Exception ignored){}
            }
        }
    }
}
