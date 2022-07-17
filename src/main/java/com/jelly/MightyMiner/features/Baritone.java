package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.structures.GridEnvironment;
import com.jelly.MightyMiner.structures.Node;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Baritone {
    //Custom grid example

    //Xxxxxxxxxxxxxx |
    //xxxxxxxxxxxxxx |
    //xxxxxxKxxxxxxx | MaxZ
    //xxxxxxxxxxxxxx |
    //xxxxxxxxxxxxxx |
    //<------------->
    //     MaxX
    // Y is the same as world coordinate

    // program coverts world coordinates to custom grid first
    // custom grid starts takes X as [0, y, 0]
    // player starts on K initially [maxX/2, y, maxZ/2]
    GridEnvironment<Node> gridEnvironment = new GridEnvironment<>();

    Minecraft mc = Minecraft.getMinecraft();

    int maxX = 10000;
    int maxY = 256;
    int maxZ = 10000;

    List<Node> checkedNodes = new ArrayList<>();
    List<Node> openNodes = new ArrayList<>();
    List<BlockPos> blocksToWalk = new ArrayList<>();

    boolean completed = false;
    public int step = 0;

    public void clearBlocksToWalk(){
        openNodes.clear();
        checkedNodes.clear();
        blocksToWalk.clear();
        gridEnvironment.clear();
        BlockRenderer.renderMap.clear();
        step = 0;
        completed = false;
    }

    public void walkTo(BlockPos endingBlock){

        new Thread(() -> {
            clearBlocksToWalk();
            BlockRenderer.renderMap.put(endingBlock, Color.RED);
            try {
                blocksToWalk = calculatePath(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), endingBlock);
            }catch (Exception e){
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Can't find path" ));
                e.printStackTrace();
            }
            for(BlockPos blockPos : blocksToWalk){
                BlockRenderer.renderMap.put(blockPos, Color.ORANGE);
            }
        }).start();

    }

    private List<BlockPos> calculatePath(BlockPos startingPos, BlockPos endingBlock) throws Exception {


        Node startNode;
        Node currentNode;
        Node goalNode = new Node(endingBlock);

        int currentGridX = maxX / 2;
        int currentGridY = (int) mc.thePlayer.posY;
        int currentGridZ = maxZ / 2;

        instantiateAnyNode(currentGridX, currentGridY, currentGridZ, new Node(startingPos));
        currentNode = gridEnvironment.get(currentGridX, currentGridY, currentGridZ);
        startNode = currentNode;

        while (!completed) {
            Thread.sleep(10);
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
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY, currentGridZ + 1), currentNode, endingBlock);
            }
            if (currentGridY > 0) {
                instantiateNode(currentGridX, currentGridY - 1, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY - 1, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridY < maxY) {
                instantiateNode(currentGridX, currentGridY + 1, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY + 1, currentGridZ), currentNode, endingBlock);
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
                completed = true;
            }
        }
        return trackBackPath(currentNode, startNode);



    }
    private void openNodeAndCalculateCost(Node searchNode, Node currentNode, BlockPos endingBlockPos){
        if (!searchNode.checked && !searchNode.opened && BlockUtils.isWalkable(mc.theWorld.getBlockState(searchNode.blockPos).getBlock())
                && (BlockUtils.canWalkOn(searchNode.blockPos.down()) //walkable by itself
                || (!BlockUtils.isWalkable(mc.theWorld.getBlockState(searchNode.blockPos.down(2)).getBlock())
                    && (BlockUtils.canWalkOn(searchNode.blockPos.add(-1, -1, 0)) || BlockUtils.canWalkOn(searchNode.blockPos.add(1, -1, 0))
                        || BlockUtils.canWalkOn(searchNode.blockPos.add(0, -1, 1)) || BlockUtils.canWalkOn(searchNode.blockPos.add(0, -1, -1)))))){
            searchNode.opened = true;
            searchNode.lastNode = currentNode;
            openNodes.add(searchNode);
            calculateCost(searchNode, endingBlockPos);
            BlockRenderer.renderMap.put(searchNode.blockPos, Color.GREEN);

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

        if(node.lastNode != null)
            node.gValue = node.lastNode.gValue + 1f;
        else
            node.gValue = 1f;
        node.fValue = node.gValue + node.hValue;
    }

    private List<BlockPos> trackBackPath(Node goalNode, Node startNode){
        List<BlockPos> blocksToWalk = new ArrayList<>();
        while(!startNode.equals(goalNode) && goalNode.lastNode != null){
            blocksToWalk.add(goalNode.lastNode.blockPos);
            goalNode = goalNode.lastNode;
        }
        mc.thePlayer.addChatMessage(new ChatComponentText("Block count : " + blocksToWalk.size()));
        return blocksToWalk;
    }

}
