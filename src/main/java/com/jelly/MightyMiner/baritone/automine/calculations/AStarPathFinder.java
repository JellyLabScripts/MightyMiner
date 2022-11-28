package com.jelly.MightyMiner.baritone.automine.calculations;

import com.jelly.MightyMiner.baritone.automine.calculations.exceptions.ChunkLoadException;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.baritone.automine.movement.Moves;
import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathFinderBehaviour;
import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathMode;
import com.jelly.MightyMiner.baritone.automine.calculations.exceptions.NoBlockException;
import com.jelly.MightyMiner.baritone.automine.calculations.exceptions.NoPathException;
import com.jelly.MightyMiner.baritone.automine.structures.*;
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
import net.minecraft.util.BlockPos;

public class AStarPathFinder {
    Minecraft mc = Minecraft.getMinecraft();

    ArrayList<BlockPos> blackListedPos = new ArrayList<>();

    AStarCalculator calculator = new AStarCalculator();

    PathFinderBehaviour pathFinderBehaviour;
    PathMode mode = PathMode.MINE;
    BlockPos lastTarget;

    public AStarPathFinder(PathFinderBehaviour options) {

        pathFinderBehaviour = options;
    }

    public void addToBlackList(BlockPos... blackListedPos) {
        this.blackListedPos.addAll(Arrays.asList(blackListedPos));
    }

    public void removeFromBlackList(BlockPos blockPos) {
        blackListedPos.remove(blockPos);
    }


    public Path getPath(PathMode mode, boolean withPreference, Block... blockType) throws NoBlockException, NoPathException {
        initialize(mode);

        long pastTime = System.currentTimeMillis();;

        LinkedList<LinkedList<BlockNode>> possiblePaths = new LinkedList<>();
        List<BlockPos> foundBlocks = new ArrayList<>();

        if (withPreference) { // loop for EACH block type
            for (Block block : blockType) {
                foundBlocks = BlockUtils.findBlock(pathFinderBehaviour.getSearchRadius() * 2, blackListedPos, pathFinderBehaviour.getMinY(), pathFinderBehaviour.getMaxY(), block);
                possiblePaths = getPossiblePaths(foundBlocks);

                if (!possiblePaths.isEmpty()) {
                    Logger.playerLog("Total time | Time per path : " + (System.currentTimeMillis() - pastTime) + " ms | " + ((System.currentTimeMillis() - pastTime) * 1.0D / possiblePaths.size()) + " ms");
                    possiblePaths.sort(Comparator.comparingDouble(this::calculatePathCost));
                    setLastTarget(possiblePaths.getFirst());
                    return new Path(possiblePaths.getFirst(), mode);
                }
            }

        } else { // 1 loop for ALL block types
            foundBlocks = BlockUtils.findBlock(pathFinderBehaviour.getSearchRadius() * 2, blackListedPos, pathFinderBehaviour.getMinY(), pathFinderBehaviour.getMaxY(), blockType);
            possiblePaths = getPossiblePaths(foundBlocks);
        }

        if(foundBlocks.isEmpty())
            throw new NoBlockException();

        if (possiblePaths.isEmpty())
            throw new NoPathException();

        Logger.playerLog("Total time | Time per path : " + (System.currentTimeMillis() - pastTime) + " ms | " + ((System.currentTimeMillis() - pastTime) * 1.0D / possiblePaths.size()) + " ms");
        possiblePaths.sort(Comparator.comparingDouble(this::calculatePathCost));
        setLastTarget(possiblePaths.getFirst());
        return new Path(possiblePaths.getFirst(), mode);
    }


    public Path getPath(PathMode mode, BlockPos blockPos) throws NoPathException { // from blockPos
        initialize(mode);


        LinkedList<BlockNode> path = calculator.calculatePath(BlockUtils.getPlayerLoc(), blockPos, pathFinderBehaviour, mode, 20000);

        if (path.isEmpty())
            throw new NoPathException();

        setLastTarget(path);

        return  path.pollLast().isFullPath()? new Path(path, mode) : new SemiPath(path, mode);
    }


    private void setLastTarget(LinkedList<BlockNode> blockList){
        removeFromBlackList(lastTarget);
        this.lastTarget = blockList.getFirst().getBlockPos();
    }

    private LinkedList<LinkedList<BlockNode>> getPossiblePaths(List<BlockPos> targetBlocks){

        LinkedList<LinkedList<BlockNode>> possiblePaths = new LinkedList<>();
        int limit = 3000;
        for (int i = 0; i < Math.min(targetBlocks.size(), 20); i++) {
            LinkedList<BlockNode> path = pathFinderBehaviour.isStaticMode() ? calculator.calculateStaticPath(targetBlocks.get(i)) : calculator.calculatePath(BlockUtils.getPlayerLoc(), targetBlocks.get(i), pathFinderBehaviour, mode, limit);

            if (!path.isEmpty()) {

                if(!pathFinderBehaviour.isStaticMode())
                    path.removeLast(); // remove last dummy blockNode, as this method call must be in chunk load limit

                possiblePaths.add(path);
                limit = calculator.getSteps();
            }
        }
        return possiblePaths;

    }

    private void initialize(PathMode mode){
        this.addToBlackList(lastTarget);

        Logger.log("-------Blacklisted blocks------");
        blackListedPos.forEach(System.out::println);
        Logger.log("-------------");

        this.mode = mode;
    }




    private double calculatePathCost(List<BlockNode> nodes) {
        double cost = 0.0D;
        if (nodes.size() <= 2) {
            for (BlockNode node : nodes)
                cost += (Math.abs(AngleUtils.getActualRotationYaw(mc.thePlayer.rotationYaw) - AngleUtils.getRequiredYaw(node.getBlockPos())) + Math.abs(mc.thePlayer.rotationPitch - AngleUtils.getRequiredPitch(node.getBlockPos()))) / 540.0d;
        } else {
            for (BlockNode node : nodes)
                cost += (node.getBlockType() == BlockType.WALK) ? 1D : 1.5D;
        }
        return cost;
    }




}
