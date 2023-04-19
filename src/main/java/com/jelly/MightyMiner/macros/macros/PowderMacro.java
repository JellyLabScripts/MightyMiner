package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.features.Autosell;
import com.jelly.MightyMiner.features.RGANuker;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.utils.*;
import com.jelly.MightyMiner.utils.BlockUtils.BlockData;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.BlockUtils.Box;
import com.jelly.MightyMiner.utils.BlockUtils.OffsetBox;
import com.jelly.MightyMiner.utils.PlayerUtils;
import com.jelly.MightyMiner.utils.Timer;
import com.jelly.MightyMiner.utils.Utils.MathUtils;
import com.jelly.MightyMiner.utils.Utils.ThreadUtils;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.jelly.MightyMiner.utils.AngleUtils.*;
import static com.jelly.MightyMiner.utils.BlockUtils.BlockUtils.*;


public class PowderMacro extends Macro {

    List<Block> blocksAllowedToMine = new ArrayList<Block>() {
        {
            add(Blocks.stone);
            add(Blocks.air);
            add(Blocks.coal_ore);
            add(Blocks.iron_ore);
            add(Blocks.emerald_ore);
            add(Blocks.gold_ore);
            add(Blocks.redstone_ore);
            add(Blocks.lapis_ore);
            add(Blocks.lit_redstone_ore);
            add(Blocks.diamond_ore);
            add(Blocks.prismarine);
            add(Blocks.chest);
        }
    };
    List<Block> mineSlowBlocks = new ArrayList<Block>(){
        {
            add(Blocks.prismarine);
            add(Blocks.stained_glass_pane);
            add(Blocks.stained_glass);
            add(Blocks.wool);
        }
    };
    //base stuff
    Rotation rotation = new Rotation();
    AutoMineBaritone mineBaritone;
    BlockRenderer renderer = new BlockRenderer();
    ExecutorService executor = Executors.newSingleThreadExecutor();

    //states
    State currentState;
    State treasureCacheState;
    TreasureState treasureState = TreasureState.INIT;

    enum State {
        NORMAL,
        TREASURE,
        UTurn
    }

    enum TreasureState {
        INIT,
        WALKING,
        SOLVING,
        FINISHED,
        RETURNING

    }

    //mechanics
    long treasureInitialTime;
    float playerYaw;
    int turnState = 0;
    BlockPos uTurnCachePos;

    //chests
    BlockPos currentChest;
    volatile Queue<BlockPos> chestQueue = new LinkedList<>();
    volatile CircularFifoQueue<BlockPos> solvedOrSolvingChests = new CircularFifoQueue<>(3);
    volatile BlockPos returnBlockPos;
    BlockPos targetBlockPos;

    //aote
    boolean aote;
    boolean saved;
    int aoteTick;
    float savedPitch;
    int savedItemIndex;

    //antistuck
    double lastX;
    double lastZ;
    final Timer cooldown = new Timer();


    @Override
    public void onEnable() {
        if(MightyMiner.config.playerFailsafe) {
            if (PlayerUtils.isNearPlayer(MightyMiner.config.playerRad)) {
                LogUtils.addMessage("Not starting, there is a player nearby");
                this.toggle();
                return;
            }
        }
        if(MightyMiner.config.powBlueCheeseSwitch){
            if(PlayerUtils.getItemInHotbarFromLore(true, "Blue Cheese") == -1){
                LogUtils.addMessage("You don't have a blue cheese drill. Switch disabled");
                MightyMiner.config.powBlueCheeseSwitch = false;
            }
        }

        if(PlayerUtils.getItemInHotbar(true, "Drill", "Gauntlet", "Pickonimbus", "Pickaxe") == -1) {
            LogUtils.addMessage("You don't have any mining tool!");
            this.toggle();
            return;
        }

        if (MightyMiner.config.powMineGemstone && PlayerUtils.getItemInHotbar(true, "Drill", "Gauntlet", "Pickonimbus") == -1) {
            LogUtils.addMessage("You don't have a drill, gauntlet or pickonimbus in your hotbar to mine gemstones (in case of pathing)");
            this.toggle();
            return;
        }


        mineBaritone = new AutoMineBaritone(getAutomineConfig());

        rotation.reset();

        currentState = State.NORMAL;
        treasureState = TreasureState.INIT;
        turnState = 1;
        treasureInitialTime = System.currentTimeMillis();
        playerYaw = AngleUtils.getClosest();

        chestQueue.clear();
        solvedOrSolvingChests.clear();
        renderer.renderMap.clear();

        aote = false;

        if(MightyMiner.config.powMineGemstone && !blocksAllowedToMine.contains(Blocks.stained_glass)){
            blocksAllowedToMine.add(Blocks.stained_glass_pane);
            blocksAllowedToMine.add(Blocks.stained_glass);
        } else if(!MightyMiner.config.powMineGemstone && blocksAllowedToMine.contains(Blocks.stained_glass)){
            blocksAllowedToMine.removeIf(a -> a.equals(Blocks.stained_glass) || a.equals(Blocks.stained_glass_pane));
        }

    }

    @Override
    public void onDisable() {
        RGANuker.enabled = false;
        Autosell.disable();
        aote = false;
        if (mineBaritone != null) // nullpointerexception crash sometimes if detected player right after turning on the macro
            mineBaritone.disableBaritone();
        KeybindHandler.resetKeybindState();
    }

    @Override
    public void onTick(TickEvent.Phase phase){
        if(phase != TickEvent.Phase.START || !enabled)
            return;

        if(!RGANuker.enabled && MightyMiner.config.powNuker){
            RGANuker.enabled = true;
        }

        if(MightyMiner.config.powAutosell){
            if(Autosell.isEnabled())
                return;
            if(Autosell.shouldStart()) {
                mineBaritone.disableBaritone();
                Autosell.enable();
                return;
            }
        }

        if(rotation.rotating){
            KeybindHandler.resetKeybindState();
            return;
        }

        updateState();

        if(aote || (currentState != State.NORMAL && currentState != State.UTurn)){
            // disable antistuck
            lastX = 10000;
            lastZ = 10000;
            cooldown.reset();
        }

        //aote
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
                rotation.initAngleLock(AngleUtils.get360RotationYaw(), 89, 500);
                return;
            }


            mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Void", "End");
            KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, true);


            if (BlockUtils.inCenterOfBlock() || !PlayerUtils.isPossibleToAOTE()) {
                aote = false;
                rotation.reset();
                rotation.easeTo(AngleUtils.get360RotationYaw(), savedPitch, 500);
                mc.thePlayer.inventory.currentItem = savedItemIndex;
                KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, false);
            }
            return;
        } else {
            saved = false;
        }

        //state handling
        switch (currentState){
            case TREASURE:

                switch(treasureState){
                    case INIT: case SOLVING:
                        if(MightyMiner.config.powBlueCheeseSwitch)
                            mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbarFromLore(true, "Blue Cheese");
                        KeybindHandler.resetKeybindState();
                        break;
                    case WALKING:
                        if(targetBlockPos == null) return;

                        switch(mineBaritone.getState()){
                            case IDLE:
                                if(BlockUtils.getPlayerLoc().equals(targetBlockPos))
                                    treasureState = TreasureState.SOLVING;
                                else {
                                    mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Drill", "Gauntlet", "Pickonimbus");
                                    mineBaritone.goTo(targetBlockPos);
                                }
                                break;
                            case FAILED:
                                terminateTreasureSolving();
                                break;
                        }
                        break;
                    case RETURNING:

                        switch(mineBaritone.getState()){
                            case IDLE:
                                if(returnBlockPos == null || BlockUtils.getPlayerLoc().equals(returnBlockPos)){
                                    mineBaritone.disableBaritone();
                                    currentState = treasureCacheState;
                                } else mineBaritone.goTo(returnBlockPos);
                                break;
                            case FAILED:
                                terminateTreasureSolving();
                                break;
                        }
                        break;

                }
                break;

            case UTurn: case NORMAL:

                if (cooldown.hasReached(5000)) {
                    if(Math.abs(mc.thePlayer.posX - lastX) < 0.2f && Math.abs(mc.thePlayer.posZ - lastZ) < 0.2f){
                        new Thread(antistuck).start();
                    }
                    cooldown.reset();
                    lastX = mc.thePlayer.posX;
                    lastZ = mc.thePlayer.posZ;
                }

                if(frontShouldMineSlow()){
                    RGANuker.enabled = false;
                    if(havePotentialObstacles()
                            && !BlockUtils.getRelativeBlock(0, 0, 0).equals(Blocks.stained_glass_pane) && !BlockUtils.getRelativeBlock(0, 1, 0).equals(Blocks.stained_glass_pane)
                            && PlayerUtils.notAtCenter(playerYaw)
                            && MightyMiner.config.powCenter
                            && MightyMiner.config.powMineGemstone)
                        aote = true;
                }

                if(MightyMiner.config.powNuker) {
                    rotation.initAngleLock(playerYaw, (shouldLookDown() ? 60 : (frontShouldMineSlow() ? 27 : 0)), 200);
                } else if(frontShouldMineSlow())
                    rotation.initAngleLock(playerYaw, shouldLookDown() ? 60 : 27, 200);


                if(MightyMiner.config.powPickaxeSwitch)
                    mc.thePlayer.inventory.currentItem = frontShouldMineSlow() ? PlayerUtils.getItemInHotbar("Drill", "Gauntlet") : PlayerUtils.getItemInHotbar("Pickaxe");
                else
                    mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Drill", "Gauntlet", "Pickaxe");

                KeybindHandler.setKeyBindState(KeybindHandler.keybindW, shouldWalkForward() || MightyMiner.config.powNuker || currentState == State.UTurn);
                KeybindHandler.setKeyBindState(KeybindHandler.keybindAttack, mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && mc.objectMouseOver.getBlockPos().getY() >= (int)mc.thePlayer.posY);
                this.checkMiningSpeedBoost();
                break;
        }



    }

    private void updateState(){

        if(!chestQueue.isEmpty()) {
            if(currentState != State.TREASURE)
                treasureState = TreasureState.INIT;

            currentState = State.TREASURE;
        }

        switch (currentState) {
            case TREASURE:
                switch (treasureState){
                    case INIT:
                        treasureInitialTime = System.currentTimeMillis();
                        currentChest = chestQueue.poll();
                        for(BlockPos blockPos : BlockUtils.getAllBlocksInLine2d(BlockUtils.getPlayerLoc(), currentChest)){
                            if(MathUtils.getDistanceBetweenTwoBlock(currentChest, blockPos) < 3.3f && !BlockUtils.getBlock(blockPos).equals(Blocks.chest) && !BlockUtils.getBlock(blockPos.down()).equals(Blocks.air)){
                                targetBlockPos = blockPos;
                                break;
                            }
                        }

                        returnBlockPos = null;
                        for (int i = 0; i < 7; i++) {
                            if (BlockUtils.getRelativeBlockPos(0, 0, i).equals(currentChest) || BlockUtils.getRelativeBlockPos(0, 1, i).equals(currentChest)) {
                                returnBlockPos = BlockUtils.getRelativeBlockPos(0, 0, i + 1);
                            }
                        }
                        if(returnBlockPos == null){
                            if(!isInsideBox(new OffsetBox(1, -1, 1, 0, 7, 0), targetBlockPos, playerYaw))
                                returnBlockPos = BlockUtils.getPlayerLoc();
                        }
                        treasureState = TreasureState.WALKING;

                        break;
                    case FINISHED:
                        treasureState = chestQueue.isEmpty() ? TreasureState.RETURNING : TreasureState.INIT;
                        break;
                }
                if((System.currentTimeMillis() - treasureInitialTime) / 1000f > 10 && treasureState != TreasureState.RETURNING) {
                    treasureState = TreasureState.RETURNING;
                    LogUtils.debugLog("Completed treasure due to timeout");
                    chestQueue.poll();
                    return;
                }
            case NORMAL:
                if(shouldTurn(5)) {
                    turnState = 1 - turnState;
                    uTurnCachePos = BlockUtils.getPlayerLoc();
                    playerYaw = AngleUtils.get360RotationYaw(playerYaw + getRotAmount());
                    currentState = State.UTurn;
                }
                break;
            case UTurn:
                if (MathUtils.getDistanceBetweenTwoBlock(BlockUtils.getPlayerLoc(), uTurnCachePos) > MightyMiner.config.powLaneWidth || shouldTurn(3)) {
                    playerYaw = AngleUtils.get360RotationYaw(playerYaw + getRotAmount());
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

        if(rotation.rotating){
            rotation.update();
        } else if(!frontShouldMineSlow()
                && (currentState == State.UTurn || currentState == State.NORMAL)
                && !aote
                && !MightyMiner.config.powNuker
                && !Autosell.isEnabled()
                && !(mineBaritone.getState() == AutoMineBaritone.BaritoneState.EXECUTING)){
            rotation.updateInCircle(MightyMiner.config.powRotateRadius, 3, playerYaw, MightyMiner.config.powRotateRate);
        }
    }

    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event) {
        if(event.type == RenderGameOverlayEvent.ElementType.TEXT){
            mc.fontRendererObj.drawString(currentState + " " + treasureState, 5 , 5, -1);
            mc.fontRendererObj.drawString("Chests in waiting queue: " + chestQueue.size() + " | Chests in finished queue: " + solvedOrSolvingChests.size() + "/3", 5 , 17, -1);
        }
    }

    @Override
    public void onMessageReceived(String message){
        if(message.contains("You have successfully picked the lock on this chest")){
            treasureState = TreasureState.FINISHED;
        }
        if(message.contains("You uncovered a treasure chest!")){
            KeybindHandler.resetKeybindState();
            executor.submit(addChestToQueue);
        }
    }


    @Override
    public void onPacketReceived(Packet<?> packet){
        if(currentState == State.TREASURE && treasureState == TreasureState.SOLVING && treasureInitialTime > 200 && packet instanceof S2APacketParticles && currentChest != null){
            if(((S2APacketParticles) packet).getParticleType() == EnumParticleTypes.CRIT){
                try {
                    if(Math.abs((((S2APacketParticles) packet).getXCoordinate()) - (currentChest.getX() + 0.5f)) < 0.7f && Math.abs((((S2APacketParticles) packet).getYCoordinate()) - (currentChest.getY() + 0.5f)) < 0.7f && Math.abs((((S2APacketParticles) packet).getZCoordinate()) - (currentChest.getZ() + 0.5f)) < 0.7f) {
                        rotation.initAngleLock(
                                AngleUtils.getRequiredYaw(((S2APacketParticles) packet).getXCoordinate() - mc.thePlayer.posX, ((S2APacketParticles) packet).getZCoordinate() - mc.thePlayer.posZ),
                                AngleUtils.getRequiredPitch(((S2APacketParticles) packet).getXCoordinate() - mc.thePlayer.posX, (((S2APacketParticles) packet).getYCoordinate()) - (mc.thePlayer.posY + 1.62d), ((S2APacketParticles) packet).getZCoordinate() - mc.thePlayer.posZ),
                                300);
                    }
                }catch (Exception ignored){}
            }
        }
    }

    void terminateTreasureSolving(){
        mineBaritone.disableBaritone();
        if(chestQueue.isEmpty()) {
            currentState = treasureCacheState;
        } else {
            treasureState = TreasureState.INIT;
        }
    }


    Runnable addChestToQueue = () -> {

        ThreadUtils.sleep(200);
        Logger.log("Adding chest to queue");

        List<BlockPos> foundBlocks = BlockUtils.findBlock(new Box(-7, 7, 3, 0, -7, 7),
                new ArrayList<>(solvedOrSolvingChests), 0, 256, new BlockData<>(Blocks.chest));

        if(foundBlocks.isEmpty()){
            LogUtils.addMessage("That chest was impossible to solve");
            return;
        }

        BlockPos chest = foundBlocks.get(0);
        solvedOrSolvingChests.add(chest);
        chestQueue.add(chest);

        if(currentState != State.TREASURE)
            treasureCacheState = currentState;
    };

    boolean shouldWalkForward(){
        int blocksEmpty = 0;
        for(int i = 1; i <= 4; i++){
            if(i <= 3){
                if(mineSlowBlocks.contains(BlockUtils.getRelativeBlock(0, 0, i, playerYaw)) || mineSlowBlocks.contains(BlockUtils.getRelativeBlock(0, 1, i, playerYaw)))
                    return true;
            }
            BlockPos check = BlockUtils.getRelativeBlockPos(0, 0, i, playerYaw);
            if(getBlock(check).equals(Blocks.air) || mineSlowBlocks.contains(getBlock(check)))
                ++blocksEmpty;
            if(getBlock(check.up()).equals(Blocks.air) || mineSlowBlocks.contains(getBlock(check.up())))
                ++blocksEmpty;
        }
        return blocksEmpty >= 6;
    }


    int getRotAmount(){
        // see if have obstacles sideways
        if(scanBox(new OffsetBox(-3, 3, 1, 0, 0, 0), blocksAllowedToMine, null, playerYaw)){
            if(scanBox(new OffsetBox(3, 0, 1, 0, 0, 0), blocksAllowedToMine, null, playerYaw))
                return -90;
            else if(scanBox(new OffsetBox(-3, 0, 1, 0, 0, 0), blocksAllowedToMine, null, playerYaw))
                return 90 ;
            else return 180;
        } else
            return turnState == 1 ? 90 : -90; // both sides can be walked, oscillate between 90 and -90 to increase area mined
    }


    boolean shouldLookDown(){
        BlockPos front = getRelativeBlockPos(0, 0, 1);
        return (shouldLookAtCenter(front) && isPassable(front.up())) || (shouldLookAtCenter(getPlayerLoc()) && isPassable(getPlayerLoc().up()));
    }

    boolean frontShouldMineSlow(){
        BlockPos front = getRelativeBlockPos(0, 0, 1);
        return mineSlowBlocks.contains(getBlock(front)) || mineSlowBlocks.contains(getBlock(front.up())) || getBlock(getPlayerLoc()).equals(Blocks.stained_glass_pane) ||  getBlock(getPlayerLoc().up()).equals(Blocks.stained_glass_pane);
    }

    boolean havePotentialObstacles(){
        return scanBox(new OffsetBox(1, -1, 0, 1, 0, 1), null, mineSlowBlocks, playerYaw)
                || getBlock(getPlayerLoc()).equals(Blocks.stained_glass_pane) || getBlock(getPlayerLoc().up()).equals(Blocks.stained_glass_pane);
    }

    boolean shouldTurn(int checkRadius){
        return scanBox(new OffsetBox(0, 0, 0, 1, 0, checkRadius), blocksAllowedToMine, null, playerYaw);
    }

    Runnable antistuck = () -> {
        ThreadUtils.sleep(20);
        KeybindHandler.setKeyBindState(KeybindHandler.keybindS, true);
        ThreadUtils.sleep(350);
        KeybindHandler.setKeyBindState(KeybindHandler.keybindS, false);
        KeybindHandler.setKeyBindState(KeybindHandler.keybindD, true);
        ThreadUtils.sleep(350);
        KeybindHandler.setKeyBindState(KeybindHandler.keybindD, false);
        KeybindHandler.setKeyBindState(KeybindHandler.keybindA, true);
        ThreadUtils.sleep(350);
        KeybindHandler.setKeyBindState(KeybindHandler.keybindA, false);
    };


    BaritoneConfig getAutomineConfig(){
        return new BaritoneConfig(
                MiningType.DYNAMIC,
                false,
                true,
                false,
                250,
                8,
                new ArrayList<Block>(){
                    {
                        add(Blocks.chest);
                    }
                },
                null,
                256,
                0
        );
    }
}
