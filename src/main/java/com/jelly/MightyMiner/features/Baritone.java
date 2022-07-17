package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.structures.GridEnvironment;
import com.jelly.MightyMiner.structures.Node;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import javax.lang.model.type.NullType;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Baritone {
    //Custom grid example

    //Xxxxxxxxxxxxxx |
    //xxxxxxxxxxxxxx |
    //xxxxxxKxxxxxxx | MaxZ
    //xxxxxxxxxxxxxx |
    //xxxxxxxxxxxxxx |
    //<------------->
    //     MaxX
    // Y is the same as world coordinate

    // program coverts world coordinates to custom grid first
    // custom grid starts takes X as [0, y, 0]
    // player starts on K initially [maxX/2, y, maxZ/2]
    GridEnvironment<Node> gridEnvironment = new GridEnvironment<>();

    Minecraft mc = Minecraft.getMinecraft();

    int maxX = 10000;
    int maxY = 256;
    int maxZ = 10000;

    int currentGridX;
    int currentGridY;
    int currentGridZ;
    //Node[][][] nodeGrid = new Node[maxX][maxY][maxZ];

    List<Node> checkedNodes = new ArrayList<>();
    List<Node> openNodes = new ArrayList<>();
    List<BlockPos> blockToWalk = new ArrayList<>();

    Node currentNode;
    Node startNode;

    BlockPos endingBlock;
    boolean completed = false;
    public boolean walking = false;
    public int step = 0;

    Rotation rotation = new Rotation();

    public void clearBlocksToWalk(){
        checkedNodes.clear();
        openNodes.clear();
        blockToWalk.clear();
        gridEnvironment.clear();
        step = 0;
        completed = false;
    }

    public void walkTo(BlockPos endingBlock){
        clearBlocksToWalk();
        calculateBlocksToWalk(endingBlock);
    }

    private void searchBlocks(BlockPos endingBlock) throws Exception {
        Node goalNode = new Node(endingBlock);
        while(!completed){
            step++;
            currentNode.checked = true;
            checkedNodes.add(currentNode);
            openNodes.remove(currentNode);

           // System.out.println(openNodes.size() + "open nodes");
            if(currentGridX > 0) {
                instantiateNode(currentGridX - 1, currentGridY, currentGridZ);
                openNode(gridEnvironment.get(currentGridX - 1, currentGridY, currentGridZ));
            }
            if(currentGridX < maxX) {
                instantiateNode(currentGridX + 1, currentGridY, currentGridZ);
                openNode(gridEnvironment.get(currentGridX + 1, currentGridY, currentGridZ));
            }
            if(currentGridZ > 0) {
                instantiateNode(currentGridX, currentGridY, currentGridZ - 1);
                openNode(gridEnvironment.get(currentGridX , currentGridY, currentGridZ - 1));
            }
            if(currentGridZ < maxZ) {
                instantiateNode(currentGridX, currentGridY, currentGridZ + 1);
                openNode(gridEnvironment.get(currentGridX , currentGridY, currentGridZ + 1));
            }
            if(currentGridY > 0) {
                instantiateNode(currentGridX, currentGridY - 1, currentGridZ);
                openNode(gridEnvironment.get(currentGridX , currentGridY - 1 , currentGridZ));
            }
            if(currentGridY < maxY) {
                instantiateNode(currentGridX, currentGridY + 1, currentGridZ);
                openNode(gridEnvironment.get(currentGridX , currentGridY + 1, currentGridZ));
            }


            int bestIndex = 0;
            double minFcost = 9999;
            for (int i = 0; i < openNodes.size(); i++){
                if(openNodes.get(i).fValue < minFcost) {
                    bestIndex = i;
                    minFcost = openNodes.get(i).fValue;
                } else if(openNodes.get(i).fValue == minFcost){
                    if(openNodes.get(i).hValue < openNodes.get(bestIndex).hValue){
                        bestIndex = i;
                    }
                }
            }
            int tempX, tempY, tempZ;
            tempX = currentGridX; tempY = currentGridY; tempZ = currentGridZ;
            currentGridX += openNodes.get(bestIndex).blockPos.getX() - gridEnvironment.get(tempX, tempY, tempZ).blockPos.getX();
            currentGridY += openNodes.get(bestIndex).blockPos.getY() - gridEnvironment.get(tempX, tempY, tempZ).blockPos.getY();
            currentGridZ += openNodes.get(bestIndex).blockPos.getZ() - gridEnvironment.get(tempX, tempY, tempZ).blockPos.getZ();

            currentNode = openNodes.get(bestIndex);

            if(goalNode.blockPos.equals(currentNode.blockPos)){
                mc.thePlayer.addChatMessage(new ChatComponentText("Block count : " + openNodes.size()));
                completed = true;
            }
        }
        trackBackPath(currentNode);
        for(BlockPos blockPos : blockToWalk){
            BlockRenderer.renderMap.put(blockPos, Color.ORANGE);
        }

    }
    private void openNode(Node searchNode){
        if(!searchNode.checked && !searchNode.opened
                && BlockUtils.isWalkable(mc.theWorld.getBlockState(searchNode.blockPos).getBlock())
                && BlockUtils.isWalkable(mc.theWorld.getBlockState(searchNode.blockPos.up()).getBlock())
                && (!BlockUtils.isWalkable(mc.theWorld.getBlockState(searchNode.blockPos.down(1)).getBlock()) || !BlockUtils.isWalkable(mc.theWorld.getBlockState(searchNode.blockPos.down(2)).getBlock()))){
            searchNode.opened = true;
            calculateCost(searchNode);
            searchNode.lastNode = currentNode;
            openNodes.add(searchNode);
        }
    }




    private void calculateBlocksToWalk(BlockPos endingBlock){
        BlockRenderer.renderMap.clear();
        BlockRenderer.renderMap.put(endingBlock, Color.RED);

        try {
            currentGridX = maxX / 2;
            currentGridY = (int) mc.thePlayer.posY;
            currentGridZ = maxZ / 2;
            instantiateNode(currentGridX, currentGridY, currentGridZ, new Node(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)));
            currentNode = gridEnvironment.get(currentGridX, currentGridY, currentGridZ);
            startNode = currentNode;
            this.endingBlock = endingBlock;
            searchBlocks(endingBlock);

        }catch (Exception e){
            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Can't find path" ));
            e.printStackTrace();
        }


    }
    private void instantiateNode(int gridX, int gridY, int gridZ){
        instantiateNode(gridX, gridY, gridZ, new Node(startNode.blockPos.add(gridX - maxX/2, gridY - startNode.blockPos.getY(), gridZ -  maxZ/2)));
    }
    private void instantiateNode(int gridX, int gridY, int gridZ, Node node){
        if(gridEnvironment.get(gridX, gridY, gridZ) == null)
            gridEnvironment.set(gridX, gridY, gridZ, node);
    }

    private void calculateCost(Node node){
        node.hValue = MathUtils.getDistanceBetweenTwoBlock(node.blockPos, endingBlock);
        node.gValue = MathUtils.getDistanceBetweenTwoBlock(node.blockPos, startNode.blockPos);
        node.fValue = node.gValue + node.hValue;
    }
    private void trackBackPath(Node startingGoalNode){
        while(!startNode.equals(startingGoalNode) && startingGoalNode.lastNode != null){
            blockToWalk.add(startingGoalNode.lastNode.blockPos);
            startingGoalNode = startingGoalNode.lastNode;
        }

    }

}
