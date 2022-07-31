package com.jelly.MightyMiner.baritone.baritones;

import com.jelly.MightyMiner.baritone.Baritone;
import com.jelly.MightyMiner.baritone.structures.BlockNode;
import com.jelly.MightyMiner.baritone.structures.BlockType;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.baritone.structures.GridEnvironment;
import com.jelly.MightyMiner.baritone.structures.Node;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.collections4.map.LinkedMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AutoMineBaritone extends Baritone{

    GridEnvironment<Node> gridEnvironment = new GridEnvironment<>();

    int maxX = 10000;
    int maxY = 256;
    int maxZ = 10000;

    List<Node> checkedNodes = new ArrayList<>();
    List<Node> openNodes = new ArrayList<>();


    LinkedList<BlockNode> blocksToMine = new LinkedList<>();
    LinkedList<BlockNode> minedBlocks = new LinkedList<>();
    int step = 0;


    boolean inAction = false;
    Rotation rotation = new Rotation();

    int deltaJumpTick = 0;

    List<Block> forbiddenMiningBlocks;
    List<Block> allowedMiningBlocks;
    boolean shiftWhenMine;

    enum PlayerState {
        WALKING,
        MINING,
        NONE
    }
    PlayerState currentState;

    public AutoMineBaritone(){}

    public AutoMineBaritone(List<Block> forbiddenMiningBlocks){
        this.forbiddenMiningBlocks = forbiddenMiningBlocks;
    }

    public AutoMineBaritone(List<Block> forbiddenMiningBlocks, List<Block> allowedMiningBlocks){
        this.forbiddenMiningBlocks = forbiddenMiningBlocks;
        this.allowedMiningBlocks = allowedMiningBlocks;
    }

    public AutoMineBaritone(List<Block> forbiddenMiningBlocks, List<Block> allowedMiningBlocks, boolean shiftWhenMine){
        this.forbiddenMiningBlocks = forbiddenMiningBlocks;
        this.allowedMiningBlocks = allowedMiningBlocks;
        this.shiftWhenMine = shiftWhenMine;
    }



    public void clearBlocksToWalk(){
        openNodes.clear();
        checkedNodes.clear();
        blocksToMine.clear();
        gridEnvironment.clear();
        BlockRenderer.renderMap.clear();
        minedBlocks.clear();
        step = 0;
        currentState = PlayerState.NONE;

    }


    @Override
    protected void onEnable(BlockPos destinationBlock) throws Exception{
        mc.gameSettings.gammaSetting = 100;
        clearBlocksToWalk();
        KeybindHandler.resetKeybindState();
        mc.thePlayer.addChatMessage(new ChatComponentText("Starting automine"));


        BlockRenderer.renderMap.put(destinationBlock, Color.RED);
        blocksToMine = calculateBlocksToMine(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), destinationBlock);
        for(BlockNode blockNode : blocksToMine){
            BlockRenderer.renderMap.put(blockNode.getBlockPos(), Color.ORANGE);
        }
        BlockRenderer.renderMap.put(destinationBlock, Color.RED);
        inAction = true;
        currentState = PlayerState.NONE;
    }

    @Override
    public void onDisable() {
        inAction = false;
        KeybindHandler.resetKeybindState();
        clearBlocksToWalk();
    }


    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event){
        if(event.type == RenderGameOverlayEvent.ElementType.TEXT){
            if(blocksToMine != null){
                if(!blocksToMine.isEmpty()){
                    for(int i = 0; i < blocksToMine.size(); i++){
                        mc.fontRendererObj.drawString(blocksToMine.get(i).getBlockPos().toString() + " " + blocksToMine.get(i).getBlockType().toString() , 5, 5 + 10 * i, -1);
                    }
                }
            }
            if(currentState != null)
                mc.fontRendererObj.drawString(currentState.toString(), 300, 5, -1);
        }
    }



    @Override
    public void onTickEvent(TickEvent.Phase phase){

        if(phase != TickEvent.Phase.START)
            return;

        if(!inAction)
            return;

        if(blocksToMine.isEmpty())
            return;

        if ( (blocksToMine.getLast().getBlockType() == BlockType.MINE && BlockUtils.isPassable(blocksToMine.getLast().getBlockPos()))
        || (blocksToMine.getLast().getBlockType() == BlockType.WALK &&
                (BlockUtils.onTheSameXZ(blocksToMine.getLast().getBlockPos(), BlockUtils.getPlayerLoc()) || !BlockUtils.fitsPlayer(blocksToMine.getLast().getBlockPos().down())) )
        ){

            minedBlocks.add(blocksToMine.getLast());
            BlockRenderer.renderMap.remove(blocksToMine.getLast().getBlockPos());
            blocksToMine.removeLast();
        }
        if(blocksToMine.isEmpty() || BlockUtils.isPassable(blocksToMine.getFirst().getBlockPos())){
            mc.thePlayer.addChatMessage(new ChatComponentText("Finished baritone"));
            inAction = false;
            KeybindHandler.resetKeybindState();
            disableBaritone();
            return;
        }



        updateState();

        BlockPos lastMinedBlockPos = minedBlocks.isEmpty() ? null : minedBlocks.getLast().getBlockPos();
        BlockPos targetMineBlock = blocksToMine.getLast().getBlockPos();

        if(currentState == PlayerState.WALKING){


            KeybindHandler.updateKeys(
                    lastMinedBlockPos != null || BlockUtils.isPassable(targetMineBlock),
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    deltaJumpTick > 0);


            if(lastMinedBlockPos != null)
                rotation.intLockAngle(AngleUtils.getRequiredYaw(lastMinedBlockPos), mc.thePlayer.rotationPitch, 350);
            else if(BlockUtils.isPassable(targetMineBlock))
                rotation.intLockAngle(AngleUtils.getRequiredYaw(targetMineBlock), mc.thePlayer.rotationPitch, 350);


            if ( (lastMinedBlockPos != null
                    && (lastMinedBlockPos.getY() == (int) mc.thePlayer.posY + 2 || lastMinedBlockPos.getY() == (int) mc.thePlayer.posY + 1)
                    && !(BlockUtils.onTheSameXZ(lastMinedBlockPos, BlockUtils.getPlayerLoc()))
                    && (BlockUtils.fitsPlayer(lastMinedBlockPos.down()) || BlockUtils.fitsPlayer(lastMinedBlockPos.down(2)))  )
                    ||
                (lastMinedBlockPos == null && BlockUtils.isPassable(targetMineBlock) && targetMineBlock.getY() == (int) mc.thePlayer.posY + 1 )
            ) {
                deltaJumpTick = 3;
            }
        }

        if(currentState == PlayerState.MINING){


            KeybindHandler.updateKeys(
                    false,
                    false,
                    false,
                    false,
                    mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null &&
                            mc.objectMouseOver.getBlockPos().equals(targetMineBlock),
                    false,
                    shiftWhenMine,
                    false);


            if(mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && !mc.objectMouseOver.getBlockPos().equals(targetMineBlock)) {
                if (!BlockUtils.isPassable(targetMineBlock) && !rotation.rotating)
                    rotation.intLockAngle(AngleUtils.getRequiredYaw(targetMineBlock), AngleUtils.getRequiredPitch(targetMineBlock), 350);
            }


        }

        if (deltaJumpTick > 0)
            deltaJumpTick--;



    }

    @Override
    public void onRenderEvent(){
        if(rotation.rotating)
            rotation.update();

    }
    private void updateState(){
        if(blocksToMine.isEmpty())
            return;

        if(minedBlocks.isEmpty()){
            currentState =  blocksToMine.getLast().getBlockType().equals(BlockType.MINE) ? PlayerState.MINING : PlayerState.WALKING;
            return;
        }

        if(currentState == PlayerState.WALKING){
            if(minedBlocks.getLast().getBlockType() == BlockType.WALK){
                if(blocksToMine.getLast().getBlockType() == BlockType.MINE)
                    currentState = PlayerState.MINING;

            } else if(minedBlocks.getLast().getBlockType() == BlockType.MINE) {
                if (BlockUtils.onTheSameXZ(minedBlocks.getLast().getBlockPos(), BlockUtils.getPlayerLoc()))
                    currentState = PlayerState.MINING;
            }

        } else if(currentState == PlayerState.MINING){
            System.out.println((BlockUtils.fitsPlayer(minedBlocks.getLast().getBlockPos().down()) + " " + minedBlocks.getLast().getBlockPos().down(2) + " " + !BlockUtils.onTheSameXZ(minedBlocks.getLast().getBlockPos(), BlockUtils.getPlayerLoc())));

            if(blocksToMine.getLast().getBlockType() == BlockType.WALK){
                currentState = PlayerState.WALKING;
            } else if (blocksToMine.getLast().getBlockType() == BlockType.MINE) {
                if( (BlockUtils.fitsPlayer(minedBlocks.getLast().getBlockPos().down()) || BlockUtils.fitsPlayer(minedBlocks.getLast().getBlockPos().down(2)))
                        && !BlockUtils.onTheSameXZ(minedBlocks.getLast().getBlockPos(), BlockUtils.getPlayerLoc())) {
                    currentState = PlayerState.WALKING;
                }
            }


        } else if(currentState == PlayerState.NONE){
            currentState = blocksToMine.getLast().getBlockType().equals(BlockType.MINE) ? PlayerState.MINING : PlayerState.WALKING;
        }
    }

    private LinkedList<BlockNode> calculateBlocksToMine(BlockPos startingPos, BlockPos endingBlock) throws Exception {

        if(BlockUtils.canSeeBlock(endingBlock) && BlockUtils.canReachBlock(endingBlock)){
            return new LinkedList<BlockNode>(){{add(new BlockNode(endingBlock, getBlockType(endingBlock)));}};
        }

        boolean completedPathfind = false;
        Node startNode;
        Node currentNode;
        Node goalNode = new Node(endingBlock);

        int currentGridX = maxX / 2;
        int currentGridY = (int) mc.thePlayer.posY;
        int currentGridZ = maxZ / 2;

        instantiateAnyNode(currentGridX, currentGridY, currentGridZ, new Node(startingPos));
        currentNode = gridEnvironment.get(currentGridX, currentGridY, currentGridZ);
        startNode = currentNode;


        while (!completedPathfind) {
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
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX,  currentGridY, currentGridZ + 1), currentNode, endingBlock);
            }

            if (currentGridY > 0 && currentGridX > 0) {
                instantiateNode(currentGridX - 1, currentGridY - 1, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX - 1, currentGridY - 1, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridY > 0 && currentGridX < maxX) {
                instantiateNode(currentGridX + 1, currentGridY - 1, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX + 1, currentGridY - 1, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridY > 0 && currentGridZ > 0) {
                instantiateNode(currentGridX, currentGridY - 1, currentGridZ - 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY - 1, currentGridZ - 1), currentNode, endingBlock);
            }
            if (currentGridY > 0 && currentGridZ < maxZ) {
                instantiateNode(currentGridX, currentGridY - 1, currentGridZ + 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY - 1, currentGridZ + 1), currentNode, endingBlock);
            }

            if (currentGridY < maxY && currentGridX > 0) {
                instantiateNode(currentGridX - 1, currentGridY + 1, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX - 1, currentGridY + 1, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridY < maxY && currentGridX < maxX) {
                instantiateNode(currentGridX + 1, currentGridY + 1, currentGridZ, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX + 1, currentGridY + 1, currentGridZ), currentNode, endingBlock);
            }
            if (currentGridY < maxY && currentGridZ > 0) {
                instantiateNode(currentGridX, currentGridY + 1, currentGridZ - 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY + 1, currentGridZ - 1), currentNode, endingBlock);
            }
            if (currentGridY < maxY && currentGridZ < maxZ) {
                instantiateNode(currentGridX, currentGridY + 1, currentGridZ + 1, startNode);
                openNodeAndCalculateCost(gridEnvironment.get(currentGridX, currentGridY + 1, currentGridZ + 1), currentNode, endingBlock);
            }



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
        }
        return trackBackPath(currentNode, startNode);



    }
    private void openNodeAndCalculateCost(Node searchNode, Node currentNode, BlockPos endingBlockPos){
        if ( (!searchNode.checked
                && !searchNode.opened
                && BlockUtils.canWalkOn(searchNode.blockPos.down()))){

            if(currentNode.lastNode != null){
                if(Math.abs(currentNode.lastNode.blockPos.getY() - searchNode.blockPos.getY()) > 1 &&
                        BlockUtils.onTheSameXZ(currentNode.lastNode.blockPos, searchNode.blockPos))
                    return;
            }
            if(!searchNode.blockPos.equals(endingBlockPos)) {
                if (forbiddenMiningBlocks != null && forbiddenMiningBlocks.contains(BlockUtils.getBlock(searchNode.blockPos))  && !BlockUtils.getBlock(searchNode.blockPos).equals(Blocks.air))
                    return;

                if (allowedMiningBlocks != null && !allowedMiningBlocks.contains(BlockUtils.getBlock(searchNode.blockPos)) && !BlockUtils.getBlock(searchNode.blockPos).equals(Blocks.air))
                    return;
            }

            searchNode.opened = true;
            searchNode.lastNode = currentNode;
            openNodes.add(searchNode);
            calculateCost(searchNode, endingBlockPos);
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

        if(node.lastNode != null) {
            if(node.lastNode.blockPos.getY() != node.blockPos.getY()) {
                node.gValue = node.lastNode.gValue + 2;
            } else {
                node.gValue = node.lastNode.gValue + 1;
            }
        }
        else
            node.gValue = 1f;
        node.fValue = node.gValue + node.hValue;
    }

    private LinkedList<BlockNode> trackBackPath(Node goalNode, Node startNode){
        LinkedList<BlockNode> blocksToMine = new LinkedList<>();

        Node formerNode = null;


        if(goalNode.blockPos != null && goalNode.lastNode != null && goalNode.lastNode.blockPos != null) {
            if (goalNode.lastNode.blockPos.getY() > goalNode.blockPos.getY()) {
                blocksToMine.add(new BlockNode(goalNode.blockPos, getBlockType(goalNode.blockPos)));
                if (!BlockUtils.isPassable(goalNode.blockPos.up())) {
                    blocksToMine.add(new BlockNode(goalNode.blockPos.up(), getBlockType(goalNode.blockPos.up())));
                }
            } else {
                blocksToMine.add(new BlockNode(goalNode.blockPos, getBlockType(goalNode.blockPos)));
                if(AngleUtils.shouldLookAtCenter(goalNode.blockPos) && !AngleUtils.shouldLookAtCenter(goalNode.blockPos.up())){
                    blocksToMine.add(new BlockNode(goalNode.blockPos.up(), getBlockType(goalNode.blockPos.up())));
                }
            }
            formerNode = goalNode;
            goalNode = goalNode.lastNode;
        }


        if (goalNode.lastNode != null && formerNode != null) {

            do {
                if (formerNode.blockPos.getY() > goalNode.blockPos.getY()) {

                    if (!BlockUtils.isPassable(goalNode.blockPos.up(2))) {
                        blocksToMine.add(new BlockNode(goalNode.blockPos.up(2), getBlockType(goalNode.blockPos.up(2))));
                    }
                    if (!BlockUtils.isPassable(goalNode.blockPos.up())) {
                        blocksToMine.add(new BlockNode(goalNode.blockPos.up(), getBlockType(goalNode.blockPos.up())));
                    }

                    blocksToMine.add(new BlockNode(goalNode.blockPos, getBlockType(goalNode.blockPos)));
                } else if (goalNode.lastNode.blockPos.getY() > goalNode.blockPos.getY()) {
                    blocksToMine.add(new BlockNode(goalNode.blockPos, getBlockType(goalNode.blockPos)));

                    if (!BlockUtils.isPassable(goalNode.blockPos.up())) {
                        blocksToMine.add(new BlockNode(goalNode.blockPos.up(), getBlockType(goalNode.blockPos.up())));
                    }
                    if (!BlockUtils.isPassable(goalNode.blockPos.up(2))) {
                        blocksToMine.add(new BlockNode(goalNode.blockPos.up(2), getBlockType(goalNode.blockPos.up(2))));
                    }

                } else {

                    blocksToMine.add(new BlockNode(goalNode.blockPos, getBlockType(goalNode.blockPos)));

                    if (!BlockUtils.isPassable(goalNode.blockPos.up()))
                        blocksToMine.add(new BlockNode(goalNode.blockPos.up(), getBlockType(goalNode.blockPos.up())));
                }
                formerNode = goalNode;
                goalNode = goalNode.lastNode;
            } while(!startNode.equals(goalNode) && goalNode.lastNode.blockPos != null);

            //LogUtils.debugLog(blocksToMine.getLast().getBlockPos().toString());
            if(blocksToMine.getLast().getBlockPos().getY() == (int)mc.thePlayer.posY + 1 || blocksToMine.getLast().getBlockPos().getY() == (int)mc.thePlayer.posY + 2){
                blocksToMine.add(new BlockNode(BlockUtils.getPlayerLoc().up(2), getBlockType(BlockUtils.getPlayerLoc().up(2))));
            }

        }



        mc.thePlayer.addChatMessage(new ChatComponentText("Block count : " + blocksToMine.size()));
        return blocksToMine;
    }

    public static BlockType getBlockType (BlockPos blockToSearch) {
        return BlockUtils.isPassable(blockToSearch) ? BlockType.WALK : BlockType.MINE;
    }

}
