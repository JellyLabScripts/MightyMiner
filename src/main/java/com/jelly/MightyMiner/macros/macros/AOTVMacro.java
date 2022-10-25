package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.MineBehaviour;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;
import rosegoldaddons.utils.RenderUtils;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AOTVMacro extends Macro {
    AutoMineBaritone baritone;

    enum State{
        NONE,
        Teleporting,
        Mining
    }



    State currentState = State.NONE;

    BlockPos targetCoordinate;
    int targetCoordIndex;
    int rightClickCD;
    boolean rotationFlag;
    Rotation rotation = new Rotation();

    List<BlockPos> coords;




    @Override
    protected void onEnable() {
        baritone = new AutoMineBaritone(getAutoMineConfig());
        currentState = State.Mining;

        coords = MightyMiner.coordsConfig.getSelectedRoute().valueList();
  
        targetCoordIndex = -1;
        for(int i = 0; i < coords.size(); i++){
            if(BlockUtils.getPlayerLoc().down().equals(coords.get(i))){
                targetCoordIndex = i;
            }
        }

        if(targetCoordIndex == -1){
            LogUtils.addMessage("You must stand on one of the coordinates to start!");
            MacroHandler.disableScript();
            return;
        }
        targetCoordinate = coords.get(targetCoordIndex);
    }

    @Override
    public void onTick(TickEvent.Phase phase) {

        baritone.onTickEvent(phase);

        if(phase != TickEvent.Phase.START) return;

        if(targetCoordIndex == -1) return;

        switch(currentState) {
            case NONE:
                break;
            case Teleporting:

                if(rightClickCD >= 0)
                    rightClickCD--;

                KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);

                if(rotationFlag)
                    rotation.intLockAngle(AngleUtils.getRequiredYawCenter(targetCoordinate), AngleUtils.getRequiredPitchCenter(targetCoordinate),  500);


                if(rightClickCD == -1)
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, false);
                else if(!rotation.rotating && rightClickCD < 2) {
                    rotationFlag = false;
                    rotation.reset();
                    mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Void");
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, true);
                }


                break;
            case Mining:
                useMiningSpeedBoost();
                if(!baritone.isEnabled()) {
                    try {
                        baritone.mineForInSingleThread(Blocks.stained_glass, Blocks.stained_glass_pane);
                    } catch (Exception ignored) {
                        currentState = State.NONE;
                        baritone.disableBaritone();
                        if(targetCoordIndex < coords.size() - 1) {
                            targetCoordinate = coords.get(targetCoordIndex + 1);
                            targetCoordIndex++;
                        }
                        else {
                            targetCoordinate = coords.get(0);
                            targetCoordIndex = 0;
                        }
                    }
                }
        }
        updateState();
    }

    public void updateState(){
        switch(currentState){
            case NONE:
                currentState = State.Teleporting;
                rotationFlag = true;
                rightClickCD = 15;
                LogUtils.debugLog("Going to coordinates " + targetCoordinate.getX() + " " + targetCoordinate.getY() + " " + targetCoordinate.getZ());
                return;
            case Teleporting:
                if((BlockUtils.getPlayerLoc().down().equals(targetCoordinate))) {
                    currentState = State.Mining;
                    KeybindHandler.resetKeybindState();
                }
        }
    }
    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event) {
        baritone.onOverlayRenderEvent(event);
    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {
        baritone.onRenderEvent();
        if(rotation.rotating)
            rotation.update();

        if (MightyMiner.config.highlightRouteBlocks) {
            coords.forEach(coord -> {
                RenderUtils.drawBlockBox(coord, MightyMiner.config.routeBlocksColor, 3, event.partialTicks);
            });
        }

        if (MightyMiner.config.showRouteLines) {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);

            double x = mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * (double)event.partialTicks;
            double y = mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * (double)event.partialTicks;
            double z = mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * (double)event.partialTicks;
            Vec3 pos = new Vec3(x, y, z);
            GL11.glTranslated(-pos.xCoord, -pos.yCoord, -pos.zCoord);

            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            GL11.glBegin(GL11.GL_LINE_STRIP);
            GL11.glColor4f(MightyMiner.config.routeLineColor.getRed() / 255f, MightyMiner.config.routeLineColor.getGreen() / 255f, MightyMiner.config.routeLineColor.getBlue() / 255f, MightyMiner.config.routeLineColor.getAlpha() / 255f);

            for (int i = 0; i < coords.size() - 1; i++) {
                drawLineWithGL(coords.get(i), coords.get(i+1));
            }
            drawLineWithGL(coords.get(coords.size() - 1), coords.get(0));

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnd();
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }

    @Override
    protected void onDisable() {
        baritone.disableBaritone();
    }

    private void drawLineWithGL(BlockPos blockA, BlockPos blockB) {
        GL11.glVertex3d(blockA.getX() + 0.5,blockA.getY() + 0.5,blockA.getZ() + 0.5);
        GL11.glVertex3d(blockB.getX() + 0.5,blockB.getY() + 0.5,blockB.getZ() + 0.5);
    }

    private MineBehaviour getAutoMineConfig(){
        return new MineBehaviour(
                AutoMineType.STATIC,
                true,
                false,
                false,
                MightyMiner.config.aotvRotationTime,
                MightyMiner.config.aotvRestartTimeThreshold, //changed with config
                null,
                null,
                256,
                0

        );
    }
}


