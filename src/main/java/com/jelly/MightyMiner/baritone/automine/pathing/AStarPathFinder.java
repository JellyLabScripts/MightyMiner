package com.jelly.MightyMiner.baritone.automine.pathing;

import com.jelly.MightyMiner.baritone.automine.movement.Moves;
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
import org.apache.commons.collections4.map.LinkedMap;

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

    ArrayList<BlockPos> blackListedPos = new ArrayList<>();



    public AStarPathFinder(PathBehaviour options){
        this.pathBehaviour = options;
    }

    public void addToBlackList(BlockPos... blackListedPos){

        this.blackListedPos.addAll(Arrays.asList(blackListedPos));
    }
    public void removeFromBlackList(BlockPos blockPos){
        this.blackListedPos.remove(blockPos);
    }

    public LinkedList<BlockNode> getPathWithPreference(Block... blockType) throws NoBlockException, NoPathException {

        LinkedList<LinkedList<BlockNode>> possiblePaths = new LinkedList<>();
        if(BlockUtils.findBlock(pathBehaviour.getSearchRadius() * 2, blackListedPos, pathBehaviour.getMinY(), pathBehaviour.getMaxY(), blockType).isEmpty())
            throw new NoBlockException();
        for(Block block : blockType) {
            List<BlockPos> foundBlocks = BlockUtils.findBlock(pathBehaviour.getSearchRadius() * 2, blackListedPos, pathBehaviour.getMinY(), pathBehaviour.getMaxY(), block);
            for (int i = 0; i < (Math.min(foundBlocks.size(), 20)); i++) {
                LinkedList<BlockNode> path = calculatePath(BlockUtils.getPlayerLoc(), foundBlocks.get(i));
                if (!path.isEmpty()) {
                    if (pathBehaviour.isStaticMode() && path.size() > 1)
                        continue;
                    possiblePaths.add(path);
                }
            }
            if(!possiblePaths.isEmpty()) {
                possiblePaths.sort(Comparator.comparingDouble(this::calculatePathCost));
                return possiblePaths.getFirst();
            }

        }
        throw new NoPathException();

    }

    public LinkedList<BlockNode> getPath(Block... blockType) throws NoBlockException, NoPathException {

        for(Block block : blockType){
            Logger.playerLog(block.toString());
        }
        List<BlockPos> foundBlocks = BlockUtils.findBlock(pathBehaviour.getSearchRadius() * 2, blackListedPos, pathBehaviour.getMinY(), pathBehaviour.getMaxY(), blockType);
        Logger.playerLog("Found blocks : " + foundBlocks.size());

        long pastTime = System.currentTimeMillis();

        if(foundBlocks.isEmpty())
            throw new NoBlockException();

        LinkedList<LinkedList<BlockNode>> possiblePaths = new LinkedList<>();
        for(int i = 0; i < (Math.min(foundBlocks.size(), 20)); i++){
            LinkedList<BlockNode> path = calculatePath(BlockUtils.getPlayerLoc(), foundBlocks.get(i));
            if(!path.isEmpty()){
                if(pathBehaviour.isStaticMode() && path.size() > 1)
                    continue;

                possiblePaths.add(path);
               // if(possiblePaths.getLast().size() == 1)
               //     return possiblePaths.getLast();
            }
        }

        if(possiblePaths.isEmpty())
            throw new NoPathException();

        Logger.playerLog("Total time | Time per path : " + (System.currentTimeMillis() - pastTime) + " ms | " + (System.currentTimeMillis() - pastTime) * 1.0d / possiblePaths.size() + " ms");

        possiblePaths.sort(Comparator.comparingDouble(this::calculatePathCost));
        return possiblePaths.getFirst();
    }

    public LinkedList<BlockNode> getPath(BlockPos blockPos) throws NoPathException {
        LinkedList<BlockNode> path = calculatePath(BlockUtils.getPlayerLoc(), blockPos);
        if(path.isEmpty())
            throw new NoPathException();

        return path;
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

            for(Moves move : Moves.values()){
                instantiateNode(currentGridX + move.dx, currentGridY + move.dy, currentGridZ + move.dz, startNode);
                checkNode(move, gridEnvironment.get(currentGridX + move.dx, currentGridY + move.dy, currentGridZ + move.dz), currentNode, endingBlock);
            }



            if(currentNode.blockPos.equals(endingBlock)) {
                return trackBackPath(currentNode, startNode);
            }


        }
        return new LinkedList<>();
    }
    private void checkNode(Moves move, Node searchNode, Node currentNode, BlockPos endingBlockPos){

        if(checkedNodes.contains(searchNode) || BlockUtils.isPassable(searchNode.blockPos.down()))
            return;

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

        switch (move){
            /*case DIAGONAL_NORTHEAST: case DIAGONAL_NORTHWEST: case DIAGONAL_SOUTHEAST: case DIAGONAL_SOUTHWEST:
                if(!BlockUtils.isPassable(new BlockPos(searchNode.blockPos.getX(), searchNode.blockPos.getY(), currentNode.blockPos.getZ())) || !BlockUtils.isPassable(new BlockPos(currentNode.blockPos.getX(), searchNode.blockPos.getY(), searchNode.blockPos.getZ())) ||
                        !BlockUtils.isPassable(new BlockPos(searchNode.blockPos.getX(), searchNode.blockPos.getY() + 1, currentNode.blockPos.getZ())) || !BlockUtils.isPassable(new BlockPos(currentNode.blockPos.getX(), searchNode.blockPos.getY() + 1, searchNode.blockPos.getZ())))
                    return;
                break;*/
            case DOWN:
                if(BlockUtils.isPassable(searchNode.blockPos))
                    return;
        }

        BlockRenderer.renderMap.put(searchNode.blockPos, Color.CYAN);

        if(!openNodes.contains(searchNode)){
            calculateCost(searchNode, endingBlockPos);
            searchNode.lastNode = currentNode;
            // searchNode.lastNode.move = move; //problematic
            System.out.println(move);
            openNodes.add(searchNode);
        } else {
            if(currentNode.gValue + move.cost < searchNode.gValue){
                searchNode.lastNode = currentNode;
                calculateCost(searchNode, endingBlockPos);
                //update open nodes
                openNodes.remove(searchNode);
                openNodes.add(searchNode);
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

    /*private LinkedList<BlockNode> trackBackPath(Node goalNode, Node startNode){
        LinkedList<BlockNode> blocksToMine = new LinkedList<>();

        Node formerNode = goalNode;
        Node currentTrackNode = goalNode;

        if (currentTrackNode != null) {
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
    }*/
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
            node.gValue = node.lastNode.gValue + node.lastNode.move.cost;
        else
            node.gValue = 1;
        node.fValue = node.gValue + node.hValue;
    }
    private double calculatePathCost(List<BlockNode> nodes){
        double cost = 0;
        if(nodes.size() <= 2){
            for (BlockNode node : nodes) {
                cost += (Math.abs(AngleUtils.getActualRotationYaw(mc.thePlayer.rotationYaw) - AngleUtils.getRequiredYaw(node.getBlockPos())) + Math.abs(mc.thePlayer.rotationPitch - AngleUtils.getRequiredPitch(node.getBlockPos()))) / 540.0d;
            }
        } else {
            for (BlockNode node : nodes) {
                cost += (node.getBlockType() == BlockType.WALK) ? 1.5d : 1d; //avoid open areas
            }
        }
        return cost;
    }

    BlockType getBlockType (BlockPos blockToSearch) {
        return BlockUtils.isPassable(blockToSearch) ? BlockType.WALK : BlockType.MINE;
    }

    double getHeuristic(BlockPos start, BlockPos goal){
        return MathUtils.getBlockDistanceBetweenTwoBlock(start, goal);
    }
}
