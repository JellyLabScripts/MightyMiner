package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.render.BlockRenderer;
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


    Minecraft mc = Minecraft.getMinecraft();

    int maxX = 1000;
    int maxY = 256;
    int maxZ = 1000;

    int currentGridX;
    int currentGridY;
    int currentGridZ;
    Node[][][] nodeGrid = new Node[maxX][maxY][maxZ];

    List<Node> checkedNodes = new ArrayList<>();
    List<Node> openNodes = new ArrayList<>();
    List<BlockPos> blockToWalk = new ArrayList<>();

    Node currentNode;
    Node startNode;

    BlockPos endingBlock;
    boolean completed = false;
    public boolean walking = false;

    Rotation rotation = new Rotation();

    public void clearBlocksToWalk(){
        checkedNodes.clear();
        openNodes.clear();
        blockToWalk.clear();
        completed = false;
    }

    public void walkTo(BlockPos endingBlock){
        clearBlocksToWalk();
        this.endingBlock = endingBlock;
        walking = true;
        new Thread(() -> {
            try {
                calculateBlocksToWalk(endingBlock);
                System.out.println("hi");
                rotation.easeTo((float) (Math.atan2(-(mc.thePlayer.posX - blockToWalk.get(blockToWalk.size() - 1).getX() - 0.5), mc.thePlayer.posZ - blockToWalk.get(blockToWalk.size() - 1).getZ() - 0.5) * 180 / Math.PI), 0,1);
                Thread.sleep(1000);
                while (Math.floor(mc.thePlayer.posX) != endingBlock.getX() || Math.floor(mc.thePlayer.posY) != endingBlock.getY() || Math.floor(mc.thePlayer.posZ) != endingBlock.getZ()) {
                    Thread.sleep(1);
                    System.out.println(rotation.rotating);
                    if (rotation.rotating) {
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                        continue;
                    }

                    rotation.lockAngle((float) (Math.atan2(-(mc.thePlayer.posX - blockToWalk.get(blockToWalk.size() - 1).getX() - 0.5), mc.thePlayer.posZ - blockToWalk.get(blockToWalk.size() - 1).getZ() - 0.5) * 180 / Math.PI), 0);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
                }
                walking = false;
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
            }catch (Exception ignored){}
        }).start();

    }

    private void searchBlocks(BlockPos endingBlock){
        Node goalNode = new Node(endingBlock);
        while(!completed){
            currentNode.checked = true;
            checkedNodes.add(currentNode);
            openNodes.remove(currentNode);

           // System.out.println(openNodes.size() + "open nodes");
            if(currentGridX > 0) {
                instantiateNode(currentGridX - 1, currentGridY, currentGridZ);
                openNode(nodeGrid[currentGridX - 1][currentGridY][currentGridZ]);
            }
            if(currentGridX < maxX) {
                instantiateNode(currentGridX + 1, currentGridY, currentGridZ);
                openNode(nodeGrid[currentGridX + 1][currentGridY][currentGridZ]);
            }
            if(currentGridZ > 0) {
                instantiateNode(currentGridX, currentGridY, currentGridZ - 1);
                openNode(nodeGrid[currentGridX][currentGridY][currentGridZ - 1]);
            }
            if(currentGridZ < maxZ) {
                instantiateNode(currentGridX, currentGridY, currentGridZ + 1);
                openNode(nodeGrid[currentGridX][currentGridY][currentGridZ + 1]);
            }
            if(currentGridY > 0) {
                instantiateNode(currentGridX, currentGridY - 1, currentGridZ);
                openNode(nodeGrid[currentGridX][currentGridY - 1][currentGridZ]);
            }
            if(currentGridY < maxY) {
                instantiateNode(currentGridX, currentGridY + 1, currentGridZ);
                openNode(nodeGrid[currentGridX][currentGridY + 1][currentGridZ]);
            }


            int bestIndex = 0;
            double minFcost = 9999;
            for (int i = 0; i < openNodes.size(); i++){
                if(openNodes.get(i).fValue < minFcost) {
                    bestIndex = i;
                    minFcost = openNodes.get(i).fValue;
                } else if(openNodes.get(i).fValue == minFcost){
                    System.out.println("Same f cost");
                    if(openNodes.get(i).hValue < openNodes.get(bestIndex).hValue){
                        bestIndex = i;
                    }
                }
            }
            int tempX, tempY, tempZ;
            tempX = currentGridX; tempY = currentGridY; tempZ = currentGridZ;
            currentGridX += openNodes.get(bestIndex).blockPos.getX() - nodeGrid[tempX][tempY][tempZ].blockPos.getX();
            currentGridY += openNodes.get(bestIndex).blockPos.getY() - nodeGrid[tempX][tempY][tempZ].blockPos.getY();
            currentGridZ += openNodes.get(bestIndex).blockPos.getZ() - nodeGrid[tempX][tempY][tempZ].blockPos.getZ();

            currentNode = openNodes.get(bestIndex);
           // System.out.println(currentNode.blockPos + " current node");

            if(goalNode.blockPos.equals(currentNode.blockPos)){
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
            nodeGrid = new Node[maxX][maxY][maxZ];
            currentGridX = maxX / 2;
            currentGridY = (int) mc.thePlayer.posY;
            currentGridZ = maxZ / 2;
            nodeGrid[currentGridX][currentGridY][currentGridZ] = new Node(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
            currentNode = nodeGrid[currentGridX][currentGridY][currentGridZ];
            startNode = currentNode;
            searchBlocks(endingBlock);

        }catch (NullPointerException nullPointerException){
            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Can't find path" ));
        }


    }
    private void instantiateNode(int gridX, int gridY, int gridZ){
        if(nodeGrid[gridX][gridY][gridZ] == null)
        nodeGrid[gridX][gridY][gridZ] = new Node(startNode.blockPos.add(gridX - maxX/2, gridY - startNode.blockPos.getY(), gridZ -  maxZ/2));

        if(nodeGrid[gridX][gridY][gridZ].blockPos.equals(currentNode.blockPos))
        System.out.println(nodeGrid[gridX][gridY][gridZ].blockPos + "BUGGED");
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
