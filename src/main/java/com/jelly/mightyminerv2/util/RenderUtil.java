package com.jelly.mightyminerv2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.List;

public final class RenderUtil {
  private static final Minecraft mc = Minecraft.getMinecraft();

  private RenderUtil() {
    // Private constructor to prevent instantiation
  }

  public static void drawPoint(Vec3 vec, Color color) {
    drawBox(new AxisAlignedBB(
            vec.xCoord - 0.05, vec.yCoord - 0.05, vec.zCoord - 0.05,
            vec.xCoord + 0.05, vec.yCoord + 0.05, vec.zCoord + 0.05
    ), color);
  }

  public static void drawLine(Vec3 start, Vec3 end, Color color) {
    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableDepth();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.color(
            color.getRed() / 255F,
            color.getGreen() / 255F,
            color.getBlue() / 255F,
            color.getAlpha() / 255F
    );

    GL11.glLineWidth(1.5f);
    GL11.glBegin(GL11.GL_LINES);
    GL11.glVertex3d(
            start.xCoord - mc.getRenderManager().viewerPosX,
            start.yCoord - mc.getRenderManager().viewerPosY,
            start.zCoord - mc.getRenderManager().viewerPosZ
    );
    GL11.glVertex3d(
            end.xCoord - mc.getRenderManager().viewerPosX,
            end.yCoord - mc.getRenderManager().viewerPosY,
            end.zCoord - mc.getRenderManager().viewerPosZ
    );
    GL11.glEnd();

    GlStateManager.enableTexture2D();
    GlStateManager.enableDepth();
    GlStateManager.disableBlend();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
  }

  public static void outlineBlock(BlockPos pos, Color color) {
    RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(
            pos.getX(), pos.getY(), pos.getZ(),
            pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
    ).expand(0.002, 0.002, 0.002).offset(
            -mc.getRenderManager().viewerPosX,
            -mc.getRenderManager().viewerPosY,
            -mc.getRenderManager().viewerPosZ
    ));
  }

  public static void drawBlock(BlockPos blockPos, Color color) {
    drawBox(new AxisAlignedBB(
            blockPos.getX(),
            blockPos.getY(),
            blockPos.getZ(),
            blockPos.getX() + 1,
            blockPos.getY() + 1,
            blockPos.getZ() + 1
    ), color);
  }

  public static void drawBox(AxisAlignedBB aabb, Color color) {
    aabb = aabb.offset(
            -mc.getRenderManager().viewerPosX,
            -mc.getRenderManager().viewerPosY,
            -mc.getRenderManager().viewerPosZ
    );

    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.disableDepth();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.disableTexture2D();
    GlStateManager.depthMask(false);

    final Tessellator tessellator = Tessellator.getInstance();
    final WorldRenderer worldRenderer = tessellator.getWorldRenderer();

    GlStateManager.color(
            color.getRed() / 255F,
            color.getGreen() / 255F,
            color.getBlue() / 255F,
            color.getAlpha() / 255F
    );

    // Draw box
    worldRenderer.begin(7, DefaultVertexFormats.POSITION);
    worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
    worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();

    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();

    worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();

    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();

    worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
    worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
    worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();

    worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
    worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
    worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
    tessellator.draw();

    GlStateManager.depthMask(true);
    GlStateManager.enableTexture2D();
    GlStateManager.enableDepth();
    GlStateManager.disableBlend();
    GlStateManager.popMatrix();
  }

  public static void drawMultiLineText(List<String> lines, RenderGameOverlayEvent event, Color color, float scale) {
    ScaledResolution scaledResolution = event.resolution;
    int scaledWidth = scaledResolution.getScaledWidth();
    FontRenderer fontRenderer = mc.fontRendererObj;

    GlStateManager.pushMatrix();
    GlStateManager.translate((float) (scaledWidth / 2), 50, 0.0F);
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.scale(scale, scale, scale);

    int yOffset = 0;
    for (String line : lines) {
      fontRenderer.drawString(
              line,
              -fontRenderer.getStringWidth(line) / 2f,
              yOffset,
              color.getRGB(),
              true
      );
      yOffset += fontRenderer.FONT_HEIGHT * 2;
    }

    GlStateManager.popMatrix();
  }

  public static void drawCenterTopText(String text, RenderGameOverlayEvent event, Color color, float scale) {
    ScaledResolution scaledResolution = event.resolution;
    int scaledWidth = scaledResolution.getScaledWidth();
    FontRenderer fontRenderer = mc.fontRendererObj;

    GlStateManager.pushMatrix();
    GlStateManager.translate((float) (scaledWidth / 2), 50, 0.0F);
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.scale(scale, scale, scale);

    fontRenderer.drawString(
            text,
            -fontRenderer.getStringWidth(text) / 2f,
            0,
            color.getRGB(),
            true
    );

    GlStateManager.popMatrix();
  }

  public static void drawText(String str, double x, double y, double z, float scale) {
    FontRenderer fontRenderer = mc.fontRendererObj;

    double renderPosX = x - mc.getRenderManager().viewerPosX;
    double renderPosY = y - mc.getRenderManager().viewerPosY;
    double renderPosZ = z - mc.getRenderManager().viewerPosZ;

    double distance = Math.sqrt(renderPosX * renderPosX + renderPosY * renderPosY + renderPosZ * renderPosZ);
    double multiplier = Math.max(distance / 150f, 0.1f);
    scale *= 0.45f * multiplier;

    float xMultiplier = mc.gameSettings.thirdPersonView == 2 ? -1 : 1;

    GlStateManager.pushMatrix();
    GlStateManager.translate(renderPosX, renderPosY, renderPosZ);
    GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0, 1, 0);
    GlStateManager.rotate(mc.getRenderManager().playerViewX * xMultiplier, 1, 0, 0);
    GlStateManager.scale(-scale, -scale, scale);
    GlStateManager.disableLighting();
    GlStateManager.depthMask(false);
    GlStateManager.disableDepth();
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

    int textWidth = fontRenderer.getStringWidth(str);

    GlStateManager.disableTexture2D();
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldRenderer = tessellator.getWorldRenderer();
    GlStateManager.color(0, 0, 0, 0.5f);
    worldRenderer.begin(7, DefaultVertexFormats.POSITION);
    worldRenderer.pos(-textWidth / 2 - 1, -1, 0).endVertex();
    worldRenderer.pos(-textWidth / 2 - 1, 8, 0).endVertex();
    worldRenderer.pos(textWidth / 2 + 1, 8, 0).endVertex();
    worldRenderer.pos(textWidth / 2 + 1, -1, 0).endVertex();
    tessellator.draw();
    GlStateManager.enableTexture2D();

    fontRenderer.drawString(str, -textWidth / 2, 0, -1);

    GlStateManager.enableDepth();
    GlStateManager.depthMask(true);
    GlStateManager.enableLighting();
    GlStateManager.disableBlend();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.popMatrix();
  }

  public static void drawTracer(Vec3 to, Color color) {
    Vec3 from = new Vec3(
            mc.thePlayer.posX,
            mc.thePlayer.posY + mc.thePlayer.getEyeHeight(),
            mc.thePlayer.posZ
    );
    drawLine(from, to, color);
  }
}