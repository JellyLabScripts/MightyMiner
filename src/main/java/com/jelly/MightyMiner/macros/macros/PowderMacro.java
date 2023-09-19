package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.*;
import com.jelly.MightyMiner.utils.BlockUtils.BlockData;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.BlockUtils.Box;
import com.jelly.MightyMiner.utils.Utils.MathUtils;
import com.jelly.MightyMiner.utils.Utils.ThreadUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jelly.MightyMiner.utils.BlockUtils.BlockUtils.*;
import static com.jelly.MightyMiner.utils.AngleUtils.*;


public class PowderMacro extends Macro {

    private final Block[] stoneBlocks = {Blocks.stone, Blocks.coal_ore, Blocks.iron_ore, Blocks.gold_ore, Blocks.lapis_ore, Blocks.emerald_ore, Blocks.redstone_ore, Blocks.diamond_ore};
    private final Block[] obstacleBlocks = {Blocks.stained_glass, Blocks.stained_glass_pane, Blocks.wool, Blocks.prismarine, Blocks.chest};

    private final Block[] caveBlocks = {Blocks.bedrock, Blocks.flowing_water, Blocks.water, Blocks.flowing_lava, Blocks.lava,
            Blocks.dirt,  Blocks.stained_hardened_clay, Blocks.hardened_clay, Blocks.wooden_slab, Blocks.log, Blocks.log2, // goblin holdout
            Blocks.red_flower, Blocks.yellow_flower, Blocks.clay, Blocks.leaves, Blocks.leaves2, Blocks.grass, Blocks.tallgrass, // jungle
            Blocks.cobblestone, Blocks.stone_slab, Blocks.stone_slab2, Blocks.double_stone_slab2, Blocks.double_stone_slab, Blocks.wool, Blocks.stone_stairs, Blocks.stone_brick_stairs, Blocks.oak_fence, Blocks.cobblestone_wall, // precursor remnants
            Blocks.spruce_stairs, Blocks.acacia_stairs, Blocks.birch_stairs, Blocks.dark_oak_stairs, Blocks.jungle_stairs, Blocks.oak_stairs}; // mithril deposits

    private final Timer timer = new Timer();


    private final Rotation rotator = new Rotation();
    private final Queue<BlockPos> chestQueue = new LinkedList<>();
    private final CircularFifoQueue<BlockPos> solvedOrSolvingChests = new CircularFifoQueue<>(3);
    private BlockPos targetChest;
    private final Clock timeoutTimer = new Clock();
    private enum State {
        NORMAL, TREASURE_WALK, TREASURE_SOLVE
    }

    private State currentState;
    private boolean hasObstaclesToChest;
    private float rotationYawAxis;
    private int rightClickDelay;

    @Override
    protected void onEnable() {
        chestQueue.clear();
        solvedOrSolvingChests.clear();
        rotationYawAxis = getClosest();
        currentState = State.NORMAL;
        rightClickDelay = 6;
    }

    @Override
    protected void onDisable() {
        rotator.reset();
        KeybindHandler.resetKeybindState();
    }

    @Override
    public void onTick(TickEvent.Phase phase) {

        if(phase == TickEvent.Phase.END) return;

        if (rightClickDelay > 0) {
            rightClickDelay--;
        }

        updateState();

        switch(currentState) {
            case NORMAL:
                KeybindHandler.setKeyBindState(KeybindHandler.keybindW, countObstaclesInLane(4, stoneBlocks) < 2);
                KeybindHandler.setKeyBindState(KeybindHandler.keybindAttack, true);

                int closestLaneWithNoObstacles = getClosestLaneWithNoObstacles(6, obstacleBlocks);

                if (closestLaneWithNoObstacles == 0) {
                    alignWithBlock();
                } else if (closestLaneWithNoObstacles < 0) {
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindA, true);
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindD, false);
                } else {
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindA, false);
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindD, true);
                }

                if(countObstaclesInLane(10, caveBlocks) > 0) {
                    rotationYawAxis += closestObstaclesLeftOrRight(20, caveBlocks) == 1 ? 90 : -90;
                    rotationYawAxis = get360RotationYaw(rotationYawAxis);
                }

                break;

            case TREASURE_WALK:
                KeybindHandler.setKeyBindState(KeybindHandler.keybindW, true);
                KeybindHandler.setKeyBindState(KeybindHandler.keybindAttack, !fitsPlayer(getRelativeBlockPos(0, -1, 1)));
                float instantaneous_required_yaw = getRequiredYawCenter(targetChest);
                float target_pitch = hasObstaclesToChest ? 30 : getRequiredPitchCenter(targetChest);

                rotator.initAngleLock(instantaneous_required_yaw, target_pitch,
                        Math.max(getYawRotationTime(instantaneous_required_yaw, 45, 200, 500), getPitchRotationTime(target_pitch, 30, 200, 500)));
                break;

            case TREASURE_SOLVE:
                if(mc.objectMouseOver == null || mc.objectMouseOver.getBlockPos() == null)
                    return;

                BlockPos aimPos = mc.objectMouseOver.getBlockPos();
                double chestDistance = MathUtils.getDistanceBetweenTwoPoints(mc.thePlayer.posX, mc.thePlayer.posY + 1.62D, mc.thePlayer.posZ,
                        targetChest.getX() + 0.5D, targetChest.getY() + 0.5D, targetChest.getZ() + 0.5D);
                double aimDistance = MathUtils.getDistanceBetweenTwoPoints(mc.thePlayer.posX, mc.thePlayer.posY + 1.62D, mc.thePlayer.posZ,
                        aimPos.getX() + 0.5D, aimPos.getY() + 0.5D, aimPos.getZ() + 0.5D);
                KeybindHandler.setKeyBindState(KeybindHandler.keybindAttack, aimDistance < chestDistance && !getBlock(aimPos).equals(Blocks.chest));
                break;

        }
    }


    private void updateState() {

        switch (currentState) {
            case NORMAL:
                if(chestQueue.isEmpty())
                    return;

                KeybindHandler.resetKeybindState();

                currentState = State.TREASURE_WALK;
                targetChest = chestQueue.poll();
                timeoutTimer.schedule(10000);

                MovingObjectPosition trace = mc.theWorld.rayTraceBlocks(BlockUtils.getBlockCenter(getPlayerLoc()), BlockUtils.getBlockCenter(targetChest));

                hasObstaclesToChest = trace != null && !trace.getBlockPos().equals(targetChest);
                break;
            case TREASURE_WALK:
                if (mc.theWorld.getBlockState(targetChest).getBlock() != Blocks.chest || isOpen()) {
                    currentState = State.NORMAL;
                    return;
                }
                if(MathUtils.getDistanceBetweenTwoBlock(targetChest, getPlayerLoc()) >= 3.3f)
                    return;

                currentState = State.TREASURE_SOLVE;
                rotator.easeTo(getRequiredYawCenter(targetChest), getRequiredPitchCenter(targetChest),
                        Math.max(getYawRotationTime(getRequiredYawCenter(targetChest), 45, 150, 350),
                                getPitchRotationTime(getRequiredPitchCenter(targetChest), 30, 150, 350)));
                KeybindHandler.resetKeybindState();
                break;
            case TREASURE_SOLVE:
                if (mc.theWorld.getBlockState(targetChest).getBlock() != Blocks.chest || isOpen()) {
                    currentState = State.NORMAL;
                    return;
                }
                if (MightyMiner.config.powGreatExplorer) {
                    if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos().equals(targetChest)) {
                        if (rightClickDelay == 0) {
                            rightClickDelay = 8 + MathHelper.getRandomIntegerInRange(new Random(), 0, 5);
                            KeybindHandler.rightClick();
                        }
                        return;
                    }

                    float yaw = getRequiredYawCenter(targetChest);
                    float pitch = getRequiredPitchCenter(targetChest);

                    if (AngleUtils.getAngleDifference(yaw, mc.thePlayer.rotationYaw) > 0.5 ||
                            AngleUtils.getAngleDifference(pitch, mc.thePlayer.rotationPitch) > 0.5) {
                        if (rotator.rotating) {
                            return;
                        }

                        rotator.initAngleLock(yaw, pitch,
                                Math.max(getYawRotationTime(getRequiredYawCenter(targetChest), 45, 250, 500),
                                        getPitchRotationTime(getRequiredPitchCenter(targetChest), 30, 250, 500)));

                        return;
                    }

                    if (mc.objectMouseOver == null || !mc.objectMouseOver.getBlockPos().equals(targetChest)) {
                        KeybindHandler.leftClick();
                    }
                }
                break;
        }

        if(currentState == State.NORMAL || !timeoutTimer.passed())
            return;

        LogUtils.addMessage("Timeout due to unable to solve the chest");
        currentState = State.NORMAL;
        timeoutTimer.reset();
    }


    private void alignWithBlock() {
        double playerX = mc.thePlayer.posX;
        double playerZ = mc.thePlayer.posZ;
        double blockX = getPlayerLoc().getX() + 0.5;
        double blockZ = getPlayerLoc().getZ() + 0.5;

        switch (mc.thePlayer.getHorizontalFacing()) {
            case NORTH:
                KeybindHandler.setKeyBindState(KeybindHandler.keybindA, playerX > blockX - 0.2);
                KeybindHandler.setKeyBindState(KeybindHandler.keybindD, playerX < blockX + 0.2);
                break;
            case SOUTH:
                KeybindHandler.setKeyBindState(KeybindHandler.keybindA, playerX < blockX + 0.2);
                KeybindHandler.setKeyBindState(KeybindHandler.keybindD, playerX > blockX - 0.2);
                break;
            case EAST:
                KeybindHandler.setKeyBindState(KeybindHandler.keybindA, playerZ > blockZ - 0.2);
                KeybindHandler.setKeyBindState(KeybindHandler.keybindD, playerZ < blockZ + 0.2);
                break;
            case WEST:
                KeybindHandler.setKeyBindState(KeybindHandler.keybindA, playerZ < blockZ + 0.2);
                KeybindHandler.setKeyBindState(KeybindHandler.keybindD, playerZ > blockZ - 0.2);
                break;
        }
    }


    @Override
    public void onLastRender(RenderWorldLastEvent event) {
        if(rotator.rotating)
            rotator.update();
        else if(currentState == State.NORMAL) {
            if(MightyMiner.config.powMiningShape == 0)
                rotator.updateInLimacon(1.5f, 3, rotationYawAxis, 8);
            else
                rotator.updateInEllipse(0.5f, 1f, Arrays.asList(stoneBlocks).contains(getFrontBlock()) ? 1 : 3, rotationYawAxis, 8);
        }
    }

    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event) {
        if(event.type == RenderGameOverlayEvent.ElementType.TEXT){
            mc.fontRendererObj.drawString(currentState + " " + currentState, 5 , 5, -1);
            mc.fontRendererObj.drawString("Chests in waiting queue: " + chestQueue.size() + " | Chests in finished queue: " + solvedOrSolvingChests.size() + "/3", 5 , 17, -1);
        }
    }

    @Override
    public void onMessageReceived(String message){
        if(message.contains("You uncovered a treasure chest!")){
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    addChestToQueue();
                }
            }, 200);
        } else if(message.contains("You have successfully picked the lock on this chest")) {
            currentState = State.NORMAL;
        } else if(message.contains("You cannot mine this close to an entrance")) {
            if(currentState != State.NORMAL)
                return;

            System.out.println(railwayOnLeftOrRight());

            // TODO: SCAN RAILWAYS
        } else if (message.contains("You must pick the lock on this chest to open it!")) {
            Logger.log("You don't have great explorer so no need for it to be enabled");
            MightyMiner.config.powGreatExplorer = false;
        }

    }

    @Override
    public void onPacketReceived(Packet<?> packet){
        if(currentState != State.TREASURE_SOLVE || !(packet instanceof S2APacketParticles) || targetChest == null || MightyMiner.config.powGreatExplorer)
            return;

        if(!(((S2APacketParticles) packet).getParticleType() == EnumParticleTypes.CRIT))
            return;

        if(Math.abs((((S2APacketParticles) packet).getXCoordinate()) - (targetChest.getX() + 0.5f)) >= 0.7f ||
                Math.abs((((S2APacketParticles) packet).getYCoordinate()) - (targetChest.getY() + 0.5f)) >= 0.7f ||
                Math.abs((((S2APacketParticles) packet).getZCoordinate()) - (targetChest.getZ() + 0.5f)) >= 0.7f)
            return;

        if(rotator.rotating)
            rotator.reset();

        rotator.initAngleLock(
                getRequiredYaw(((S2APacketParticles) packet).getXCoordinate() - mc.thePlayer.posX, ((S2APacketParticles) packet).getZCoordinate() - mc.thePlayer.posZ),
                getRequiredPitch(((S2APacketParticles) packet).getXCoordinate() - mc.thePlayer.posX, (((S2APacketParticles) packet).getYCoordinate()) - (mc.thePlayer.posY + 1.62d), ((S2APacketParticles) packet).getZCoordinate() - mc.thePlayer.posZ),
                (int) (300 + Math.random() * 200));
    }

    public boolean isOpen() {
        TileEntityChest chest = (TileEntityChest) mc.theWorld.getTileEntity(targetChest);
        if (chest == null) return false;
        int state = chest.numPlayersUsing;
        return state > 0;
    }


    private void addChestToQueue() {
        LogUtils.debugLog("Adding chest to queue");
        List<BlockPos> foundBlocks = BlockUtils.findBlock(new Box(-7, 7, 3, -1, -7, 7),
                new ArrayList<>(solvedOrSolvingChests), 0, 256, new BlockData<>(Blocks.chest));

        if(foundBlocks.isEmpty()){
            LogUtils.addMessage("That chest was impossible to solve");
            return;
        }

        BlockPos chest = foundBlocks.get(0);
        solvedOrSolvingChests.add(chest);
        chestQueue.add(chest);
    }



    private int countObstaclesInLane(int range, Block... obstaclesToCheck) {
        int obstacles = 0;
        for(int i = 0; i <= range; i++){
            BlockPos checkPos = getRelativeBlockPos(0, 0, i, rotationYawAxis);
            if(Arrays.asList(obstaclesToCheck).contains(getBlock(checkPos)))
                ++obstacles;
            if(Arrays.asList(obstaclesToCheck).contains(getBlock(checkPos.up())))
                ++obstacles;

        }
        return obstacles;
    }

    // Returns -1 if the obstacle is on the left, 1 if the closest obstacle is on the right
    private int closestObstaclesLeftOrRight(int halfScanRange, Block... obstaclesToCheck) {
        for(int i = 0; i <= halfScanRange; i++){
            if(Arrays.asList(obstaclesToCheck).contains(getBlock(getRelativeBlockPos(i, 0, 0, rotationYawAxis))))
                return 1;
            else if(Arrays.asList(obstaclesToCheck).contains(getBlock(getRelativeBlockPos(-i, 0, 0, rotationYawAxis))))
                return -1;
        }
        return 1;
    }

    // Returns -1 if the railway is on the left, otherwise 1
    private int railwayOnLeftOrRight() {
        for(int i = 0; i <= 20; i++) {
            for(int y = -20; y <= 20; y++) {
                for(int j = 0; j <= 20; j++) {
                    if(getRelativeBlock(i, y, j).equals(Blocks.rail))
                        return 1;
                }
            }
        }
        return -1;

    }


    private int getClosestLaneWithNoObstacles(int range, Block... obstaclesToCheck) {
        boolean leftHaveObstacles = false, rightHaveObstacles = false;
        for(int i = 0; i <= 5; i++){
            for(int j = 0; j <= range; j++){
                BlockPos checkPosRight = getRelativeBlockPos(i, 0, j, rotationYawAxis);
                BlockPos checkPosLeft = getRelativeBlockPos(-i, 0, j, rotationYawAxis);
                if(Arrays.asList(obstaclesToCheck).contains(getBlock(checkPosRight)) || Arrays.asList(obstaclesToCheck).contains(getBlock(checkPosRight.up()))) {
                    rightHaveObstacles = true;
                }
                if(Arrays.asList(obstaclesToCheck).contains(getBlock(checkPosLeft)) || Arrays.asList(obstaclesToCheck).contains(getBlock(checkPosLeft.up()))) {
                    leftHaveObstacles = true;
                }
            }

            if(!leftHaveObstacles)
                return -i;
            else if(!rightHaveObstacles)
                return i;

            leftHaveObstacles = false;
            rightHaveObstacles = false;
        }
        LogUtils.debugLog("No lane with no obstacles found!");
        return 0;
    }

}
