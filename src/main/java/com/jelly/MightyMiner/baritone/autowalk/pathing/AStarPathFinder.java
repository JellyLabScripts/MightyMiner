package com.jelly.MightyMiner.baritone.autowalk.pathing;

import com.jelly.MightyMiner.baritone.autowalk.pathing.config.PathBehaviour;
import com.jelly.MightyMiner.baritone.logging.Logger;
import com.jelly.MightyMiner.baritone.structures.GridEnvironment;
import com.jelly.MightyMiner.baritone.structures.Node;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.awt.*;
import java.util.*;
import java.util.List;

public class AStarPathFinder {
    Minecraft mc = Minecraft.getMinecraft();
    GridEnvironment<Node> gridEnvironment = new GridEnvironment<>();

    int step;
    PathBehaviour pathBehaviour;

    List<Node> checkedNodes = new ArrayList<>();
    PriorityQueue<Node> openNodes = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fValue));


    public AStarPathFinder(PathBehaviour options){
        this.pathBehaviour = options;
    }

    public LinkedList<BlockPos> getPath(BlockPos targetBlockPos) {
        return calculatePath(BlockUtils.getPlayerLoc(), targetBlockPos);
    }


    private LinkedList<BlockPos> calculatePath(BlockPos startingPos, BlockPos endingBlock) {

        gridEnvironment.clear();
        checkedNodes.clear();
        openNodes.clear();

        Node startNode;
        Node currentNode;

        int currentGridX = 0;
        int currentGridY = (int) mc.thePlayer.posY;
        int currentGridZ = 0;

        instantiateAnyNode(currentGridX, currentGridY, currentGridZ, new Node(startingPos));
        startNode = gridEnvironment.get(currentGridX, currentGridY, currentGridZ);
        step = 0;

        openNodes.add(startNode);
        while (!openNodes.isEmpty()) {
            currentNode = openNodes.poll();
            if(currentNode.lastNode != null) {
                currentGridX = currentNode.blockPos.getX() - startNode.blockPos.getX();
                currentGridY = currentNode.blockPos.getY();
                currentGridZ = currentNode.blockPos.getZ() - startNode.blockPos.getZ();
            }

            checkedNodes.add(currentNode);
            step++;

            if(step > 300) break;

            instantiateNode(currentGridX - 1, currentGridY, currentGridZ, startNode);
            checkNode(gridEnvironment.get(currentGridX - 1, currentGridY, currentGridZ), currentNode, endingBlock);

            instantiateNode(currentGridX + 1, currentGridY, currentGridZ, startNode);
            checkNode(gridEnvironment.get(currentGridX + 1, currentGridY, currentGridZ), currentNode, endingBlock);

            instantiateNode(currentGridX, currentGridY, currentGridZ - 1, startNode);
            checkNode(gridEnvironment.get(currentGridX, currentGridY, currentGridZ - 1), currentNode, endingBlock);

            instantiateNode(currentGridX, currentGridY, currentGridZ + 1, startNode);
            checkNode(gridEnvironment.get(currentGridX,  currentGridY, currentGridZ + 1), currentNode, endingBlock);

            instantiateNode(currentGridX - 1, currentGridY - 1, currentGridZ, startNode);
            checkNode(gridEnvironment.get(currentGridX - 1, currentGridY - 1, currentGridZ), currentNode, endingBlock);

            instantiateNode(currentGridX + 1, currentGridY - 1, currentGridZ, startNode);
            checkNode(gridEnvironment.get(currentGridX + 1, currentGridY - 1, currentGridZ), currentNode, endingBlock);

            instantiateNode(currentGridX, currentGridY - 1, currentGridZ - 1, startNode);
            checkNode(gridEnvironment.get(currentGridX, currentGridY - 1, currentGridZ - 1), currentNode, endingBlock);

            instantiateNode(currentGridX, currentGridY - 1, currentGridZ + 1, startNode);
            checkNode(gridEnvironment.get(currentGridX, currentGridY - 1, currentGridZ + 1), currentNode, endingBlock);

            instantiateNode(currentGridX, currentGridY - 1, currentGridZ, startNode);
            checkNode(gridEnvironment.get(currentGridX, currentGridY - 1, currentGridZ), currentNode, endingBlock);

            instantiateNode(currentGridX - 1, currentGridY + 1, currentGridZ, startNode);
            checkNode(gridEnvironment.get(currentGridX - 1, currentGridY + 1, currentGridZ), currentNode, endingBlock);

            instantiateNode(currentGridX + 1, currentGridY + 1, currentGridZ, startNode);
            checkNode(gridEnvironment.get(currentGridX + 1, currentGridY + 1, currentGridZ), currentNode, endingBlock);

            instantiateNode(currentGridX, currentGridY + 1, currentGridZ - 1, startNode);
            checkNode(gridEnvironment.get(currentGridX, currentGridY + 1, currentGridZ - 1), currentNode, endingBlock);

            instantiateNode(currentGridX, currentGridY + 1, currentGridZ + 1, startNode);
            checkNode(gridEnvironment.get(currentGridX, currentGridY + 1, currentGridZ + 1), currentNode, endingBlock);


            if(currentNode.blockPos.equals(endingBlock)) {
                return trackBackPath(currentNode, startNode);
            }


        }
        return new LinkedList<>();
    }
    private void checkNode(Node searchNode, Node currentNode, BlockPos endingBlockPos){

        if (!checkedNodes.contains(searchNode) && BlockUtils.fitsPlayer(searchNode.blockPos.down())){
            if(!openNodes.contains(searchNode)){
                calculateCost(searchNode, endingBlockPos);
                searchNode.lastNode = currentNode;
                openNodes.add(searchNode);
            } else {
                if(currentNode.gValue + (Math.abs(searchNode.blockPos.getY() - currentNode.blockPos.getY()) > 0 ? 2 : 1) < searchNode.gValue){
                    Logger.log("Found better path");
                    searchNode.lastNode = currentNode;
                    calculateCost(searchNode, endingBlockPos);
                    openNodes.remove(searchNode);
                    openNodes.add(searchNode);
                }
            }


        }

    }

    private void instantiateNode(int gridX, int gridY, int gridZ, Node startNode){
        instantiateAnyNode(gridX, gridY, gridZ, new Node(startNode.blockPos.add(gridX, gridY - startNode.blockPos.getY(), gridZ)));
    }
    private void instantiateAnyNode(int gridX, int gridY, int gridZ, Node node){
        if(gridEnvironment.get(gridX, gridY, gridZ) == null)
            gridEnvironment.set(gridX, gridY, gridZ, node);
    }

    private LinkedList<BlockPos> trackBackPath(Node goalNode, Node startNode){
        LinkedList<BlockPos> blocksToWalk = new LinkedList<>();
        blocksToWalk.add(goalNode.blockPos);
        while(!startNode.equals(goalNode) && goalNode.lastNode != null){
            blocksToWalk.add(goalNode.lastNode.blockPos);
            goalNode = goalNode.lastNode;
        }
        mc.thePlayer.addChatMessage(new ChatComponentText("Block count : " + blocksToWalk.size()));
        return blocksToWalk;
    }

    private void calculateCost(Node node, BlockPos endingBlock){
        node.hValue = getHeuristic(node.blockPos, endingBlock);

        if(node.lastNode != null)
            node.gValue = node.lastNode.gValue + ((node.lastNode.blockPos.getY() != node.blockPos.getY()) ? 2 : 1) * ((BlockUtils.isPassable(node.blockPos)) ? 0.5f : 2);
        else
            node.gValue = 1f;
        node.fValue = node.gValue + node.hValue;
    }

    double getHeuristic(BlockPos start, BlockPos goal){
        return MathUtils.getDistanceBetweenTwoBlock(start, goal);
    }

}
