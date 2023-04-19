package com.jelly.MightyMiner.baritone.automine.calculations;

import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathFinderBehaviour;
import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathMode;
import com.jelly.MightyMiner.baritone.automine.calculations.exceptions.ChunkLoadException;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.baritone.automine.movement.Moves;
import com.jelly.MightyMiner.baritone.automine.structures.BlockNode;
import com.jelly.MightyMiner.baritone.automine.structures.BlockType;
import com.jelly.MightyMiner.baritone.automine.structures.Node;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.Utils.MathUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import org.joml.Vector3i;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

public class AStarCalculator {
    Minecraft mc = Minecraft.getMinecraft();

    ConcurrentHashMap<Vector3i, Node> nodes = new ConcurrentHashMap<>();

    PriorityQueue<Node> openNodes = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));

    @Getter
    private int step;

    PathFinderBehaviour pathFinderBehaviour;
    PathMode mode;


    public LinkedList<BlockNode> calculateStaticPath(BlockPos goalBlock) {
        LinkedList<BlockNode> path = new LinkedList<>();
        if (BlockUtils.canMineBlock(goalBlock)) {
            path.add(new BlockNode(goalBlock, BlockType.MINE));
        }
        return path;
    }


    public LinkedList<BlockNode> calculatePath(BlockPos startingPos, BlockPos endingBlock, PathFinderBehaviour pathFinderBehaviour, PathMode mode, int stepLimit) {
        this.pathFinderBehaviour = pathFinderBehaviour;
        this.mode = mode;

        nodes.clear();
        openNodes.clear();

        step = 0;

        // sanity check if we are at the destination already
        if (BlockUtils.canMineBlock(endingBlock) && this.mode == PathMode.MINE) {
            step++;
            LinkedList<BlockNode> blockNodes = new LinkedList<>();

            blockNodes.add(new BlockNode(endingBlock, BlockType.MINE));

            return blockNodes;
        }


        Vector3i currentGridPos = new Vector3i(0, (int) mc.thePlayer.posY, 0);

        instantiateAnyNode(currentGridPos, new Node(startingPos));
        Node startNode = nodes.get(currentGridPos);
        Node currentNode;

        openNodes.add(startNode);

        while (!openNodes.isEmpty()) {
            currentNode = openNodes.poll();

            if (currentNode != null) {
                // reconstruct path
                if (currentNode.pos.equals(endingBlock)) {
                    return trackBackPath(true, currentNode, startNode);
                }

                openNodes.remove(currentNode);

                //KeybindHandler.debugBlockRenderer.renderMap.put(currentNode.blockPos, Color.ORANGE);
                if (currentNode.parentNode != null) {
                    currentGridPos.x = currentNode.pos.getX() - startNode.pos.getX();
                    currentGridPos.y = currentNode.pos.getY();
                    currentGridPos.z = currentNode.pos.getZ() - startNode.pos.getZ();
                }

                // stop if we went over iteration limit
                step++;
                if (step > stepLimit) {
                    break;
                }

                for (Moves move : Moves.values()) {
                    Vector3i pos = currentGridPos.add(move.dx, move.dy, move.dz, new Vector3i());
                    instantiateNode(pos, startNode);
                    try {
                        checkNode(move, nodes.get(pos), currentNode, endingBlock);
                    } catch (ChunkLoadException e) {
                        return trackBackPath(false, currentNode, startNode);
                    }
                }
            }
        }

        // pathfinding failed so return empty list
        return new LinkedList<>();
    }

    private void checkNode(Moves move, Node searchNode, Node currentNode, BlockPos endingBlockPos) throws ChunkLoadException {

        if (!mc.theWorld.getChunkFromBlockCoords(searchNode.pos).isLoaded()) {
            throw new ChunkLoadException();
        }

        if (BlockUtils.isPassable(searchNode.pos.down())) {
            return;
        }

        // Cannot use getBlockCached() here because if blocks are updated it would cause the path to be un-walkable!
        if (pathFinderBehaviour.getForbiddenMiningBlocks() != null) {
            switch (move) {
                case ASCEND_EAST:
                case ASCEND_NORTH:
                case ASCEND_SOUTH:
                case ASCEND_WEST:
                    if (pathFinderBehaviour.getForbiddenMiningBlocks().contains(BlockUtils.getBlock(currentNode.pos.up(2))))
                        return;
                    break;
                case DESCEND_EAST:
                case DESCEND_NORTH:
                case DESCEND_SOUTH:
                case DESCEND_WEST:
                    if (pathFinderBehaviour.getForbiddenMiningBlocks().contains(BlockUtils.getBlock(searchNode.pos.up(2))))
                        return;
                    break;
            }
            if (pathFinderBehaviour.getForbiddenMiningBlocks().contains(BlockUtils.getBlock(searchNode.pos)) || pathFinderBehaviour.getForbiddenMiningBlocks().contains(BlockUtils.getBlock(searchNode.pos.up())))
                return;

        }

        if (pathFinderBehaviour.getAllowedMiningBlocks() != null) {
            switch (move) {
                case ASCEND_EAST:
                case ASCEND_NORTH:
                case ASCEND_SOUTH:
                case ASCEND_WEST:
                    if (!pathFinderBehaviour.getAllowedMiningBlocks().contains(BlockUtils.getBlock(currentNode.pos.up(2))))
                        return;
                    break;
                case DESCEND_EAST:
                case DESCEND_NORTH:
                case DESCEND_SOUTH:
                case DESCEND_WEST:
                    if (!pathFinderBehaviour.getAllowedMiningBlocks().contains(BlockUtils.getBlock(searchNode.pos.up(2))))
                        return;
                    break;
            }
            if (!pathFinderBehaviour.getAllowedMiningBlocks().contains(BlockUtils.getBlock(searchNode.pos)) || !pathFinderBehaviour.getAllowedMiningBlocks().contains(BlockUtils.getBlock(searchNode.pos.up())))
                return;

        }


        switch (move) {
            case DIAGONAL_NORTHEAST:
            case DIAGONAL_NORTHWEST:
            case DIAGONAL_SOUTHEAST:
            case DIAGONAL_SOUTHWEST:
                BlockPos block3 = new BlockPos(searchNode.pos.getX(), searchNode.pos.getY(), currentNode.pos.getZ());
                BlockPos block4 = new BlockPos(currentNode.pos.getX(), searchNode.pos.getY(), searchNode.pos.getZ());
                if (!BlockUtils.fitsPlayer(searchNode.pos.down()) || !BlockUtils.fitsPlayer(currentNode.pos.down()) || !BlockUtils.fitsPlayer(block3.down()) || !BlockUtils.fitsPlayer(block4.down())) {
                    return;
                }
                break;
            case DOWN:
                if (BlockUtils.isPassable(searchNode.pos)) {
                    return;
                }
                break;
        }


        double gCost = getMoveCost(move, searchNode.pos) + currentNode.gCost;

        if (searchNode.gCost == -1 || gCost < searchNode.gCost) {

            searchNode.parentNode = currentNode;
            setCost(searchNode, gCost, getHeuristic(searchNode.pos, endingBlockPos));
            if (!openNodes.contains(searchNode)) {
                openNodes.add(searchNode);
            } else {
                openNodes.remove(searchNode);
                openNodes.add(searchNode);
            }
        }
    }


    private void instantiateNode(Vector3i in, Node startNode) {
        instantiateAnyNode(in, new Node(startNode.pos.add(in.x, in.y - startNode.pos.getY(), in.z)));
    }

    private void instantiateAnyNode(Vector3i in, Node node) {
        nodes.computeIfAbsent(in, k -> node);
    }


    // structure -> Actual blocks [][]...[][][] + [] last one which tells if the path is complete
    private LinkedList<BlockNode> trackBackPath(boolean isFullPath, Node goalNode, Node startNode) {
        LinkedList<BlockNode> blocksToMine = new LinkedList<>();
        Node formerNode = null;
        Node currentTrackNode = null;

        if (mode == PathMode.MINE) {
            if (goalNode.parentNode != null && goalNode.parentNode.pos != null) {
                blocksToMine.add(new BlockNode(goalNode.pos, getBlockType(goalNode.pos)));
                if (goalNode.parentNode.pos.getY() > goalNode.pos.getY()) {

                    if (!BlockUtils.isPassable(goalNode.pos.up()))
                        blocksToMine.add(new BlockNode(goalNode.pos.up(), getBlockType(goalNode.pos.up())));
                    if (!BlockUtils.isPassable(goalNode.pos.up(2)) && getBlockType(goalNode.pos.up(2)) == BlockType.WALK)
                        blocksToMine.add(new BlockNode(goalNode.pos.up(2), getBlockType(goalNode.pos.up(2))));
                } else if (goalNode.parentNode.pos.getY() == goalNode.pos.getY() && (
                        AngleUtils.shouldLookAtCenter(goalNode.pos) /*|| getBlockType(goalNode.blockPos.up()) == BlockType.WALK)*/)) {
                    blocksToMine.add(new BlockNode(goalNode.pos.up(), BlockType.MINE/*getBlockType(goalNode.blockPos.up())*/));
                }
                formerNode = goalNode;
                currentTrackNode = goalNode.parentNode;
            }
        } else {
            formerNode = goalNode;
            currentTrackNode = goalNode;
        }

        if (currentTrackNode != null && currentTrackNode.parentNode != null) {
            do {

                BlockNode e = new BlockNode(currentTrackNode.pos.up(2), getBlockType(currentTrackNode.pos.up(2)));
                BlockNode e1 = new BlockNode(currentTrackNode.pos, getBlockType(currentTrackNode.pos));
                BlockNode e2 = new BlockNode(currentTrackNode.pos.up(), getBlockType(currentTrackNode.pos.up()));

                if (currentTrackNode.parentNode.pos.getY() > currentTrackNode.pos.getY()) { // going down

                    blocksToMine.add(e1);
                    if (!BlockUtils.isPassable(currentTrackNode.pos.up())) {
                        blocksToMine.add(e2);
                    }
                    if (!BlockUtils.isPassable(currentTrackNode.pos.up(2))) {
                        blocksToMine.add(e);
                    }

                } else if (formerNode.pos.getY() > currentTrackNode.pos.getY()) { // going up

                    if (!BlockUtils.isPassable(currentTrackNode.pos.up(2)) && ((
                            !formerNode.pos.equals(goalNode.pos) && !BlockUtils.isPassable(currentTrackNode.pos)) || BlockUtils.isPassable(currentTrackNode.pos))) {
                        blocksToMine.add(e);
                    }
                    if (!BlockUtils.isPassable(currentTrackNode.pos.up())) {
                        blocksToMine.add(e2);
                    }
                    blocksToMine.add(e1);

                } else {

                    blocksToMine.add(e1);

                    if (!BlockUtils.isPassable(currentTrackNode.pos.up())) {
                        blocksToMine.add(e2);
                    }
                }
                formerNode = currentTrackNode;
                currentTrackNode = currentTrackNode.parentNode;
            } while (!startNode.equals(currentTrackNode) && currentTrackNode.parentNode.pos != null);

            if ((blocksToMine.getLast()).getPos().getY() >= (int) mc.thePlayer.posY + 1 && (blocksToMine.get(blocksToMine.size() - 2)).getPos().getY() >= (int) mc.thePlayer.posY + 1) {
                blocksToMine.add(new BlockNode(BlockUtils.getPlayerLoc().up(2), getBlockType(BlockUtils.getPlayerLoc().up(2))));
            }
        }

        blocksToMine.add(new BlockNode(isFullPath));
        Logger.log("Block count : " + blocksToMine.size());
        return blocksToMine;
    }

    private void setCost(Node node, double gCost, double hCost) {
        node.gCost = gCost;
        node.hCost = hCost;
        node.fCost = gCost + hCost;
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
