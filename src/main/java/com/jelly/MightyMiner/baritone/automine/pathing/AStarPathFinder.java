package com.jelly.MightyMiner.baritone.automine.pathing;

import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.baritone.automine.pathing.exceptions.NoBlockException;
import com.jelly.MightyMiner.baritone.structures.BlockNode;
import com.jelly.MightyMiner.baritone.structures.BlockType;
import com.jelly.MightyMiner.baritone.structures.GridEnvironment;
import com.jelly.MightyMiner.baritone.structures.Node;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AStarPathFinder {
    Minecraft mc = Minecraft.getMinecraft();
    GridEnvironment<Node> gridEnvironment = new GridEnvironment<>();

    int step;

    List<Block> forbiddenMiningBlocks;
    List<Block> allowedMiningBlocks;

    List<Node> checkedNodes = new ArrayList<>();
    List<Node> openNodes = new ArrayList<>();


    public AStarPathFinder(List<Block> forbiddenMiningBlocks, List<Block> allowedMiningBlocks){
        this.forbiddenMiningBlocks = forbiddenMiningBlocks;
        this.allowedMiningBlocks = allowedMiningBlocks;
    }

    public LinkedList<BlockNode> getPath(Block... blockType) throws Exception{

        List<BlockPos> foundBlocks = BlockUtils.findBlock(30, blockType);
       /* for(BlockPos blockPos : foundBlocks) {
            BlockRenderer.renderMap.put(blockPos, Color.LIGHT_GRAY);
        }*/

        if(foundBlocks.isEmpty()){
            throw new NoBlockException();
        }

        LinkedList<BlockNode> lowestCostPath = new LinkedList<>();
        int prevCost = 9999;
        Logger.playerLog("Found blocks : " + foundBlocks.size());
        for(int i = 0; i < (Math.min(foundBlocks.size(), 15)); i++){
            LinkedList<BlockNode> currentPath;
            try {
                currentPath = calculatePath(BlockUtils.getPlayerLoc(), foundBlocks.get(i));
            } catch (Exception ignored){
                continue;
            }
            Logger.playerLog("Possible path index : " + i);
            if(!currentPath.isEmpty()) {
                int currentCost = calculatePathCost(currentPath);
                BlockRenderer.renderMap.put(currentPath.getFirst().getBlockPos(), Color.GREEN);

                if (currentCost < prevCost) {
                    Logger.playerLog("Better path detected : index : " + i);
                    lowestCostPath = currentPath;
                    prevCost = currentCost;
                }
                if (currentCost <= 3) {
                    return lowestCostPath;
                }

            }
        }
        return lowestCostPath;
    }


    private LinkedList<BlockNode> calculatePath(BlockPos startingPos, BlockPos endingBlock) throws Exception {

        gridEnvironment.clear();
        checkedNodes.clear();
        openNodes.clear();

        if(BlockUtils.canSeeBlock(endingBlock) && BlockUtils.canReachBlock(endingBlock)){
            return new LinkedList<BlockNode>(){{add(new BlockNode(endingBlock, getBlockType(endingBlock)));}};
        }

        boolean completedPathfind = false;
        Node startNode;
        Node currentNode;
        Node goalNode = new Node(endingBlock);

        int currentGridX = 0;
        int currentGridY = (int) mc.thePlayer.posY;
        int currentGridZ = 0;

        instantiateAnyNode(currentGridX, currentGridY, currentGridZ, new Node(startingPos));
        currentNode = gridEnvironment.get(currentGridX, currentGridY, currentGridZ);
        startNode = currentNode;
        step = 0;


        while (!completedPathfind) {
            step++;
            currentNode.checked = true;
            checkedNodes.add(currentNode);
            openNodes.remove(currentNode);

            instantiateNode(currentGridX - 1, currentGridY, currentGridZ, startNode);
            openNodeAndCalculateCost(gridEnvironment.get(currentGridX - 1, currentGridY, currentGridZ), currentNode, endingBlock);

             instantiateNode(currentGridX + 1, currentGridY, currentGridZ, startNode);
             openNodeAndCalculateCost(gridEnvironment.get(currentGridX + 1, currentGridY, currentGridZ), currentNode, endingBlock);


             instantiateNode(currentGridX, currentGridY, currentGridZ - 1, startNode);
             openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY, currentGridZ - 1), currentNode, endingBlock);

             instantiateNode(currentGridX, currentGridY, currentGridZ + 1, startNode);
             openNodeAndCalculateCost(gridEnvironment.get(currentGridX,  currentGridY, currentGridZ + 1), currentNode, endingBlock);


             instantiateNode(currentGridX - 1, currentGridY - 1, currentGridZ, startNode);
             openNodeAndCalculateCost(gridEnvironment.get(currentGridX - 1, currentGridY - 1, currentGridZ), currentNode, endingBlock);


             instantiateNode(currentGridX + 1, currentGridY - 1, currentGridZ, startNode);
             openNodeAndCalculateCost(gridEnvironment.get(currentGridX + 1, currentGridY - 1, currentGridZ), currentNode, endingBlock);


             instantiateNode(currentGridX, currentGridY - 1, currentGridZ - 1, startNode);
             openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY - 1, currentGridZ - 1), currentNode, endingBlock);


             instantiateNode(currentGridX, currentGridY - 1, currentGridZ + 1, startNode);
             openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY - 1, currentGridZ + 1), currentNode, endingBlock);

             instantiateNode(currentGridX - 1, currentGridY + 1, currentGridZ, startNode);
             openNodeAndCalculateCost(gridEnvironment.get(currentGridX - 1, currentGridY + 1, currentGridZ), currentNode, endingBlock);

             instantiateNode(currentGridX + 1, currentGridY + 1, currentGridZ, startNode);
             openNodeAndCalculateCost(gridEnvironment.get(currentGridX + 1, currentGridY + 1, currentGridZ), currentNode, endingBlock);

             instantiateNode(currentGridX, currentGridY + 1, currentGridZ - 1, startNode);
             openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY + 1, currentGridZ - 1), currentNode, endingBlock);

             instantiateNode(currentGridX, currentGridY + 1, currentGridZ + 1, startNode);
             openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY + 1, currentGridZ + 1), currentNode, endingBlock);

             instantiateNode(currentGridX, currentGridY - 1, currentGridZ, startNode);
             openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY - 1, currentGridZ), currentNode, endingBlock);



            int bestIndex = 0;
            double minFcost = 9999;

            for (int i = 0; i < openNodes.size(); i++) {
                if(openNodes.get(i).hValue == 0){
                    bestIndex = i;
                    break;
                }
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
            if(step > 30000)
            {
                Logger.playerLog("Steps exceeded");
                throw new Exception();
            }
        }
        return trackBackPath(currentNode, startNode);



    }
    private void openNodeAndCalculateCost(Node searchNode, Node currentNode, BlockPos endingBlockPos){

        if ( (!searchNode.checked
                && !searchNode.opened
                && BlockUtils.canWalkOn(searchNode.blockPos.down()))){

            if(!searchNode.blockPos.equals(endingBlockPos)) {
                if (forbiddenMiningBlocks != null && forbiddenMiningBlocks.contains(BlockUtils.getBlock(searchNode.blockPos))  && !BlockUtils.getBlock(searchNode.blockPos).equals(Blocks.air))
                    return;

                if (allowedMiningBlocks != null && !allowedMiningBlocks.contains(BlockUtils.getBlock(searchNode.blockPos)) && !BlockUtils.getBlock(searchNode.blockPos).equals(Blocks.air))
                    return;
            }
            BlockRenderer.renderMap.put(searchNode.blockPos, Color.cyan);

            searchNode.opened = true;
            searchNode.lastNode = currentNode;
            openNodes.add(searchNode);
            calculateCost(searchNode, endingBlockPos);
        }

    }

    private void instantiateNode(int gridX, int gridY, int gridZ, Node startNode){
        instantiateAnyNode(gridX, gridY, gridZ, new Node(startNode.blockPos.add(gridX, gridY - startNode.blockPos.getY(), gridZ)));
    }
    private void instantiateAnyNode(int gridX, int gridY, int gridZ, Node node){
        if(gridEnvironment.get(gridX, gridY, gridZ) == null)
            gridEnvironment.set(gridX, gridY, gridZ, node);
    }

    private LinkedList<BlockNode> trackBackPath(Node goalNode, Node startNode){
        LinkedList<BlockNode> blocksToMine = new LinkedList<>();

        Node formerNode = null;
        Node currentTrackNode = null;


        if(goalNode.lastNode != null && goalNode.lastNode.blockPos != null){
            blocksToMine.add(new BlockNode(goalNode.blockPos, getBlockType(goalNode.blockPos)));
            if (goalNode.lastNode.blockPos.getY() > goalNode.blockPos.getY()) {
                if (!BlockUtils.isPassable(goalNode.blockPos.up())) {
                    blocksToMine.add(new BlockNode(goalNode.blockPos.up(), getBlockType(goalNode.blockPos.up())));
                }
            } else if(goalNode.lastNode.blockPos.getY() == goalNode.blockPos.getY()){
                if(AngleUtils.shouldLookAtCenter(goalNode.blockPos))
                    blocksToMine.add(new BlockNode(goalNode.blockPos.up(), getBlockType(goalNode.blockPos.up())));
            }
            formerNode = goalNode;
            currentTrackNode = goalNode.lastNode;
        }


        if (currentTrackNode != null && currentTrackNode.lastNode != null) {
            do {
                if(currentTrackNode.lastNode.blockPos.getY() > currentTrackNode.blockPos.getY()){
                    blocksToMine.add(new BlockNode(currentTrackNode.blockPos, getBlockType(currentTrackNode.blockPos)));

                    if (!BlockUtils.isPassable(currentTrackNode.blockPos.up())) {
                        blocksToMine.add(new BlockNode(currentTrackNode.blockPos.up(), getBlockType(currentTrackNode.blockPos.up())));
                    }
                    if (!BlockUtils.isPassable(currentTrackNode.blockPos.up(2))) {
                        blocksToMine.add(new BlockNode(currentTrackNode.blockPos.up(2), getBlockType(currentTrackNode.blockPos.up(2))));
                    }
                } else if (formerNode.blockPos.getY() > currentTrackNode.blockPos.getY()) {
                    if (!BlockUtils.isPassable(currentTrackNode.blockPos.up(2)) &&
                            ( (!formerNode.blockPos.equals(goalNode.blockPos) && !BlockUtils.isPassable(currentTrackNode.blockPos)) || BlockUtils.isPassable(currentTrackNode.blockPos))) {
                        blocksToMine.add(new BlockNode(currentTrackNode.blockPos.up(2), getBlockType(currentTrackNode.blockPos.up(2))));
                    }
                    if (!BlockUtils.isPassable(currentTrackNode.blockPos.up())) {
                        blocksToMine.add(new BlockNode(currentTrackNode.blockPos.up(), getBlockType(currentTrackNode.blockPos.up())));
                    }

                    blocksToMine.add(new BlockNode(currentTrackNode.blockPos, getBlockType(currentTrackNode.blockPos)));
                } else {

                    if((AngleUtils.shouldLookAtCenter(currentTrackNode.blockPos.up()) && !AngleUtils.shouldLookAtCenter(currentTrackNode.blockPos))) {


                        if (!BlockUtils.isPassable(currentTrackNode.blockPos.up()))
                            blocksToMine.add(new BlockNode(currentTrackNode.blockPos.up(), getBlockType(currentTrackNode.blockPos.up())));

                        blocksToMine.add(new BlockNode(currentTrackNode.blockPos, getBlockType(currentTrackNode.blockPos)));

                    } else {

                        blocksToMine.add(new BlockNode(currentTrackNode.blockPos, getBlockType(currentTrackNode.blockPos)));

                        if (!BlockUtils.isPassable(currentTrackNode.blockPos.up()))
                            blocksToMine.add(new BlockNode(currentTrackNode.blockPos.up(), getBlockType(currentTrackNode.blockPos.up())));

                    }
                }
                formerNode = currentTrackNode;
                currentTrackNode = currentTrackNode.lastNode;
            } while(!startNode.equals(currentTrackNode) && currentTrackNode.lastNode.blockPos != null);

            //add back one block when needed
            if((blocksToMine.getLast().getBlockPos().getY() >= (int)mc.thePlayer.posY + 1 && blocksToMine.get(blocksToMine.size() - 2).getBlockPos().getY() >= (int)mc.thePlayer.posY + 1)){
                blocksToMine.add(new BlockNode(BlockUtils.getPlayerLoc().up(2), getBlockType(BlockUtils.getPlayerLoc().up(2))));
            }

        }
        Logger.playerLog("Block count : " + blocksToMine.size());
        return blocksToMine;
    }

    private void calculateCost(Node node, BlockPos endingBlock){
        node.hValue = getHeuristic(node.blockPos, endingBlock);

        if(node.lastNode != null) {
            if(node.lastNode.blockPos.getY() != node.blockPos.getY()) {
                node.gValue = node.lastNode.gValue + 2;
            } else {
                node.gValue = node.lastNode.gValue + 1;
            }
        }
        else
            node.gValue = 1f;

    //    node.gValue *= BlockUtils.isPassable(node.blockPos) ? 0.5f : 2f;
        node.fValue = node.gValue + node.hValue;
    }
    private int calculatePathCost(List<BlockNode> nodes){
        int cost = 0;
        for(BlockNode node : nodes){
            cost += (node.getBlockType() == BlockType.WALK) ? 2 : 1;
        }
        return cost;
    }

    BlockType getBlockType (BlockPos blockToSearch) {
        return BlockUtils.isPassable(blockToSearch) ? BlockType.WALK : BlockType.MINE;
    }

    double getHeuristic(BlockPos start, BlockPos goal){
        return MathUtils.getDistanceBetweenTwoBlock(start, goal);
    }
}
