package com.jelly.MightyMiner.baritone.autowalk.pathing;

import com.jelly.MightyMiner.baritone.autowalk.movement.Moves;
import com.jelly.MightyMiner.baritone.autowalk.pathing.config.PathBehaviour;
import com.jelly.MightyMiner.baritone.logging.Logger;
import com.jelly.MightyMiner.baritone.structures.GridEnvironment;
import com.jelly.MightyMiner.baritone.autowalk.pathing.structures.Node;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.client.registry.ClientRegistry;
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


    public AStarPathFinder(PathBehaviour options){
        this.pathBehaviour = options;
    }

    public LinkedMap<BlockPos, Moves> getPath(BlockPos targetBlockPos) {
        return calculatePath(BlockUtils.getPlayerLoc(), targetBlockPos);
    }


    private LinkedMap<BlockPos, Moves> calculatePath(BlockPos startingPos, BlockPos endingBlock) {

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
            assert currentNode != null;
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
        return new LinkedMap<>();
    }
    private void checkNode(Moves move, Node searchNode, Node currentNode, BlockPos endingBlockPos){

        if(checkedNodes.contains(searchNode) || !BlockUtils.fitsPlayer(searchNode.blockPos.down()))
            return;

        switch (move){
            case DIAGONAL_NORTHEAST: case DIAGONAL_NORTHWEST: case DIAGONAL_SOUTHEAST: case DIAGONAL_SOUTHWEST:
                if(!BlockUtils.isPassable(new BlockPos(searchNode.blockPos.getX(), searchNode.blockPos.getY(), currentNode.blockPos.getZ())) || !BlockUtils.isPassable(new BlockPos(currentNode.blockPos.getX(), searchNode.blockPos.getY(), searchNode.blockPos.getZ())))
                    return;
                break;
            case ASCEND_EAST: case ASCEND_NORTH: case ASCEND_SOUTH: case ASCEND_WEST:
                if(!BlockUtils.isPassable(currentNode.blockPos.up(2)))
                    return;
                break;
            case DESCEND_EAST: case DESCEND_NORTH: case DESCEND_SOUTH: case DESCEND_WEST:
                if(!BlockUtils.isPassable(searchNode.blockPos.up(2)))
                    return;
                break;
        }

        if(!openNodes.contains(searchNode)){
            calculateCost(searchNode, endingBlockPos);
            searchNode.lastNode = currentNode;
           // searchNode.lastNode.move = move; //problematic
            System.out.println(move);
            openNodes.add(searchNode);
        } else {
            if(currentNode.gValue + move.cost < searchNode.gValue){
                System.out.println("better path found");
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

    private LinkedMap<BlockPos, Moves> trackBackPath(Node goalNode, Node startNode){
        LinkedMap<BlockPos, Moves> blocksToWalk = new LinkedMap<>();
        if(goalNode.lastNode!= null)
            blocksToWalk.put(goalNode.blockPos, goalNode.lastNode.move);
        while(!startNode.equals(goalNode) && goalNode.lastNode != null){
            blocksToWalk.put(goalNode.lastNode.blockPos,
                    Moves.getMove(goalNode.blockPos.getX() - goalNode.lastNode.blockPos.getX(), goalNode.blockPos.getY() - goalNode.lastNode.blockPos.getY(), goalNode.blockPos.getZ() - goalNode.lastNode.blockPos.getZ()));
            //NEED to optimize
            goalNode = goalNode.lastNode;

        }

        mc.thePlayer.addChatMessage(new ChatComponentText("Block count : " + blocksToWalk.size()));
        return blocksToWalk;
    }

    private void calculateCost(Node node, BlockPos endingBlock){
        node.hValue = getHeuristic(node.blockPos, endingBlock);
        if(node.lastNode != null)
            node.gValue = node.lastNode.gValue + node.lastNode.move.cost;
        else
            node.gValue = 1;
        node.fValue = node.gValue + node.hValue;
    }

    double getHeuristic(BlockPos start, BlockPos goal){
        return MathUtils.getDistanceBetweenTwoBlock(start, goal);
    }

}
