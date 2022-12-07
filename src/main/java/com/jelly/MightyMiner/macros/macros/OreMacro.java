package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
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
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;


public class OreMacro extends Macro {


    final List<Block> blocksAllowedToMine = new ArrayList<Block>(){
        {
            add(Blocks.stone);
            add(Blocks.gold_ore);
            add(Blocks.emerald_ore);
            add(Blocks.redstone_ore);
            add(Blocks.iron_ore);
            add(Blocks.coal_ore);
            add(Blocks.air);
        }
    };

    AutoMineBaritone baritone;
    boolean haveTreasureChest;
    long treasureInitialTime;

    Rotation rotation = new Rotation();



    @Override
    public boolean isPaused() {
        return !enabled;
    }

    @Override
    public void Pause() {
        KeybindHandler.resetKeybindState();
        baritone.disableBaritone();
        enabled = false;
    }

    @Override
    public void Unpause() {
        toggle();
    }


    @Override
    public void onEnable() {
        if (isPaused()) {
            System.out.println("Unpausing");
            Unpause();
        }
        System.out.println("Enabled Ore macro checking if player is near");
        baritone = new AutoMineBaritone(getMineBehaviour());
    }

    @Override
    public void FailSafeDisable() {
        PlayerUtils.warpBackToIsland();
        MacroHandler.disableScript();
    }

    @Override
    public void onDisable() {
        baritone.disableBaritone();
    }

    @Override
    public void onTick(TickEvent.Phase phase){
        if (!enabled) return;

        if(phase != TickEvent.Phase.START)
            return;


        if(haveTreasureChest && System.currentTimeMillis() - treasureInitialTime > 7000) {
            haveTreasureChest = false;
        }


        if(!haveTreasureChest) {
            switch(baritone.getState()){
                case IDLE:
                    baritone.mineFor(Blocks.gold_ore, Blocks.emerald_ore, Blocks.redstone_ore, Blocks.iron_ore, Blocks.coal_ore);
                    break;
                case FAILED:
                    LogUtils.addMessage("Mined all ores nearby, disabling script");
                    MacroHandler.disableScript();
                    break;
            }
        }

        useMiningSpeedBoost();
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
        if(message.contains("You uncovered a treasure chest!") && MightyMiner.config.oreOpenChest){
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
                    BlockPos closetChest = BlockUtils.findBlock(8, Blocks.chest, Blocks.trapped_chest).get(0);
                    if(Math.abs((((S2APacketParticles) packet).getXCoordinate()) - closetChest.getX()) < 2 && Math.abs((((S2APacketParticles) packet).getYCoordinate()) - closetChest.getY()) < 2 && Math.abs((((S2APacketParticles) packet).getZCoordinate()) - closetChest.getZ()) < 2) {
                        rotation.intLockAngle(
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
                MightyMiner.config.oreRotationTime,
                MightyMiner.config.oreRestartTimeThreshold,
                null,
                blocksAllowedToMine,
                MightyMiner.config.oreMaxY,
                MightyMiner.config.oreMinY
        );
    }



}
