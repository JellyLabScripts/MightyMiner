package com.jelly.MightyMiner.baritone.automine.calculations;

import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathFinderBehaviour;
import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathMode;
import com.jelly.MightyMiner.baritone.automine.calculations.exceptions.ChunkLoadException;
import com.jelly.MightyMiner.baritone.automine.calculations.exceptions.NoPathException;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.baritone.automine.movement.Moves;
import com.jelly.MightyMiner.baritone.automine.structures.BlockNode;
import com.jelly.MightyMiner.baritone.automine.structures.BlockType;
import com.jelly.MightyMiner.baritone.automine.structures.GridEnvironment;
import com.jelly.MightyMiner.baritone.automine.structures.Node;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.BlockPos;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

public class AStarCalculator{



    Minecraft mc = Minecraft.getMinecraft();
    GridEnvironment<Node> gridEnvironment = new GridEnvironment<>();

    PriorityQueue<Node> openNodes = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fValue));;

    int step;
    int stepLimit;

    PathFinderBehaviour pathFinderBehaviour;
    PathMode mode;



    public LinkedList<BlockNode> calculateStaticPath(BlockPos goalBlock){

        if (BlockUtils.canSeeBlock(goalBlock) && BlockUtils.canReachBlock(goalBlock))
            return new LinkedList<BlockNode>() {
                {
                    add(new BlockNode(goalBlock, BlockType.MINE));
                }
            };
        return new LinkedList<>();
    }

    public LinkedList<BlockNode> calculatePath(BlockPos startingPos, BlockPos endingBlock, PathFinderBehaviour pathFinderBehaviour, PathMode mode) {
        return calculatePath(startingPos, endingBlock, pathFinderBehaviour, mode, 3000);
    }


    public LinkedList<BlockNode> calculatePath(BlockPos startingPos, BlockPos endingBlock, PathFinderBehaviour pathFinderBehaviour, PathMode mode, int stepLimit) {


        this.pathFinderBehaviour = pathFinderBehaviour;
        this.mode = mode;
        this.stepLimit = stepLimit;

        gridEnvironment.clear();
        openNodes.clear();

        step = 0;


        if (BlockUtils.canSeeBlock(endingBlock) && BlockUtils.canReachBlock(endingBlock)) {
            step++;
            return new LinkedList<BlockNode>() {
                {
                    add(new BlockNode(endingBlock, BlockType.MINE));
                }
            };
        }

        int currentGridX = 0;
        int currentGridY = (int)mc.thePlayer.posY;
        int currentGridZ = 0;

        instantiateAnyNode(currentGridX, currentGridY, currentGridZ, new Node(startingPos));
        Node startNode = gridEnvironment.get(currentGridX, currentGridY, currentGridZ);
        Node currentNode;

        openNodes.add(startNode);


        while (!openNodes.isEmpty()) {


            currentNode = openNodes.poll();

            if (currentNode != null && currentNode.blockPos.equals(endingBlock))
                return trackBackPath(true, currentNode, startNode);

            openNodes.remove(currentNode);


            assert currentNode != null;
           // KeybindHandler.debugBlockRenderer.renderMap.put(currentNode.blockPos, Color.ORANGE);
            if (currentNode.lastNode != null) {
                currentGridX = currentNode.blockPos.getX() - startNode.blockPos.getX();
                currentGridY = currentNode.blockPos.getY();
                currentGridZ = currentNode.blockPos.getZ() - startNode.blockPos.getZ();
            }
            step++;


            if (step > stepLimit)
                break;
            for (Moves move : Moves.values()) {

                instantiateNode(currentGridX + move.dx, currentGridY + move.dy, currentGridZ + move.dz, startNode);
                try {
                    checkNode(move, gridEnvironment.get(currentGridX + move.dx, currentGridY + move.dy, currentGridZ + move.dz), currentNode, endingBlock);
                }catch (ChunkLoadException e){
                    return trackBackPath(false, currentNode, startNode);
                }
            }

        }
        return new LinkedList<>();
    }

    public int getSteps(){
        return step;
    }
    private void checkNode(Moves move, Node searchNode, Node currentNode, BlockPos endingBlockPos) throws ChunkLoadException{
        if(!mc.theWorld.getChunkFromBlockCoords(searchNode.blockPos).isLoaded()){
            throw new ChunkLoadException();
        }

        if (BlockUtils.isPassable(searchNode.blockPos.down()))
            return;

        if (!searchNode.blockPos.equals(endingBlockPos)) {

            if(pathFinderBehaviour.getForbiddenMiningBlocks() != null){
                switch (move){
                    case ASCEND_EAST: case ASCEND_NORTH: case ASCEND_SOUTH: case ASCEND_WEST:
                        if(pathFinderBehaviour.getForbiddenMiningBlocks().contains(BlockUtils.getBlockCached(currentNode.blockPos.up(2))))
                            return;
                        break;
                    case DESCEND_EAST: case DESCEND_NORTH: case DESCEND_SOUTH: case DESCEND_WEST:
                        if(pathFinderBehaviour.getForbiddenMiningBlocks().contains(BlockUtils.getBlockCached(searchNode.blockPos.up(2))))
                            return;
                        break;
                }
                if(pathFinderBehaviour.getForbiddenMiningBlocks().contains(BlockUtils.getBlockCached(searchNode.blockPos)) || pathFinderBehaviour.getForbiddenMiningBlocks().contains(BlockUtils.getBlockCached(searchNode.blockPos.up())))
                    return;

            }

            if(pathFinderBehaviour.getAllowedMiningBlocks() != null){
                switch (move){
                    case ASCEND_EAST: case ASCEND_NORTH: case ASCEND_SOUTH: case ASCEND_WEST:
                        if(!pathFinderBehaviour.getAllowedMiningBlocks().contains(BlockUtils.getBlockCached(currentNode.blockPos.up(2))))
                            return;
                        break;
                    case DESCEND_EAST: case DESCEND_NORTH: case DESCEND_SOUTH: case DESCEND_WEST:
                        if(!pathFinderBehaviour.getAllowedMiningBlocks().contains(BlockUtils.getBlockCached(searchNode.blockPos.up(2))))
                            return;
                        break;
                }
                if(!pathFinderBehaviour.getAllowedMiningBlocks().contains(BlockUtils.getBlockCached(searchNode.blockPos)) || !pathFinderBehaviour.getAllowedMiningBlocks().contains(BlockUtils.getBlockCached(searchNode.blockPos.up())))
                    return;

            }
        }

        switch (move) {
            case DIAGONAL_NORTHEAST: case DIAGONAL_NORTHWEST: case DIAGONAL_SOUTHEAST: case DIAGONAL_SOUTHWEST:
                if (!BlockUtils.isPassable(new BlockPos(searchNode.blockPos.getX(), searchNode.blockPos.getY(), currentNode.blockPos.getZ())) || !BlockUtils.isPassable(new BlockPos(currentNode.blockPos.getX(), searchNode.blockPos.getY(), searchNode.blockPos.getZ())) ||
                        !BlockUtils.isPassable(new BlockPos(searchNode.blockPos.getX(), searchNode.blockPos.getY() + 1, currentNode.blockPos.getZ())) || !BlockUtils.isPassable(new BlockPos(currentNode.blockPos.getX(), searchNode.blockPos.getY() + 1, searchNode.blockPos.getZ())))
                    return;
                break;
            case DOWN:
                if (BlockUtils.isPassable(searchNode.blockPos))
                    return;
                break;
        }


        double gCost = getMoveCost(move, searchNode.blockPos) + currentNode.gValue;

        if(searchNode.gValue == -1 || gCost < searchNode.gValue){

            searchNode.lastNode = currentNode;
            setCost(searchNode, gCost, getHeuristic(searchNode.blockPos, endingBlockPos));
            if(!openNodes.contains(searchNode)){
                openNodes.add(searchNode);
            } else {
                openNodes.remove(searchNode);
                openNodes.add(searchNode);
            }
        }
    }

    private void instantiateNode(int gridX, int gridY, int gridZ, Node startNode) {
        instantiateAnyNode(gridX, gridY, gridZ, new Node(startNode.blockPos.add(gridX, gridY - startNode.blockPos.getY(), gridZ)));
    }

    private void instantiateAnyNode(int gridX, int gridY, int gridZ, Node node) {
        if (gridEnvironment.get(gridX, gridY, gridZ) == null)
            gridEnvironment.set(gridX, gridY, gridZ, node);
    }

    // structure -> Actual blocks [][]...[][][] + [] last one which tells if the path is complete
    private LinkedList<BlockNode> trackBackPath(boolean isFullPath, Node goalNode, Node startNode) {
        LinkedList<BlockNode> blocksToMine = new LinkedList<>();
        Node formerNode = null;
        Node currentTrackNode = null;

        if(mode == PathMode.MINE) {
            if (goalNode.lastNode != null && goalNode.lastNode.blockPos != null) {
                blocksToMine.add(new BlockNode(goalNode.blockPos, getBlockType(goalNode.blockPos)));
                if (goalNode.lastNode.blockPos.getY() > goalNode.blockPos.getY()) {

                    if (!BlockUtils.isPassable(goalNode.blockPos.up()))
                        blocksToMine.add(new BlockNode(goalNode.blockPos.up(), getBlockType(goalNode.blockPos.up())));
                    if (!BlockUtils.isPassable(goalNode.blockPos.up(2)) && getBlockType(goalNode.blockPos.up(2)) == BlockType.WALK)
                        blocksToMine.add(new BlockNode(goalNode.blockPos.up(2), getBlockType(goalNode.blockPos.up(2))));
                } else if (goalNode.lastNode.blockPos.getY() == goalNode.blockPos.getY() && (
                        AngleUtils.shouldLookAtCenter(goalNode.blockPos) /*|| getBlockType(goalNode.blockPos.up()) == BlockType.WALK)*/)) {
                    blocksToMine.add(new BlockNode(goalNode.blockPos.up(), BlockType.MINE/*getBlockType(goalNode.blockPos.up())*/));
                }
                formerNode = goalNode;
                currentTrackNode = goalNode.lastNode;
            }
        } else {
            formerNode = goalNode;
            currentTrackNode = goalNode;
        }

        if (currentTrackNode != null && currentTrackNode.lastNode != null) {
            do {
                if (currentTrackNode.lastNode.blockPos.getY() > currentTrackNode.blockPos.getY()) { // going down

                    blocksToMine.add(new BlockNode(currentTrackNode.blockPos, getBlockType(currentTrackNode.blockPos)));
                    if (!BlockUtils.isPassable(currentTrackNode.blockPos.up()))
                        blocksToMine.add(new BlockNode(currentTrackNode.blockPos.up(), getBlockType(currentTrackNode.blockPos.up())));
                    if (!BlockUtils.isPassable(currentTrackNode.blockPos.up(2)))
                        blocksToMine.add(new BlockNode(currentTrackNode.blockPos.up(2), getBlockType(currentTrackNode.blockPos.up(2))));

                } else if (formerNode.blockPos.getY() > currentTrackNode.blockPos.getY()) { // going up

                    if (!BlockUtils.isPassable(currentTrackNode.blockPos.up(2)) && ((
                            !formerNode.blockPos.equals(goalNode.blockPos) && !BlockUtils.isPassable(currentTrackNode.blockPos)) || BlockUtils.isPassable(currentTrackNode.blockPos)))
                        blocksToMine.add(new BlockNode(currentTrackNode.blockPos.up(2), getBlockType(currentTrackNode.blockPos.up(2))));
                    if (!BlockUtils.isPassable(currentTrackNode.blockPos.up()))
                        blocksToMine.add(new BlockNode(currentTrackNode.blockPos.up(), getBlockType(currentTrackNode.blockPos.up())));
                    blocksToMine.add(new BlockNode(currentTrackNode.blockPos, getBlockType(currentTrackNode.blockPos)));

                } else {
                    blocksToMine.add(new BlockNode(currentTrackNode.blockPos, getBlockType(currentTrackNode.blockPos)));
                    if (!BlockUtils.isPassable(currentTrackNode.blockPos.up()))
                        blocksToMine.add(new BlockNode(currentTrackNode.blockPos.up(), getBlockType(currentTrackNode.blockPos.up())));

                }
                formerNode = currentTrackNode;
                currentTrackNode = currentTrackNode.lastNode;
            } while (!startNode.equals(currentTrackNode) && currentTrackNode.lastNode.blockPos != null);

            if ((blocksToMine.getLast()).getBlockPos().getY() >= (int)mc.thePlayer.posY + 1 && (blocksToMine.get(blocksToMine.size() - 2)).getBlockPos().getY() >= (int)mc.thePlayer.posY + 1)
                blocksToMine.add(new BlockNode(BlockUtils.getPlayerLoc().up(2), getBlockType(BlockUtils.getPlayerLoc().up(2))));
        }

        blocksToMine.add(new BlockNode(isFullPath)); // only as recognising function, NOT real blockNode!

        Logger.log("Block count : " + blocksToMine.size());
        return blocksToMine;
    }

    private void setCost(Node node, double gCost, double hCost) {
        node.gValue = gCost;
        node.hValue = hCost;
        node.fValue = gCost + hCost;
    }

    private double getMoveCost(Moves move, BlockPos blockPos) {
        return move.cost + (BlockUtils.isPassable(blockPos) ? 0 : 0.5f);
    }


    private BlockType getBlockType(BlockPos blockToSearch) {
        return BlockUtils.isPassable(blockToSearch) ? BlockType.WALK : BlockType.MINE;
    }


    private double getHeuristic(BlockPos start, BlockPos goal) {
        return MathUtils.getBlockDistanceBetweenTwoBlock(start, goal);
    }
}
