package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.features.YogKiller;
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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
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
    public boolean isPaused() {
        return !enabled;
    }

    @Override
    public void Pause() {
        KeybindHandler.resetKeybindState();
        toggle();
    }

    @Override
    public void Unpause() {
        toggle();
    }


    @Override
    public void onEnable() {
        if (isPaused()) {
            Unpause();
        }
        YogKiller.enabled = true;
        baritone = new AutoMineBaritone(getMineBehaviour());
    }

    @Override
    public void onDisable() {
        baritone.disableBaritone();
        YogKiller.enabled = false;
    }

    @Override
    public void onTick(TickEvent.Phase phase){

        if(phase != TickEvent.Phase.START)
            return;

        if(MightyMiner.config.gemPlayerFailsafe) {
            if(PlayerUtils.isNearPlayer(MightyMiner.config.gemPlayerRad)){
                PlayerUtils.warpBackToIsland();
                MacroHandler.disableScript();
            }
        }

        if(haveTreasureChest && System.currentTimeMillis() - treasureInitialTime > 7000) {
            haveTreasureChest = false;
        }


        if(!haveTreasureChest) {
            switch(baritone.getState()){
                case IDLE:
                    baritone.mineFor(Blocks.stained_glass_pane, Blocks.stained_glass);
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
                AutoMineType.DYNAMIC,
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
