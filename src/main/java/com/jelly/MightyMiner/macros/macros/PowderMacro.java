package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.features.RGANuker;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.utils.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.awt.*;
import java.util.*;
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

    Queue<BlockPos> chestQueue = new LinkedList<>();
    CircularFifoQueue<BlockPos> solvedOrSolvingChests = new CircularFifoQueue<>(3);

    BlockPos uTurnCachePos;

    Rotation rotation = new Rotation();

    State currentState;
    State treasureCacheState;
    TreasureState treasureState = TreasureState.NONE;

    boolean aote;
    boolean saved;
    int aoteTick;
    float savedPitch;
    int savedItemIndex;

    int turnState = 0;
    AutoMineBaritone mineBaritone;
    BlockPos currentChest;
    BlockPos returnBlockPos;
    BlockPos targetBlockPos;

    BlockRenderer renderer = new BlockRenderer();

    enum State {
        NORMAL,
        PAUSED,
        TREASURE,
        UTurn
    }


    enum TreasureState {
        NONE,
        WALKING,
        SOLVING,
        FINISHED,
        RETURNING

    }

    State prePauseState;


    @Override
    public boolean isPaused() {
        return currentState == State.PAUSED;
    }

    @Override
    public void Pause() {
        prePauseState = currentState;
        currentState = State.PAUSED;
    }

    @Override
    public void Unpause() {
        currentState = prePauseState;
        prePauseState = null;
    }


    @Override
    public void onEnable() {
        if (isPaused()) {
            Unpause();
        }
        if(MightyMiner.config.powPlayerFailsafe) {
            if (PlayerUtils.isNearPlayer(MightyMiner.config.powPlayerRad)) {
                LogUtils.addMessage("Not starting, there is a player nearby");
                this.toggle();
                return;
            }
        }

        mineBaritone = new AutoMineBaritone(getAutomineConfig());

        currentState = State.NORMAL;
        treasureState = TreasureState.NONE;
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
        blocksAllowedToMine.add(Blocks.chest);
        blocksAllowedToMine.add(Blocks.trapped_chest);

        chestQueue.clear();
        solvedOrSolvingChests.clear();
        renderer.renderMap.clear();


        if(MightyMiner.config.powMineGemstone){
            blocksAllowedToMine.add(Blocks.stained_glass_pane);
            blocksAllowedToMine.add(Blocks.stained_glass);
        }


    }

    @Override
    public void onDisable() {
        RGANuker.enabled = false;
        aote = false;
        mineBaritone.disableBaritone();
        KeybindHandler.resetKeybindState();
    }

    @Override
    public void onTick(TickEvent.Phase phase){
        if(phase != TickEvent.Phase.START)
            return;

        if(MightyMiner.config.powPlayerFailsafe) {
            if(PlayerUtils.isNearPlayer(MightyMiner.config.powPlayerRad)){
                PlayerUtils.warpBackToIsland();
                MacroHandler.disableScript();
                return;
            }
        }


        if(!RGANuker.enabled && MightyMiner.config.powNuker){
            RGANuker.enabled = true;
        }


        if(rotation.rotating){
            KeybindHandler.resetKeybindState();
            return;
        }


        updateState();

        if (aote) {
            if(!saved){
                rotation.reset();
                KeybindHandler.resetKeybindState();
                savedPitch = mc.thePlayer.rotationPitch;
                savedItemIndex = mc.thePlayer.inventory.currentItem;
                aoteTick = 12;
                saved = true;
            }

            if(aoteTick > 0) {
                aoteTick --;
                rotation.intLockAngle(AngleUtils.get360RotationYaw(), 89, 500);
                return;
            }


            mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Void", "End");
            KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, true);


            if (BlockUtils.inCenterOfBlock()) {
                aote = false;
                rotation.reset();
                rotation.easeTo(AngleUtils.get360RotationYaw(), savedPitch, 500);
                mc.thePlayer.inventory.currentItem = savedItemIndex;
                KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, false);
            }
            return;
        } else {
            if(rotation.rotating)
                rotation.reset();
            saved = false;
        }

        switch (currentState){
            case TREASURE:
                switch(treasureState){
                    case NONE: case SOLVING:
                        KeybindHandler.resetKeybindState();
                        break;
                    case WALKING:
                        if(targetBlockPos == null) return;

                        switch(mineBaritone.getState()){
                            case IDLE:
                                if(BlockUtils.getPlayerLoc().equals(targetBlockPos))
                                    treasureState = TreasureState.SOLVING;
                                else
                                    mineBaritone.goTo(targetBlockPos);
                                break;
                            case FAILED:
                                mineBaritone.goTo(targetBlockPos);
                                break;

                        }
                        break;
                    case RETURNING:
                        if(returnBlockPos == null) return;

                        switch(mineBaritone.getState()){
                            case IDLE:
                                if(BlockUtils.getPlayerLoc().equals(returnBlockPos)){
                                    currentState = treasureCacheState;
                                } else {
                                    LogUtils.debugLog("Going back to original position");
                                    mineBaritone.goTo(returnBlockPos);
                                }
                                break;
                            case FAILED:
                                mineBaritone.goTo(returnBlockPos);
                                break;

                        }
                        break;

                }
                break;

            case NORMAL: case UTurn:
                if(MightyMiner.config.powNuker) {
                    rotation.intLockAngle(playerYaw, (shouldLookDown() ? 60 : (frontShouldMineSlow() ? 27 : 0)), 200);
                } else
                    rotation.intLockAngle(playerYaw, (shouldLookDown() ? 60 : 27), 200);

                if(frontShouldMineSlow() && MightyMiner.config.powNuker){
                    RGANuker.enabled = false;
                    if(frontHaveObstacles() && notAtCenter() && MightyMiner.config.powCenter && MightyMiner.config.powMineGemstone)
                        aote = true;
                }

                if(MightyMiner.config.powPickaxeSwitch)
                    mc.thePlayer.inventory.currentItem = frontShouldMineSlow() ? PlayerUtils.getItemInHotbar("Drill", "Gauntlet") : PlayerUtils.getItemInHotbar("Pickaxe");
                else
                    mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Drill", "Gauntlet", "Pickaxe");


                KeybindHandler.setKeyBindState(KeybindHandler.keybindW, true);
                KeybindHandler.setKeyBindState(KeybindHandler.keybindAttack, mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && mc.objectMouseOver.getBlockPos().getY() >= (int)mc.thePlayer.posY);
                useMiningSpeedBoost();
                break;
            case PAUSED:
                KeybindHandler.resetKeybindState();
                break;
        }

        if(rotation.rotating){
            KeybindHandler.resetKeybindState();
        }

    }

    private void updateState(){

        if(!chestQueue.isEmpty()) {
            if(currentState != State.TREASURE)
                treasureState = TreasureState.NONE;

            currentState = State.TREASURE;
        }


        switch (currentState) {
            case TREASURE:
                switch (treasureState){
                    case NONE:
                        treasureInitialTime = System.currentTimeMillis();
                        currentChest = chestQueue.poll();
                        for(BlockPos blockPos : BlockUtils.getRasterizedBlocks(BlockUtils.getPlayerLoc(), currentChest)){
                            if(MathUtils.getDistanceBetweenTwoBlock(currentChest, blockPos) < 3.5f){
                                targetBlockPos = blockPos;
                                break;
                            }
                        }
                        treasureState = TreasureState.WALKING;
                        break;
                    case FINISHED:
                        treasureState = chestQueue.isEmpty() ? TreasureState.RETURNING : TreasureState.NONE;
                        break;
                }
                if(System.currentTimeMillis() - treasureInitialTime > 7000 && treasureState != TreasureState.RETURNING) {
                    treasureState = TreasureState.RETURNING;
                    LogUtils.debugLog("Completed treasure due to timeout");
                    chestQueue.poll();
                    return;
                }
            case NORMAL:
                if(!blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(0, 0, 1)) || !blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(0, 1, 1))) {
                    turnState = 1 - turnState;
                    playerYaw = AngleUtils.get360RotationYaw(playerYaw + getRotAmount());
                    uTurnCachePos = BlockUtils.getPlayerLoc();
                    currentState = State.UTurn;
                    KeybindHandler.resetKeybindState();
                }
                break;
            case UTurn:
                if (MathUtils.getDistanceBetweenTwoBlock(BlockUtils.getPlayerLoc(), uTurnCachePos) > MightyMiner.config.powLaneWidth
                        || !blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(0, 0, 1)) || !blocksAllowedToMine.contains(BlockUtils.getRelativeBlock(0, 1, 1))) {
                    playerYaw = AngleUtils.get360RotationYaw(playerYaw + getRotAmount());
                    if (MightyMiner.config.powCenter) {
                        aote = true;
                    }
                    currentState = State.NORMAL;
                }
                break;
        }
    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {

        if(!chestQueue.isEmpty())
            chestQueue.forEach(a -> renderer.renderAABB(a, Color.BLUE));
        if(!solvedOrSolvingChests.isEmpty())
            solvedOrSolvingChests.forEach(a -> renderer.renderAABB(a, Color.GREEN));
        if(targetBlockPos != null)
            renderer.renderAABB(targetBlockPos, Color.BLACK);


        if(rotation.rotating)
            rotation.update();
    }

    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event) {
        if(event.type == RenderGameOverlayEvent.ElementType.TEXT){
            mc.fontRendererObj.drawString(currentState + " " + treasureState, 5 , 5, -1);
        }
    }

    @Override
    public void onMessageReceived(String message){
        if(message.contains("You have successfully picked the lock on this chest")){
            treasureState = TreasureState.FINISHED;
        }
        if(message.contains("You uncovered a treasure chest!")){
            KeybindHandler.resetKeybindState();
            addChestToQueue();
        }
    }


    @Override
    public void onPacketReceived(Packet<?> packet){
        if(currentState == State.TREASURE && treasureState == TreasureState.SOLVING && treasureInitialTime > 200 && packet instanceof S2APacketParticles && currentChest != null){
            if(((S2APacketParticles) packet).getParticleType() == EnumParticleTypes.CRIT){
                try {
                    if(Math.abs((((S2APacketParticles) packet).getXCoordinate()) - currentChest.getX()) < 1.5f && Math.abs((((S2APacketParticles) packet).getYCoordinate()) - currentChest.getY()) < 1.5f && Math.abs((((S2APacketParticles) packet).getZCoordinate()) - currentChest.getZ()) < 1.5f) {
                        rotation.intLockAngle(
                                AngleUtils.getRequiredYaw(((S2APacketParticles) packet).getXCoordinate() - mc.thePlayer.posX, ((S2APacketParticles) packet).getZCoordinate() - mc.thePlayer.posZ),
                                AngleUtils.getRequiredPitch(((S2APacketParticles) packet).getXCoordinate() - mc.thePlayer.posX, (((S2APacketParticles) packet).getYCoordinate()) - (mc.thePlayer.posY + 1.62d), ((S2APacketParticles) packet).getZCoordinate() - mc.thePlayer.posZ),
                                50);
                    }
                }catch (Exception ignored){}
            }
        }
    }

    void addChestToQueue(){
        new Thread(() -> {
            ThreadUtils.sleep(200);
            List<BlockPos> foundBlocks = BlockUtils.findBlock(new Box(-7, 7, 4, 0, -7, 7), new ArrayList<>(solvedOrSolvingChests), 0, 256, Blocks.chest, Blocks.trapped_chest);
            if(foundBlocks.isEmpty()){
                LogUtils.debugLog("That chest was impossible to solve");
            }
            BlockPos chest = foundBlocks.get(0);

            solvedOrSolvingChests.add(chest);

            Logger.log("Adding chest to queue");
            if(currentState != State.TREASURE) {
                treasureCacheState = currentState;
                returnBlockPos = BlockUtils.getPlayerLoc();
                for (int i = 0; i < 5; i++) {
                    if (BlockUtils.getRelativeBlockPos(0, 0, i).equals(chest) || BlockUtils.getRelativeBlockPos(0, 1, i).equals(chest)) {
                        returnBlockPos = BlockUtils.getRelativeBlockPos(0, 0, i + 1);
                    }
                }
            }
            chestQueue.add(chest);
        }).start();
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

    boolean frontHaveObstacles(){
        return mineSlowBlocks.contains(BlockUtils.getRelativeBlock(0, 0, 1)) || mineSlowBlocks.contains(BlockUtils.getRelativeBlock(0, 1, 1)) ||
                mineSlowBlocks.contains(BlockUtils.getRelativeBlock(1, 0, 1)) || mineSlowBlocks.contains(BlockUtils.getRelativeBlock(1, 0, 1)) ||
                mineSlowBlocks.contains(BlockUtils.getRelativeBlock(-1, 0, 1)) || mineSlowBlocks.contains(BlockUtils.getRelativeBlock(-1, 0, 1))
                || BlockUtils.getRelativeBlock(0, 0, 0).equals(Blocks.stained_glass_pane) ||  BlockUtils.getRelativeBlock(0, 1, 0).equals(Blocks.stained_glass_pane) ;
    }
    boolean notAtCenter(){
        // not at center
        return !((AngleUtils.get360RotationYaw() % 180 == 0) ?
                Math.abs(mc.thePlayer.posX) % 1 > 0.3f && Math.abs(mc.thePlayer.posX) % 1 < 0.7f : Math.abs(mc.thePlayer.posZ) % 1 > 0.3f && Math.abs(mc.thePlayer.posZ) % 1 < 0.7f);


    }

    BaritoneConfig getAutomineConfig(){
        return new BaritoneConfig(
                AutoMineType.DYNAMIC,
                false,
                true,
                false,
                250,
                8,
                new ArrayList<Block>(){
                    {
                        add(Blocks.chest);
                        add(Blocks.trapped_chest);
                    }
                },
                null,
                256,
                0
        );
    }
}
