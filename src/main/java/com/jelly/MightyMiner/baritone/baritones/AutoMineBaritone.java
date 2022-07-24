package com.jelly.MightyMiner.baritone.baritones;

import com.jelly.MightyMiner.baritone.Baritone;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.structures.GridEnvironment;
import com.jelly.MightyMiner.structures.Node;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.MathUtils;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

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
    BlockPos lastBlockPos;



    public void clearBlocksToWalk(){
        openNodes.clear();
        checkedNodes.clear();
        blocksToMine.clear();
        gridEnvironment.clear();
        BlockRenderer.renderMap.clear();
        step = 0;
    }


    @Override
    protected void onEnable(BlockPos destinationBlock) {
        clearBlocksToWalk();
        new Thread(() -> {

            mc.thePlayer.addChatMessage(new ChatComponentText("Starting automine"));
            BlockRenderer.renderMap.put(destinationBlock, Color.RED);
            try {
                blocksToMine = calculateBlocksToMine(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), destinationBlock);
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
    public void onTickEvent(){

        if(walking) {

            KeybindHandler.setKeyBindState(KeybindHandler.keybindAttack, true);
            KeybindHandler.setKeyBindState(KeybindHandler.keybindW, true);

            if (BlockUtils.isPassable(blocksToMine.getLast())) {
                lastBlockPos = blocksToMine.getLast();
                BlockRenderer.renderMap.remove(blocksToMine.getLast());
                blocksToMine.removeLast();
            }

            if (blocksToMine.isEmpty()) {
                mc.thePlayer.addChatMessage(new ChatComponentText("Finished baritone"));
                walking = false;
                KeybindHandler.resetKeybindState();
                disableBaritone();
                return;
            }


            if(!BlockUtils.isPassable(blocksToMine.getLast()))
                 rotation.intLockAngle(AngleUtils.getRequiredYaw(blocksToMine.getLast()), AngleUtils.getRequiredPitch(blocksToMine.getLast()), 200);

            if (lastBlockPos != null
                    && lastBlockPos.getY() == (int) mc.thePlayer.posY + 1
                    && (lastBlockPos.getX() != Math.floor(mc.thePlayer.posX) || lastBlockPos.getZ() != Math.floor(mc.thePlayer.posZ))
                    && (BlockUtils.fitsPlayer(lastBlockPos.down()) || BlockUtils.fitsPlayer(lastBlockPos.down(2)))
            ) {
                deltaJumpTick = 3;
            }



            if (deltaJumpTick > 0) {
                deltaJumpTick--;
                KeybindHandler.setKeyBindState(KeybindHandler.keyBindJump, true);
            } else KeybindHandler.setKeyBindState(KeybindHandler.keyBindJump, false);


            System.out.println( !AngleUtils.onTheSameXZ(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), blocksToMine.get(blocksToMine.size() - 1)));


        }

    }
    @Override
    public void onRenderEvent(){
        if(rotation.rotating)
            rotation.update();
    }

    private LinkedList<BlockPos> calculateBlocksToMine(BlockPos startingPos, BlockPos endingBlock) throws Exception {

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
        if (!searchNode.checked
                && !searchNode.opened
                && !BlockUtils.isPassable(searchNode.blockPos) && !BlockUtils.isPassable(searchNode.blockPos.up())
        && BlockUtils.canWalkOn(searchNode.blockPos.down()) ){
            searchNode.opened = true;
            searchNode.lastNode = currentNode;
            openNodes.add(searchNode);
            calculateCost(searchNode, endingBlockPos);
            //BlockRenderer.renderMap.put(searchNode.blockPos, Color.GREEN);
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
        LinkedList<BlockPos> blocksToWalk = new LinkedList<>();
        blocksToWalk.add(goalNode.blockPos);
        while(!startNode.equals(goalNode) && goalNode.lastNode != null){
            blocksToWalk.add(goalNode.lastNode.blockPos);
            blocksToWalk.add(goalNode.lastNode.blockPos.up());
            if(goalNode.lastNode.blockPos.getY() != goalNode.blockPos.getY()){
                blocksToWalk.add(goalNode.lastNode.blockPos.up(2));
            }
            goalNode = goalNode.lastNode;
        }
        mc.thePlayer.addChatMessage(new ChatComponentText("Block count : " + blocksToWalk.size()));
        return blocksToWalk;
    }

}
