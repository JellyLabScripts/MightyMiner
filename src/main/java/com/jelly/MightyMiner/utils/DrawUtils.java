package com.jelly.MightyMiner.utils;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.jelly.MightyMiner.MightyMiner;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

import static java.lang.Math.sqrt;

public class DrawUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void drawCoordsRoute(List<BlockPos> coords, RenderWorldLastEvent event) {
        if (coords.size() <= 1) return;
        final Entity render = mc.getRenderViewEntity();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer bufferBuilder = tessellator.getWorldRenderer();
        final double realX = render.lastTickPosX + (render.posX - render.lastTickPosX) * event.partialTicks;
        final double realY = render.lastTickPosY + (render.posY - render.lastTickPosY) * event.partialTicks;
        final double realZ = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * event.partialTicks;
        GlStateManager.pushMatrix();
        GlStateManager.translate(-realX, -realY, -realZ);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GL11.glDisable(3553);
        GL11.glLineWidth(3f);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1f, 1f, 1f, 1f);
        bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < coords.size() - 1; i++) {
            BlockPos pos = coords.get(i);
            BlockPos nextPos = coords.get(i + 1);
            drawLine(bufferBuilder, pos, nextPos);
        }

        BlockPos pos = coords.get(0);
        BlockPos nextPos = coords.get(coords.size() - 1);
        drawLine(bufferBuilder, pos, nextPos);

        tessellator.draw();
        GlStateManager.translate(realX, realY, realZ);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    private static void drawLine(WorldRenderer bufferBuilder, BlockPos pos, BlockPos nextPos) {
        bufferBuilder.pos(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f).color(MightyMiner.config.aotvRouteLineColor.getRed() / 255f, MightyMiner.config.aotvRouteLineColor.getGreen() / 255f, MightyMiner.config.aotvRouteLineColor.getBlue() / 255f, MightyMiner.config.aotvRouteLineColor.getAlpha() / 255f).endVertex();
        bufferBuilder.pos(nextPos.getX() + 0.5f, nextPos.getY() + 0.5f, nextPos.getZ() + 0.5f).color(MightyMiner.config.aotvRouteLineColor.getRed() / 255f, MightyMiner.config.aotvRouteLineColor.getGreen() / 255f, MightyMiner.config.aotvRouteLineColor.getBlue() / 255f, MightyMiner.config.aotvRouteLineColor.getAlpha() / 255f).endVertex();
    }

    public static void drawEntity(Entity entity, OneColor color, int width, float partialTicks) {
        drawEntity(entity, color.toJavaColor(), width, partialTicks);
    }
    public static void drawEntity(Entity entity, Color color, int width, float partialTicks) {
        RenderManager renderManager = mc.getRenderManager();

        double viewerPosX = renderManager.viewerPosX;
        double viewerPosY = renderManager.viewerPosY;
        double viewerPosZ = renderManager.viewerPosZ;

        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - viewerPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - viewerPosZ;

        final AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        final AxisAlignedBB aabb = new AxisAlignedBB(
                entityBox.minX - entity.posX + x - 0.05D,
                entityBox.minY - entity.posY + y,
                entityBox.minZ - entity.posZ + z - 0.05D,
                entityBox.maxX - entity.posX + x + 0.05D,
                entityBox.maxY - entity.posY + y,
                entityBox.maxZ - entity.posZ + z + 0.05D
        );

        drawFilledBoundingBox(aabb, color, 0.7f, width);
    }

    public static void drawBlockBox(BlockPos blockPos, OneColor color, float lineWidth) {
        drawBlockBox(blockPos, color.toJavaColor(), lineWidth);
    }
    public static void drawBlockBox(BlockPos blockPos, Color color, float lineWidth) {
        if (blockPos == null) return;
        IBlockState blockState = mc.theWorld.getBlockState(blockPos);

        if (blockState == null) return;
        Block block = blockState.getBlock();
        block.setBlockBoundsBasedOnState(mc.theWorld, blockPos);
        double viewerPosX = mc.getRenderManager().viewerPosX;
        double viewerPosY = mc.getRenderManager().viewerPosY;
        double viewerPosZ = mc.getRenderManager().viewerPosZ;
        drawFilledBoundingBox(block.getSelectedBoundingBox(mc.theWorld, blockPos).expand(0.002D, 0.002D, 0.002D).offset(-viewerPosX, -viewerPosY, -viewerPosZ), color, 0.7f, lineWidth);
    }

    public static void drawBlockBox(AxisAlignedBB bb, OneColor color, float lineWidth) {
        drawBlockBox(bb, color.toJavaColor(), lineWidth);
    }

    public static void drawBlockBox(AxisAlignedBB bb, Color color, float lineWidth) {
        double viewerPosX = mc.getRenderManager().viewerPosX;
        double viewerPosY = mc.getRenderManager().viewerPosY;
        double viewerPosZ = mc.getRenderManager().viewerPosZ;
        drawFilledBoundingBox(bb.expand(0.002D, 0.002D, 0.002D).offset(-viewerPosX, -viewerPosY, -viewerPosZ), color, 0.7f, lineWidth);
    }

    public static void drawMiniBlockBox(Vec3 vec, OneColor color, float lineWidth) {
        drawMiniBlockBox(vec, color.toJavaColor(), lineWidth);
    }
    public static void drawMiniBlockBox(Vec3 vec, Color color, float lineWidth) {

        double viewerPosX = mc.getRenderManager().viewerPosX;
        double viewerPosY = mc.getRenderManager().viewerPosY;
        double viewerPosZ = mc.getRenderManager().viewerPosZ;

        double x = vec.xCoord - viewerPosX;
        double y = vec.yCoord - viewerPosY;
        double z = vec.zCoord - viewerPosZ;

        AxisAlignedBB aabb = new AxisAlignedBB(
                x - 0.05,
                y - 0.05,
                z - 0.05,
                x + 0.05,
                y + 0.05,
                z + 0.05
        );

        drawFilledBoundingBox(aabb, color, 0.7f, lineWidth);
    }

    public static void drawFilledBoundingBox(AxisAlignedBB aabb, OneColor color, float opacity, float lineWidth) {
        drawFilledBoundingBox(aabb, color.toJavaColor(), opacity, lineWidth);
    }
    public static void drawFilledBoundingBox(AxisAlignedBB aabb, Color color, float opacity, float lineWidth) {
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        float a = color.getAlpha() / 255.0F;
        float r = color.getRed() / 255.0F;
        float g = color.getGreen() / 255.0F;
        float b = color.getBlue() / 255.0F;

        GlStateManager.color(r, g, b, a * opacity);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        tessellator.draw();
        GlStateManager.color(r, g, b, a * opacity);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        tessellator.draw();
        GlStateManager.color(r, g, b, a * opacity);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        tessellator.draw();
        GlStateManager.color(r, g, b, a);
        GL11.glLineWidth(lineWidth);
        RenderGlobal.drawSelectionBoundingBox(aabb);
        GL11.glLineWidth(1.0f);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // from skytils (kt -> java)
    public static void drawWaypointText(String string, double X, double Y, double Z, float partialTicks) {
        GlStateManager.alphaFunc(516, 0.1f);
        GlStateManager.pushMatrix();
        Entity viewer = mc.getRenderViewEntity();
        double x = X - mc.getRenderManager().viewerPosX;
        double y = Y - mc.getRenderManager().viewerPosY - viewer.getEyeHeight();
        double z = Z - mc.getRenderManager().viewerPosZ;
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
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        GlStateManager.translate(0f, -0.25f, 0f);
        GlStateManager.rotate(-mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        drawTag(EnumChatFormatting.RED.toString() + ((int) dist) + " blocks");
        GlStateManager.popMatrix();
        GlStateManager.disableLighting();
    }
    // from skytils (kt -> java)
    public static void drawTag(String string) {
        FontRenderer fontRenderer = mc.fontRendererObj;
        float FLOAT_1 = 0.02666667f;

        GlStateManager.pushMatrix();
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(
                mc.getRenderManager().playerViewX,
                1.0f,
                0.0f,
                0.0f
        );
        GlStateManager.scale(-FLOAT_1, -FLOAT_1, FLOAT_1);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
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
        GlStateManager.enableDepth();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    public static Triple<Double, Double, Double> viewerPosition(float partialTicks) {
        final Entity viewer = mc.getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;
        return Triple.of(viewerX, viewerY, viewerZ);
    }

    public static void drawText(String str, double X, double Y, double Z) {
        drawText(str, X, Y, Z, false);
    }

    public static void drawText(String str, double X, double Y, double Z, boolean showDistance) {
        drawText(str, X, Y, Z, false, 1.0f);
    }

    public static void drawText(String str, double X, double Y, double Z, boolean showDistance, float lScale) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        double renderPosX = X - Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = Y - Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = Z - Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        double distance = Math.sqrt(renderPosX * renderPosX + renderPosY * renderPosY + renderPosZ * renderPosZ);
        double multiplier = Math.max(distance / 150f, 0.1f);
        lScale *= 0.45f * multiplier;

        float xMultiplier = Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? -1 : 1;

        GlStateManager.pushMatrix();
        GlStateManager.translate(renderPosX, renderPosY, renderPosZ);
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        GlStateManager.rotate(-renderManager.playerViewY, 0, 1, 0);
        GlStateManager.rotate(renderManager.playerViewX * xMultiplier, 1, 0, 0);
        GlStateManager.scale(-lScale, -lScale, lScale);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        int textWidth = fontRenderer.getStringWidth(StringUtils.stripControlCodes((str)));

        float j = textWidth / 2f;
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.color(0, 0, 0, 0.5f);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(-j - 1, -1, 0).endVertex();
        worldrenderer.pos(-j - 1, 8, 0).endVertex();
        worldrenderer.pos(j + 1, 8, 0).endVertex();
        worldrenderer.pos(j + 1, -1, 0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();

        fontRenderer.drawString(str, -textWidth / 2, 0, -1);

        if (showDistance) {
            textWidth = fontRenderer.getStringWidth(StringUtils.stripControlCodes((int) distance + " blocks"));
            fontRenderer.drawString((int) distance + " blocks", -textWidth / 2, 10, -1);
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
