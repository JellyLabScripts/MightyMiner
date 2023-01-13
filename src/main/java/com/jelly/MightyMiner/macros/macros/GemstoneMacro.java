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
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.HypixelUtils.MineUtils;
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


    @Override
    public void Pause() {
        paused = true;
        if (baritone != null)
            baritone.disableBaritone();
        KeybindHandler.resetKeybindState();
    }

    @Override
    public void Unpause() {
        paused = false;
        if (baritone != null) {
            baritone.disableBaritone();
        }
    }


    @Override
    public void onEnable() {
        System.out.println("Enabled Gemstone macro checking if player is near");
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

        if (paused)
            return;

        if(phase != TickEvent.Phase.START)
            return;


        if(haveTreasureChest && System.currentTimeMillis() - treasureInitialTime > 7000) {
            haveTreasureChest = false;
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
                    BlockPos closetChest = BlockUtils.findBlock(8, Blocks.chest, Blocks.trapped_chest).get(0);
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
