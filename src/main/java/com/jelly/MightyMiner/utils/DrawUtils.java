package com.jelly.MightyMiner.utils;

import com.jelly.MightyMiner.MightyMiner;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;
import scala.Predef;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.sqrt;
import static net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox;

public class DrawUtils {
    private static final Minecraft minecraft = Minecraft.getMinecraft();
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

    public static void drawEntity(Entity entity, int width, Color color, float partialTicks) {
        final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        GL11.glBlendFunc(770, 771);
        HashMap<Integer, Boolean> glCapMap = new HashMap<>();
        glCapMap.put(3042, GL11.glGetBoolean(3042));
        GL11.glEnable(3042);

        glCapMap.put(3553, GL11.glGetBoolean(3553));
        GL11.glDisable(3553);
        glCapMap.put(2929, GL11.glGetBoolean(2929));
        GL11.glDisable(2929);

        GL11.glDepthMask(false);
        GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() != 255 ? color.getAlpha() / 255.0f : 26 / 255.0f);
        GL11.glLineWidth(width);
        glCapMap.put(2848, GL11.glGetBoolean(2848));
        GL11.glEnable(2848);
        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
                - renderManager.viewerPosX;
        final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
                - renderManager.viewerPosY;
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
                - renderManager.viewerPosZ;

        final AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                entityBox.minX - entity.posX + x - 0.5D,
                entityBox.minY - entity.posY + y,
                entityBox.minZ - entity.posZ + z - 0.5D,
                entityBox.maxX - entity.posX + x + 0.5D,
                entityBox.maxY - entity.posY + y + 0.5D,
                entityBox.maxZ - entity.posZ + z + 0.5D
        );
        drawSelectionBoundingBox(axisAlignedBB);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glDepthMask(true);
        for (Map.Entry<Integer, Boolean> set : glCapMap.entrySet()) {
            if (set.getValue()) {
                GL11.glEnable(set.getKey());
            } else {
                GL11.glDisable(set.getKey());
            }
        }
    }

    public static void drawLineWithGL(BlockPos blockA, BlockPos blockB) {
        GL11.glVertex3d(blockA.getX() + 0.5,blockA.getY() + 0.5,blockA.getZ() + 0.5);
        GL11.glVertex3d(blockB.getX() + 0.5,blockB.getY() + 0.5,blockB.getZ() + 0.5);
    }
    // from skytils (kt -> java)
    public static void drawWaypointText(String string, double X, double Y, double Z, float partialTicks) {
        GlStateManager.alphaFunc(516, 0.1f);
        GlStateManager.pushMatrix();
        Entity viewer = minecraft.getRenderViewEntity();
        double x = X - minecraft.getRenderManager().viewerPosX;
        double y = Y - minecraft.getRenderManager().viewerPosY - viewer.getEyeHeight();
        double z = Z - minecraft.getRenderManager().viewerPosZ;
        double distSq = x * x + y * y + z * z;
        double dist = sqrt(distSq);
        if (distSq > 144) {
            x *= 12 / dist;
            y *= 12 / dist;
            z *= 12 / dist;
        }
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0f, viewer.getEyeHeight(), 0f);
        drawTag(string);
        GlStateManager.rotate(-minecraft.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(minecraft.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        GlStateManager.translate(0f, -0.25f, 0f);
        GlStateManager.rotate(-minecraft.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(minecraft.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        drawTag(EnumChatFormatting.RED.toString() + dist + "blocks");
        GlStateManager.popMatrix();
        GlStateManager.disableLighting();
    }
    // from skytils (kt -> java)
    public static void drawTag(String string) {
        FontRenderer fontRenderer = minecraft.fontRendererObj;
        float FLOAT_1 = 0.02666667f;

        GlStateManager.pushMatrix();
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-minecraft.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(
                minecraft.getRenderManager().playerViewX,
                1.0f,
                0.0f,
                0.0f
        );
        GlStateManager.scale(-FLOAT_1, -FLOAT_1, FLOAT_1);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        int i = 0;
        int j = fontRenderer.getStringWidth(string) / 2;
        GlStateManager.disableTexture2D();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(-j - 1, -1 + i, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
        worldrenderer.pos(-j - 1, 8 + 1, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
        worldrenderer.pos(j + 1, 8 + 1, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
        worldrenderer.pos(j + 1, -1 + i, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        fontRenderer.drawString(string, -j, i, 553648127);
        GlStateManager.depthMask(true);
        fontRenderer.drawString(string, -j, i, -1);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    public static Triple<Double, Double, Double> viewerPosition(float partialTicks) {
        final Entity viewer = minecraft.getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;
        return Triple.of(viewerX, viewerY, viewerZ);
    }
    public static void drawFilledBox(AxisAlignedBB aabb, Color c, float alphaMultiplier) {
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        val tessellator = Tessellator.getInstance();
        val worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f * alphaMultiplier);

        //vertical
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        tessellator.draw();
        GlStateManager.color(
                c.getRed() / 255f * 0.8f,
                c.getGreen() / 255f * 0.8f,
                c.getBlue() / 255f * 0.8f,
                c.getAlpha() / 255f * alphaMultiplier
        );

        //x
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        tessellator.draw();
        GlStateManager.color(
                c.getRed() / 255f * 0.9f,
                c.getGreen() / 255f * 0.9f,
                c.getBlue() / 255f * 0.9f,
                c.getAlpha() / 255f * alphaMultiplier
        );
        //z
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
