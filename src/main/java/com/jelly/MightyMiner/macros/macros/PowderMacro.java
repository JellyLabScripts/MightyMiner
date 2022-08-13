package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
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
    List<Block> mineSlowBlocks = new ArrayList<Block>(){
        {
            add(Blocks.prismarine);
            add(Blocks.stained_glass_pane);
            add(Blocks.stained_glass);
            add(Blocks.wool);
        }
    };
    long treasureInitialTime;

    float playerYaw;
    BlockPos uTurnCachePos;

    Rotation rotation = new Rotation();

    State currentState;
    State treasureCacheState;
    int centerToBlockTick = 0;

    boolean centering = false;

    int turnState = 0;

    enum State {
        NORMAL,
        TREASURE,
        UTurn
    }


    @Override
    public void onEnable() {

        Main.configFile.hardIndex = MightyMiner.config.powAuraType;
        Main.configFile.includeOres = true;
        currentState = State.NORMAL;
        turnState = 1;
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
        blocksAllowedToMine.add(Blocks.lapis_ore);
        blocksAllowedToMine.add(Blocks.lit_redstone_ore);
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

        if(PlayerUtils.hasPlayerInsideRadius(MightyMiner.config.powPlayerRad)){
            PlayerUtils.warpBackToIsland();
            MacroHandler.disableScript();
            Main.autoHardStone = false;
            return;
        }

        if(rotation.rotating){
            KeybindHandler.resetKeybindState();
            return;
        }



        updateState();

        if(centering) {
            if(centerToBlockTick == 0) {
                KeybindHandler.resetKeybindState();
                centerToBlockTick = 20;
            }

            if(centerToBlockTick == 10)
                PlayerUtils.centerToBlock();

            centerToBlockTick --;

            if(centerToBlockTick == 0)
                centering = false;
            return;
        }

        switch (currentState){
            case TREASURE:
                Main.autoHardStone = false;
                KeybindHandler.resetKeybindState();
                break;

            case NORMAL: case UTurn:
                if(MightyMiner.config.powStoneAura) {
                    Main.autoHardStone = !frontShouldMineSlow();
                    rotation.intLockAngle(playerYaw, (shouldLookDown() ? 60 : (frontShouldMineSlow() ? 27 : 0)), 200);
                } else
                    rotation.intLockAngle(playerYaw, (shouldLookDown() ? 60 : 27), 200);

                KeybindHandler.setKeyBindState(KeybindHandler.keybindW, true);
                KeybindHandler.setKeyBindState(KeybindHandler.keybindAttack, mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && mc.objectMouseOver.getBlockPos().getY() >= (int)mc.thePlayer.posY);
                break;
        }

        if(rotation.rotating){
            KeybindHandler.resetKeybindState();
        }

    }

    private void updateState(){
        switch (currentState) {
            case TREASURE:
                if(System.currentTimeMillis() - treasureInitialTime > 7000) {
                    LogUtils.debugLog("Completed treasure due to timeout");
                    currentState = treasureCacheState;
                }
                break;
            case NORMAL:
                if(!blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(0, 0, 1)) || !blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(0, 1, 1))) {
                    if(isInCenterOfBlock()) {
                        turnState = 1 - turnState;
                        playerYaw = AngleUtils.get360RotationYaw(playerYaw + getRotAmount());
                        uTurnCachePos = BlockUtils.getPlayerLoc();
                        currentState = State.UTurn;
                        KeybindHandler.resetKeybindState();
                    }
                }
                break;
            case UTurn:
                if (MathUtils.getDistanceBetweenTwoBlock(BlockUtils.getPlayerLoc(), uTurnCachePos) > 4) {
                    if(isInCenterOfBlock()) {
                        playerYaw = AngleUtils.get360RotationYaw(playerYaw + getRotAmount());
                        if (MightyMiner.config.powCenter)
                            centering = true;
                        currentState = State.NORMAL;
                    }
                }
                break;
        }
    }

    @Override
    public void onLastRender() {
        if(rotation.rotating)
            rotation.update();
    }

    @Override
    public void onMessageReceived(String message){
        if(message.contains("You have successfully picked the lock on this chest")){
            currentState = treasureCacheState;
            LogUtils.debugLog("Completed treasure");
        }
        if(message.contains("You uncovered a treasure chest!")){
            if(currentState != State.TREASURE)
                treasureCacheState = currentState;
            currentState = State.TREASURE;

            KeybindHandler.resetKeybindState();
            treasureInitialTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onPacketReceived(Packet<?> packet){
        if(currentState == State.TREASURE && packet instanceof S2APacketParticles){
            if(((S2APacketParticles) packet).getParticleType() == EnumParticleTypes.CRIT){
                try {
                    BlockPos closetChest = BlockUtils.findBlock(18, Blocks.chest, Blocks.trapped_chest).get(0);
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

    int getRotAmount(){
        //check blacklisted blocks
        if(!(blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(-1, 0, 0))) || !(blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(1, 0, 0)))
                || !(blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(-1, 1, 0))) || !(blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(1, 1, 0)))){

            //check which side is possible to walk, if none, 180
            return  (blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(-1, 0, 0)) && blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(-1, 1, 0))) ? (-90)
                    : (blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(1, 0, 0)) && blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(1, 1, 0)) ? 90 : 180);
        } else
            return turnState == 1 ? 90 : -90;
        // both sides can be walked, oscillate between 90 and -90 to increase area mined
    }
    boolean shouldLookDown(){
        return (AngleUtils.shouldLookAtCenter(BlockUtils.getRelativeBlockPos(0, 0, 1)) && BlockUtils.isPassable(BlockUtils.getRelativeBlock(0, 1, 1)))
        || (AngleUtils.shouldLookAtCenter(BlockUtils.getRelativeBlockPos(0, 0, 0)) && BlockUtils.isPassable(BlockUtils.getRelativeBlock(0, 1, 0)));
    }
    boolean frontShouldMineSlow(){
        return mineSlowBlocks.contains(BlockUtils.getRelativeBlock(0, 0, 1)) || mineSlowBlocks.contains(BlockUtils.getRelativeBlock(0, 1, 1))
                || BlockUtils.getRelativeBlock(0, 0, 0).equals(Blocks.stained_glass_pane) ||  BlockUtils.getRelativeBlock(0, 1, 0).equals(Blocks.stained_glass_pane) ;
    }
    boolean isInCenterOfBlock() {
        return (Math.round(AngleUtils.get360RotationYaw()) == 180 || Math.round(AngleUtils.get360RotationYaw()) == 0) ? Math.abs(Minecraft.getMinecraft().thePlayer.posZ) % 1 > 0.3f && Math.abs(Minecraft.getMinecraft().thePlayer.posZ) % 1 < 0.7f :
                Math.abs(Minecraft.getMinecraft().thePlayer.posX) % 1 > 0.3f && Math.abs(Minecraft.getMinecraft().thePlayer.posX) % 1 < 0.7f;
    }
}
