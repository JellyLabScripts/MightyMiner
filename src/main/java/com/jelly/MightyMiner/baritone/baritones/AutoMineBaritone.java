package com.jelly.MightyMiner.baritone.baritones;

import com.jelly.MightyMiner.baritone.Baritone;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.structures.GridEnvironment;
import com.jelly.MightyMiner.structures.Node;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AutoMineBaritone extends Baritone{

    GridEnvironment<Node> gridEnvironment = new GridEnvironment<>();

    int maxX = 10000;
    int maxY = 256;
    int maxZ = 10000;

    List<Node> checkedNodes = new ArrayList<>();
    List<Node> openNodes = new ArrayList<>();


    LinkedList<BlockPos> blocksToMine = new LinkedList<>();
    int step = 0;

    boolean walking = false;
    Rotation rotation = new Rotation();

    int deltaJumpTick = 0;
    BlockPos lastMinedBlockPos;

    List<Block> forbiddenMiningBlocks;
    List<Block> allowedMiningBlocks;

    public AutoMineBaritone(){}

    public AutoMineBaritone(List<Block> forbiddenMiningBlocks){
        this.forbiddenMiningBlocks = forbiddenMiningBlocks;
    }

    public AutoMineBaritone(List<Block> forbiddenMiningBlocks, List<Block> allowedMiningBlocks){
        this.forbiddenMiningBlocks = forbiddenMiningBlocks;
        this.allowedMiningBlocks = allowedMiningBlocks;
    }



    public void clearBlocksToWalk(){
        openNodes.clear();
        checkedNodes.clear();
        blocksToMine.clear();
        gridEnvironment.clear();
        BlockRenderer.renderMap.clear();
        lastMinedBlockPos = null;
        step = 0;
    }


    @Override
    protected void onEnable(BlockPos destinationBlock) {
        mc.gameSettings.gammaSetting = 100;
        clearBlocksToWalk();
        KeybindHandler.resetKeybindState();
        new Thread(() -> {

            mc.thePlayer.addChatMessage(new ChatComponentText("Starting automine"));

            BlockRenderer.renderMap.put(destinationBlock, Color.RED);
            try {
                blocksToMine = calculateBlocksToMine(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), destinationBlock);
                Thread.sleep(50);
            }catch (Exception e){
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Can't find path" ));
                e.printStackTrace();
                return;
            }
            for(BlockPos blockPos : blocksToMine){
                BlockRenderer.renderMap.put(blockPos, Color.ORANGE);
            }
            walking = true;
        }).start();
    }

    @Override
    public void onDisable() {
        walking = false;
        KeybindHandler.resetKeybindState();
        clearBlocksToWalk();
    }


    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event){
        if(event.type == RenderGameOverlayEvent.ElementType.TEXT){
            if(blocksToMine != null){
                if(!blocksToMine.isEmpty()){
                    for(int i = 0; i < blocksToMine.size(); i++){
                        mc.fontRendererObj.drawString(blocksToMine.get(i).toString(), 5, 5 + 10 * i, -1);
                    }
                }
            }
        }
    }



    boolean walkFlag = false;
    @Override
    public void onTickEvent(TickEvent.Phase phase){

        if(phase == TickEvent.Phase.START){
            if(mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && !blocksToMine.isEmpty())
                KeybindHandler.setKeyBindState(KeybindHandler.keybindAttack, mc.objectMouseOver.getBlockPos().equals(blocksToMine.getLast()) && !walkFlag);
        }
        if(phase == TickEvent.Phase.END) {
            if (walking) {

                // System.out.println(!walkFlag && BlockUtils.isPassable(blocksToMine.getLast()));
                if (!blocksToMine.isEmpty()) {
                    if ((!walkFlag && BlockUtils.isPassable(blocksToMine.getLast())) // if it is blocks to mine, check whether mined
                            || (walkFlag &&   // if it is blocks to walk, check -> possible to stand there || reached the target block
                            (!BlockUtils.fitsPlayer(blocksToMine.getLast()) || !BlockUtils.getPlayerLoc().equals(blocksToMine.getLast())))) {
                        lastMinedBlockPos = blocksToMine.getLast();
                        BlockRenderer.renderMap.remove(blocksToMine.getLast());
                        blocksToMine.removeLast();

                        walkFlag = !blocksToMine.isEmpty() && BlockUtils.isPassable(blocksToMine.getLast());
                    }
                }


                if (blocksToMine.isEmpty()) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("Finished baritone"));
                    walking = false;
                    KeybindHandler.resetKeybindState();
                    disableBaritone();
                    return;
                }

                BlockPos targetMineBlock = blocksToMine.getLast();

                if (!BlockUtils.isPassable(targetMineBlock))
                    rotation.intLockAngle(AngleUtils.getRequiredYaw(targetMineBlock), AngleUtils.getRequiredPitch(targetMineBlock), 500);

                if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && !blocksToMine.isEmpty()) {
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindAttack, mc.objectMouseOver.getBlockPos().equals(targetMineBlock) && !walkFlag);
                }

                if (blocksToMine.size() == 1) {
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindW, false);
                    KeybindHandler.setKeyBindState(KeybindHandler.keyBindJump, false);
                    return;
                }

                if (lastMinedBlockPos != null) {
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindW,
                            !blocksToMine.getLast().equals(BlockUtils.getRelativeBlockPos(0, 2, 0)) && !blocksToMine.getLast().equals(BlockUtils.getRelativeBlockPos(0, -1, 0)));
                }

                if (lastMinedBlockPos != null
                        && (lastMinedBlockPos.getY() == (int) mc.thePlayer.posY + 2 || lastMinedBlockPos.getY() == (int) mc.thePlayer.posY + 1)
                        && (lastMinedBlockPos.getX() != Math.floor(mc.thePlayer.posX) || lastMinedBlockPos.getZ() != Math.floor(mc.thePlayer.posZ))
                        && (BlockUtils.fitsPlayer(lastMinedBlockPos.down()) || BlockUtils.fitsPlayer(lastMinedBlockPos.down(2)))
                ) {
                    deltaJumpTick = 3;
                }

                if (deltaJumpTick > 0) {
                    deltaJumpTick--;
                    KeybindHandler.setKeyBindState(KeybindHandler.keyBindJump, true);
                } else KeybindHandler.setKeyBindState(KeybindHandler.keyBindJump, false);


            }
        }


    }
    @Override
    public void onRenderEvent(){
        if(rotation.rotating)
            rotation.update();

    }

    private LinkedList<BlockPos> calculateBlocksToMine(BlockPos startingPos, BlockPos endingBlock) throws Exception {

        if(startingPos.add(0, -1, 0).equals(endingBlock)){
            return new LinkedList<BlockPos>(){{add(startingPos.add(0, -1, 0));}};
        }
        if(BlockUtils.canSeeBlock(endingBlock) && BlockUtils.canReachBlock(endingBlock)){
            return new LinkedList<BlockPos>(){{add(endingBlock);}};
        }

        boolean completedPathfind = false;
        Node startNode;
        Node currentNode;
        Node goalNode = new Node(endingBlock);

        int currentGridX = maxX / 2;
        int currentGridY = (int) mc.thePlayer.posY;
        int currentGridZ = maxZ / 2;

        instantiateAnyNode(currentGridX, currentGridY, currentGridZ, new Node(startingPos));
        currentNode = gridEnvironment.get(currentGridX, currentGridY, currentGridZ);
        startNode = currentNode;


        while (!completedPathfind) {
            step++;
            currentNode.checked = true;
            checkedNodes.add(currentNode);
            openNodes.remove(currentNode);

           /* if(BlockUtils.canSeeBlock(currentNode.blockPos, endingBlock)){
                LinkedList<BlockPos> blockPoses = trackBackPath(currentNode, startNode);
                blockPoses.add(endingBlock);
                return blockPoses;
            }*/

            if (currentGridX > 0) {
                instantiateNode(currentGridX - 1, currentGridY, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX - 1, currentGridY, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridX < maxX) {
                instantiateNode(currentGridX + 1, currentGridY, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX + 1, currentGridY, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridZ > 0) {
                instantiateNode(currentGridX, currentGridY, currentGridZ - 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY, currentGridZ - 1), currentNode, endingBlock);
            }
            if (currentGridZ < maxZ) {
                instantiateNode(currentGridX, currentGridY, currentGridZ + 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX,  currentGridY, currentGridZ + 1), currentNode, endingBlock);
            }

            if (currentGridY > 0 && currentGridX > 0) {
                instantiateNode(currentGridX - 1, currentGridY - 1, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX - 1, currentGridY - 1, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridY > 0 && currentGridX < maxX) {
                instantiateNode(currentGridX + 1, currentGridY - 1, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX + 1, currentGridY - 1, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridY > 0 && currentGridZ > 0) {
                instantiateNode(currentGridX, currentGridY - 1, currentGridZ - 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY - 1, currentGridZ - 1), currentNode, endingBlock);
            }
            if (currentGridY > 0 && currentGridZ < maxZ) {
                instantiateNode(currentGridX, currentGridY - 1, currentGridZ + 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY - 1, currentGridZ + 1), currentNode, endingBlock);
            }

            if (currentGridY < maxY && currentGridX > 0) {
                instantiateNode(currentGridX - 1, currentGridY + 1, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX - 1, currentGridY + 1, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridY < maxY && currentGridX < maxX) {
                instantiateNode(currentGridX + 1, currentGridY + 1, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX + 1, currentGridY + 1, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridY < maxY && currentGridZ > 0) {
                instantiateNode(currentGridX, currentGridY + 1, currentGridZ - 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY + 1, currentGridZ - 1), currentNode, endingBlock);
            }
            if (currentGridY < maxY && currentGridZ < maxZ) {
                instantiateNode(currentGridX, currentGridY + 1, currentGridZ + 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY + 1, currentGridZ + 1), currentNode, endingBlock);
            }



            int bestIndex = 0;
            double minFcost = 9999;

            for (int i = 0; i < openNodes.size(); i++) {
                if (openNodes.get(i).fValue < minFcost) {
                    bestIndex = i;
                    minFcost = openNodes.get(i).fValue;
                }
            }

            int tempX, tempY, tempZ;
            tempX = currentGridX;
            tempY = currentGridY;
            tempZ = currentGridZ;
            currentGridX += openNodes.get(bestIndex).blockPos.getX() - gridEnvironment.get(tempX, tempY, tempZ).blockPos.getX();
            currentGridY += openNodes.get(bestIndex).blockPos.getY() - gridEnvironment.get(tempX, tempY, tempZ).blockPos.getY();
            currentGridZ += openNodes.get(bestIndex).blockPos.getZ() - gridEnvironment.get(tempX, tempY, tempZ).blockPos.getZ();

            currentNode = openNodes.get(bestIndex);
            if (goalNode.blockPos.equals(currentNode.blockPos)) {
                completedPathfind = true;
            }
        }
        return trackBackPath(currentNode, startNode);



    }
    private void openNodeAndCalculateCost(Node searchNode, Node currentNode, BlockPos endingBlockPos){
        if ( (!searchNode.checked
                && !searchNode.opened
               // && !BlockUtils.isPassable(searchNode.blockPos)
              //  && !BlockUtils.isPassable(searchNode.blockPos.up())
                && BlockUtils.canWalkOn(searchNode.blockPos.down()))){

            if(currentNode.lastNode != null){
                if(Math.abs(currentNode.lastNode.blockPos.getY() - searchNode.blockPos.getY()) > 1 &&
                        BlockUtils.onTheSameXZ(currentNode.lastNode.blockPos, searchNode.blockPos))
                    return;
            }
            if(!searchNode.blockPos.equals(endingBlockPos)) {
                if (forbiddenMiningBlocks != null && forbiddenMiningBlocks.contains(BlockUtils.getBlock(searchNode.blockPos))  && !BlockUtils.getBlock(searchNode.blockPos).equals(Blocks.air))
                    return;

                if (allowedMiningBlocks != null && !allowedMiningBlocks.contains(BlockUtils.getBlock(searchNode.blockPos)) && !BlockUtils.getBlock(searchNode.blockPos).equals(Blocks.air))
                    return;
            }

            searchNode.opened = true;
            searchNode.lastNode = currentNode;
            openNodes.add(searchNode);
            calculateCost(searchNode, endingBlockPos);
        }

    }

    private void instantiateNode(int gridX, int gridY, int gridZ, Node startNode){
        instantiateAnyNode(gridX, gridY, gridZ, new Node(startNode.blockPos.add(gridX - maxX/2, gridY - startNode.blockPos.getY(), gridZ -  maxZ/2)));
    }
    private void instantiateAnyNode(int gridX, int gridY, int gridZ, Node node){
        if(gridEnvironment.get(gridX, gridY, gridZ) == null)
            gridEnvironment.set(gridX, gridY, gridZ, node);
    }

    private void calculateCost(Node node, BlockPos endingBlock){
        node.hValue = MathUtils.getHeuristicCostBetweenTwoBlock(node.blockPos, endingBlock);

        if(node.lastNode != null) {
            if(node.lastNode.blockPos.getY() != node.blockPos.getY()) {
                node.gValue = node.lastNode.gValue + 2;
            } else {
                node.gValue = node.lastNode.gValue + 1;
            }
        }
        else
            node.gValue = 1f;
        node.fValue = node.gValue + node.hValue;
    }

    private LinkedList<BlockPos> trackBackPath(Node goalNode, Node startNode){
        LinkedList<BlockPos> blocksToMine = new LinkedList<>();

        if(goalNode.blockPos != null && goalNode.lastNode != null && goalNode.lastNode.blockPos != null) {
            if (goalNode.lastNode.blockPos.getY() > goalNode.blockPos.getY()) {
                blocksToMine.add(goalNode.blockPos);
                if (!BlockUtils.isPassable(goalNode.blockPos.up())) {
                    blocksToMine.add(goalNode.blockPos.up());
                }
            } else {
                blocksToMine.add(goalNode.blockPos);
            }
        }

        if (goalNode.lastNode != null) {
            while(!startNode.equals(goalNode) && goalNode.lastNode.blockPos != null) {
                if(goalNode.lastNode.blockPos.getY() < goalNode.blockPos.getY()) {

                    if(!BlockUtils.isPassable(goalNode.lastNode.blockPos.up(2))) {
                        blocksToMine.add(goalNode.lastNode.blockPos.up(2));
                    }
                    if(!BlockUtils.isPassable(goalNode.lastNode.blockPos.up())) {
                        blocksToMine.add(goalNode.lastNode.blockPos.up());
                    }

                    blocksToMine.add(goalNode.lastNode.blockPos);
                } else if (goalNode.lastNode.blockPos.getY() > goalNode.blockPos.getY()){

                    blocksToMine.add(goalNode.lastNode.blockPos);

                    if(!BlockUtils.isPassable(goalNode.lastNode.blockPos.up())) {
                        blocksToMine.add(goalNode.lastNode.blockPos.up());
                    }
                    if(!BlockUtils.isPassable(goalNode.lastNode.blockPos.up(2))) {
                        blocksToMine.add(goalNode.lastNode.blockPos.up(2));
                    }

                } else {
                    blocksToMine.add(goalNode.lastNode.blockPos);

                    if(!BlockUtils.isPassable(goalNode.lastNode.blockPos))
                        blocksToMine.add(goalNode.lastNode.blockPos.up());
                }
                goalNode = goalNode.lastNode;
            }
        }

        //remove player pos
        blocksToMine.removeLast();
        mc.thePlayer.addChatMessage(new ChatComponentText(blocksToMine.getLast().toString()));
        //add back player extra block

        if(blocksToMine.getLast().getY() == (int)mc.thePlayer.posY){
            blocksToMine.add(blocksToMine.getLast().up());
        }
     /*   if(blocksToMine.getLast().getY() < (int)mc.thePlayer.posY - 1){
            blocksToMine.add(blocksToMine.getLast().down());
        }*/
        mc.thePlayer.addChatMessage(new ChatComponentText("Block count : " + blocksToMine.size()));
        return blocksToMine;
    }

}
