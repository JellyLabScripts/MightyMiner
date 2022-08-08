package com.jelly.MightyMiner.baritone.automine.pathing;

import com.jelly.MightyMiner.baritone.automine.pathing.config.PathBehaviour;
import com.jelly.MightyMiner.baritone.logging.Logger;
import com.jelly.MightyMiner.baritone.automine.pathing.exceptions.NoBlockException;
import com.jelly.MightyMiner.baritone.automine.pathing.exceptions.NoPathException;
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

    public LinkedList<BlockNode> getPath(Block... blockType) throws NoBlockException, NoPathException {

        List<BlockPos> foundBlocks = BlockUtils.findBlock(30, blockType);
        Logger.playerLog("Found gemstones : " + foundBlocks.size());

        long pastTime = System.currentTimeMillis();

        if(foundBlocks.isEmpty())
            throw new NoBlockException();

        LinkedList<LinkedList<BlockNode>> possiblePaths = new LinkedList<>();
        for(int i = 0; i < (Math.min(foundBlocks.size(), 20)); i++){
            LinkedList<BlockNode> path = calculatePath(BlockUtils.getPlayerLoc(), foundBlocks.get(i));
            if(!path.isEmpty()){
                possiblePaths.add(path);
                BlockRenderer.renderMap.put(possiblePaths.getLast().getFirst().getBlockPos(), Color.GREEN);
                if(possiblePaths.getLast().size() == 1)
                    return possiblePaths.getLast();
            }
        }

        if(possiblePaths.isEmpty())
            throw new NoPathException();

        Logger.playerLog("Total time | Time per path : " + (System.currentTimeMillis() - pastTime) + " ms | " + (System.currentTimeMillis() - pastTime) * 1.0d / possiblePaths.size() + " ms");

        possiblePaths.sort(Comparator.comparingInt(this::calculatePathCost));
        return possiblePaths.getFirst();
    }


    private LinkedList<BlockNode> calculatePath(BlockPos startingPos, BlockPos endingBlock) {

        gridEnvironment.clear();
        checkedNodes.clear();
        openNodes.clear();

        if(BlockUtils.canSeeBlock(endingBlock) && BlockUtils.canReachBlock(endingBlock)){
            return new LinkedList<BlockNode>(){{add(new BlockNode(endingBlock, getBlockType(endingBlock)));}};
        }

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

            if(currentGridY > pathBehaviour.getMinY()) {
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
            }

            if(currentGridY < pathBehaviour.getMaxY()) {
                instantiateNode(currentGridX - 1, currentGridY + 1, currentGridZ, startNode);
                checkNode(gridEnvironment.get(currentGridX - 1, currentGridY + 1, currentGridZ), currentNode, endingBlock);

                instantiateNode(currentGridX + 1, currentGridY + 1, currentGridZ, startNode);
                checkNode(gridEnvironment.get(currentGridX + 1, currentGridY + 1, currentGridZ), currentNode, endingBlock);

                instantiateNode(currentGridX, currentGridY + 1, currentGridZ - 1, startNode);
                checkNode(gridEnvironment.get(currentGridX, currentGridY + 1, currentGridZ - 1), currentNode, endingBlock);

                instantiateNode(currentGridX, currentGridY + 1, currentGridZ + 1, startNode);
                checkNode(gridEnvironment.get(currentGridX, currentGridY + 1, currentGridZ + 1), currentNode, endingBlock);
            }

            if(currentNode.blockPos.equals(endingBlock)) {
                return trackBackPath(currentNode, startNode);
            }


        }
        return new LinkedList<>();
    }
    private void checkNode(Node searchNode, Node currentNode, BlockPos endingBlockPos){


        if (!checkedNodes.contains(searchNode) && BlockUtils.canWalkOn(searchNode.blockPos.down())){

            if(!searchNode.blockPos.equals(endingBlockPos)) {
                if(pathBehaviour.getForbiddenMiningBlocks() != null){
                    if ((pathBehaviour.getForbiddenMiningBlocks().contains(BlockUtils.getBlock(searchNode.blockPos))  && !BlockUtils.getBlock(searchNode.blockPos).equals(Blocks.air)) ||
                            (pathBehaviour.getForbiddenMiningBlocks().contains(BlockUtils.getBlock(searchNode.blockPos.up()))  && !BlockUtils.getBlock(searchNode.blockPos.up()).equals(Blocks.air)))
                        return;
                }
                if(pathBehaviour.getAllowedMiningBlocks() != null){
                    if((!pathBehaviour.getAllowedMiningBlocks().contains(BlockUtils.getBlock(searchNode.blockPos)) && !BlockUtils.getBlock(searchNode.blockPos).equals(Blocks.air)) ||
                            (!pathBehaviour.getAllowedMiningBlocks().contains(BlockUtils.getBlock(searchNode.blockPos.up())) && !BlockUtils.getBlock(searchNode.blockPos.up()).equals(Blocks.air)))
                        return;
                }
            }
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
                    blocksToMine.add(new BlockNode(currentTrackNode.blockPos, getBlockType(currentTrackNode.blockPos)));
                    if (!BlockUtils.isPassable(currentTrackNode.blockPos.up()))
                        blocksToMine.add(new BlockNode(currentTrackNode.blockPos.up(), getBlockType(currentTrackNode.blockPos.up())));

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

        if(node.lastNode != null)
            node.gValue = node.lastNode.gValue + ((node.lastNode.blockPos.getY() != node.blockPos.getY()) ? 2 : 1) * ((BlockUtils.isPassable(node.blockPos)) ? 0.5f : 2);
        else
            node.gValue = 1f;
        node.fValue = node.gValue + node.hValue;
    }
    private int calculatePathCost(List<BlockNode> nodes){
        int cost = 0;
        for(BlockNode node : nodes){
            cost += (node.getBlockType() == BlockType.WALK) ? 2 : 1; //avoid open areas
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
