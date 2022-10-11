package com.jelly.MightyMiner.baritone.automine.pathing;

import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.baritone.automine.movement.Moves;
import com.jelly.MightyMiner.baritone.automine.pathing.config.PathBehaviour;
import com.jelly.MightyMiner.baritone.automine.pathing.exceptions.NoBlockException;
import com.jelly.MightyMiner.baritone.automine.pathing.exceptions.NoPathException;
import com.jelly.MightyMiner.baritone.automine.structures.BlockNode;
import com.jelly.MightyMiner.baritone.automine.structures.BlockType;
import com.jelly.MightyMiner.baritone.automine.structures.GridEnvironment;
import com.jelly.MightyMiner.baritone.automine.structures.Node;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.MathUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class AStarPathFinder {
    Minecraft mc = Minecraft.getMinecraft();

    GridEnvironment<Node> gridEnvironment = new GridEnvironment<>();

    int step;

    PathBehaviour pathBehaviour;

    List<Node> checkedNodes = new ArrayList<>();

    PriorityQueue<Node> openNodes;

    ArrayList<BlockPos> blackListedPos;

    public AStarPathFinder(PathBehaviour options) {
        this.openNodes = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fValue));
        this.blackListedPos = new ArrayList<>();
        this.pathBehaviour = options;
    }

    public void addToBlackList(BlockPos... blackListedPos) {
        this.blackListedPos.addAll(Arrays.asList(blackListedPos));
    }

    public void removeFromBlackList(BlockPos blockPos) {
        this.blackListedPos.remove(blockPos);
    }

    public LinkedList<BlockNode> getPathWithPreference(Block... blockType) throws NoBlockException, NoPathException {
        LinkedList<LinkedList<BlockNode>> possiblePaths = new LinkedList<>();
        if (BlockUtils.findBlock(this.pathBehaviour.getSearchRadius() * 2, this.blackListedPos, this.pathBehaviour.getMinY(), this.pathBehaviour.getMaxY(), blockType).isEmpty())
            throw new NoBlockException();
        for (Block block : blockType) {
            List<BlockPos> foundBlocks = BlockUtils.findBlock(this.pathBehaviour.getSearchRadius() * 2, this.blackListedPos, this.pathBehaviour.getMinY(), this.pathBehaviour.getMaxY(), new Block[] { block });
            for (int i = 0; i < Math.min(foundBlocks.size(), 20); i++) {
                LinkedList<BlockNode> path = calculatePath(BlockUtils.getPlayerLoc(), foundBlocks.get(i));
                if (!path.isEmpty() && (
                        !this.pathBehaviour.isStaticMode() || path.size() <= 1))
                    possiblePaths.add(path);
            }
            if (!possiblePaths.isEmpty()) {
                possiblePaths.sort(Comparator.comparingDouble(this::calculatePathCost));
                return possiblePaths.getFirst();
            }
        }
        throw new NoPathException();
    }

    public LinkedList<BlockNode> getPath(Block... blockType) throws NoBlockException, NoPathException {
        for (Block block : blockType)
            Logger.playerLog(block.toString());
        List<BlockPos> foundBlocks = BlockUtils.findBlock(this.pathBehaviour.getSearchRadius() * 2, this.blackListedPos, this.pathBehaviour.getMinY(), this.pathBehaviour.getMaxY(), blockType);
        Logger.playerLog("Found blocks : " + foundBlocks.size());
        long pastTime = System.currentTimeMillis();
        if (foundBlocks.isEmpty())
            throw new NoBlockException();
        LinkedList<LinkedList<BlockNode>> possiblePaths = new LinkedList<>();
        for (int i = 0; i < Math.min(foundBlocks.size(), 20); i++) {
            LinkedList<BlockNode> path = calculatePath(BlockUtils.getPlayerLoc(), foundBlocks.get(i));
            if (!path.isEmpty() && (
                    !this.pathBehaviour.isStaticMode() || path.size() <= 1))
                possiblePaths.add(path);
        }
        if (possiblePaths.isEmpty())
            throw new NoPathException();
        Logger.playerLog("Total time | Time per path : " + (System.currentTimeMillis() - pastTime) + " ms | " + ((System.currentTimeMillis() - pastTime) * 1.0D / possiblePaths.size()) + " ms");
        possiblePaths.sort(Comparator.comparingDouble(this::calculatePathCost));
        return possiblePaths.getFirst();
    }

    public LinkedList<BlockNode> getPath(BlockPos blockPos) throws NoPathException {
        LinkedList<BlockNode> path = calculatePath(BlockUtils.getPlayerLoc(), blockPos);
        if (path.isEmpty())
            throw new NoPathException();
        return path;
    }

    private LinkedList<BlockNode> calculatePath(BlockPos startingPos, final BlockPos endingBlock) {
        this.gridEnvironment.clear();
        this.checkedNodes.clear();
        this.openNodes.clear();
        if (BlockUtils.canSeeBlock(endingBlock) && BlockUtils.canReachBlock(endingBlock))
            return new LinkedList<BlockNode>() {

            };
        int currentGridX = 0;
        int currentGridY = (int)this.mc.thePlayer.posY;
        int currentGridZ = 0;
        instantiateAnyNode(currentGridX, currentGridY, currentGridZ, new Node(startingPos));
        Node startNode = (Node)this.gridEnvironment.get(currentGridX, currentGridY, currentGridZ);
        this.step = 0;
        this.openNodes.add(startNode);
        while (!this.openNodes.isEmpty()) {
            Node currentNode = this.openNodes.poll();
            if (currentNode.lastNode != null) {
                currentGridX = currentNode.blockPos.getX() - startNode.blockPos.getX();
                currentGridY = currentNode.blockPos.getY();
                currentGridZ = currentNode.blockPos.getZ() - startNode.blockPos.getZ();
            }
            this.checkedNodes.add(currentNode);
            this.step++;
            if (this.step > 3000)
                break;
            for (Moves move : Moves.values()) {
                instantiateNode(currentGridX + move.dx, currentGridY + move.dy, currentGridZ + move.dz, startNode);
                checkNode(move, (Node)this.gridEnvironment.get(currentGridX + move.dx, currentGridY + move.dy, currentGridZ + move.dz), currentNode, endingBlock);
            }
            if (currentNode.blockPos.equals(endingBlock))
                return trackBackPath(currentNode, startNode);
        }
        return new LinkedList<>();
    }

    private void checkNode(Moves move, Node searchNode, Node currentNode, BlockPos endingBlockPos) {
        if (this.checkedNodes.contains(searchNode) || BlockUtils.isPassable(searchNode.blockPos.down()))
            return;
        if (!searchNode.blockPos.equals(endingBlockPos)) {
            if (this.pathBehaviour.getForbiddenMiningBlocks() != null && ((
                    this.pathBehaviour.getForbiddenMiningBlocks().contains(BlockUtils.getBlock(searchNode.blockPos)) && !BlockUtils.getBlock(searchNode.blockPos).equals(Blocks.air)) || (this.pathBehaviour
                    .getForbiddenMiningBlocks().contains(BlockUtils.getBlock(searchNode.blockPos.up())) && !BlockUtils.getBlock(searchNode.blockPos.up()).equals(Blocks.air))))
                return;
            if (this.pathBehaviour.getAllowedMiningBlocks() != null && ((
                    !this.pathBehaviour.getAllowedMiningBlocks().contains(BlockUtils.getBlock(searchNode.blockPos)) && !BlockUtils.getBlock(searchNode.blockPos).equals(Blocks.air)) || (
                    !this.pathBehaviour.getAllowedMiningBlocks().contains(BlockUtils.getBlock(searchNode.blockPos.up())) && !BlockUtils.getBlock(searchNode.blockPos.up()).equals(Blocks.air))))
                return;
        }
        switch (move) {
            case DIAGONAL_NORTHEAST:
            case DIAGONAL_NORTHWEST:
            case DIAGONAL_SOUTHEAST:
            case DIAGONAL_SOUTHWEST:
                if (!BlockUtils.isPassable(new BlockPos(searchNode.blockPos.getX(), searchNode.blockPos.getY(), currentNode.blockPos.getZ())) || !BlockUtils.isPassable(new BlockPos(currentNode.blockPos.getX(), searchNode.blockPos.getY(), searchNode.blockPos.getZ())) ||
                        !BlockUtils.isPassable(new BlockPos(searchNode.blockPos.getX(), searchNode.blockPos.getY() + 1, currentNode.blockPos.getZ())) || !BlockUtils.isPassable(new BlockPos(currentNode.blockPos.getX(), searchNode.blockPos.getY() + 1, searchNode.blockPos.getZ())))
                    return;
                break;
            case DOWN:
                if (BlockUtils.isPassable(searchNode.blockPos))
                    return;
                break;
        }
        if (!this.openNodes.contains(searchNode)) {
            searchNode.lastNode = currentNode;
            calculateCost(move, searchNode, endingBlockPos);
            this.openNodes.add(searchNode);
        } else if (currentNode.gValue + move.cost < searchNode.gValue) {
            searchNode.lastNode = currentNode;
            calculateCost(move.cost, searchNode, endingBlockPos);
            this.openNodes.remove(searchNode);
            this.openNodes.add(searchNode);
        }
    }

    private void instantiateNode(int gridX, int gridY, int gridZ, Node startNode) {
        instantiateAnyNode(gridX, gridY, gridZ, new Node(startNode.blockPos.add(gridX, gridY - startNode.blockPos.getY(), gridZ)));
    }

    private void instantiateAnyNode(int gridX, int gridY, int gridZ, Node node) {
        if (this.gridEnvironment.get(gridX, gridY, gridZ) == null)
            this.gridEnvironment.set(gridX, gridY, gridZ, node);
    }

    private LinkedList<BlockNode> trackBackPath(Node goalNode, Node startNode) {
        LinkedList<BlockNode> blocksToMine = new LinkedList<>();
        Node formerNode = null;
        Node currentTrackNode = null;
        if (goalNode.lastNode != null && goalNode.lastNode.blockPos != null) {
            blocksToMine.add(new BlockNode(goalNode.blockPos, getBlockType(goalNode.blockPos)));
            if (goalNode.lastNode.blockPos.getY() > goalNode.blockPos.getY()) {
                if (!BlockUtils.isPassable(goalNode.blockPos.up()))
                    blocksToMine.add(new BlockNode(goalNode.blockPos.up(), getBlockType(goalNode.blockPos.up())));
                if (!BlockUtils.isPassable(goalNode.blockPos.up(2)) && getBlockType(goalNode.blockPos.up(2)) == BlockType.WALK)
                    blocksToMine.add(new BlockNode(goalNode.blockPos.up(2), getBlockType(goalNode.blockPos.up(2))));
            } else if (goalNode.lastNode.blockPos.getY() == goalNode.blockPos.getY() && (
                    AngleUtils.shouldLookAtCenter(goalNode.blockPos) || getBlockType(goalNode.blockPos.up()) == BlockType.WALK)) {
                blocksToMine.add(new BlockNode(goalNode.blockPos.up(), getBlockType(goalNode.blockPos.up())));
            }
            formerNode = goalNode;
            currentTrackNode = goalNode.lastNode;
        }
        if (currentTrackNode != null && currentTrackNode.lastNode != null) {
            do {
                if (currentTrackNode.lastNode.blockPos.getY() > currentTrackNode.blockPos.getY()) {
                    blocksToMine.add(new BlockNode(currentTrackNode.blockPos, getBlockType(currentTrackNode.blockPos)));
                    if (!BlockUtils.isPassable(currentTrackNode.blockPos.up()))
                        blocksToMine.add(new BlockNode(currentTrackNode.blockPos.up(), getBlockType(currentTrackNode.blockPos.up())));
                    if (!BlockUtils.isPassable(currentTrackNode.blockPos.up(2)))
                        blocksToMine.add(new BlockNode(currentTrackNode.blockPos.up(2), getBlockType(currentTrackNode.blockPos.up(2))));
                } else if (formerNode.blockPos.getY() > currentTrackNode.blockPos.getY()) {
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
            if (((BlockNode)blocksToMine.getLast()).getBlockPos().getY() >= (int)mc.thePlayer.posY + 1 && ((BlockNode)blocksToMine.get(blocksToMine.size() - 2)).getBlockPos().getY() >= (int)mc.thePlayer.posY + 1)
                blocksToMine.add(new BlockNode(BlockUtils.getPlayerLoc().up(2), getBlockType(BlockUtils.getPlayerLoc().up(2))));
        }
        Logger.playerLog("Block count : " + blocksToMine.size());
        return blocksToMine;
    }

    private void calculateCost(Moves move, Node node, BlockPos endingBlock) {
        node.hValue = getHeuristic(node.blockPos, endingBlock);
        if (node.lastNode != null) {
            node.lastNode.gValue += move.cost;
        } else {
            node.gValue = 1.0D;
        }
        node.fValue = node.gValue + node.hValue;
    }

    private void calculateCost(double gCost, Node node, BlockPos endingBlock) {
        node.hValue = getHeuristic(node.blockPos, endingBlock);
        if (node.lastNode != null) {
            node.lastNode.gValue += gCost;
        } else {
            node.gValue = 1.0D;
        }
        node.fValue = node.gValue + node.hValue;
    }

    private double calculatePathCost(List<BlockNode> nodes) {
        double cost = 0.0D;
        if (nodes.size() <= 2) {
            for (BlockNode node : nodes)
                cost += (Math.abs(AngleUtils.getActualRotationYaw(mc.thePlayer.rotationYaw) - AngleUtils.getRequiredYaw(node.getBlockPos())) + Math.abs(mc.thePlayer.rotationPitch - AngleUtils.getRequiredPitch(node.getBlockPos()))) / 540.0d;
        } else {
            for (BlockNode node : nodes)
                cost += (node.getBlockType() == BlockType.WALK) ? 1.5D : 1.0D;
        }
        return cost;
    }

    BlockType getBlockType(BlockPos blockToSearch) {
        return BlockUtils.isPassable(blockToSearch) ? BlockType.WALK : BlockType.MINE;
    }

    double getHeuristic(BlockPos start, BlockPos goal) {
        return MathUtils.getBlockDistanceBetweenTwoBlock(start, goal);
    }
}
