package com.jelly.MightyMiner.utils;

import com.jelly.MightyMiner.MightyMiner;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class DrawUtils {
    public static void drawCoordsRoute(List<BlockPos> coords, RenderWorldLastEvent event) {
        if (coords.size() > 1) {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glLineWidth(5f);

            Minecraft mc = Minecraft.getMinecraft();

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

    public static void drawLineWithGL(BlockPos blockA, BlockPos blockB) {
        GL11.glVertex3d(blockA.getX() + 0.5,blockA.getY() + 0.5,blockA.getZ() + 0.5);
        GL11.glVertex3d(blockB.getX() + 0.5,blockB.getY() + 0.5,blockB.getZ() + 0.5);
    }
}
