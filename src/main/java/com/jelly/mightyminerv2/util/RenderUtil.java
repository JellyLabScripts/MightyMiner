package com.jelly.mightyminerv2.util;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

public class RenderUtil {

  private static final Minecraft mc = Minecraft.getMinecraft();

  public static void drawPoint(Vec3 vec, Color color) {
    drawBox(new AxisAlignedBB(vec.xCoord - 0.05, vec.yCoord - 0.05, vec.zCoord - 0.05, vec.xCoord + 0.05, vec.yCoord + 0.05, vec.zCoord + 0.05), color);
  }

  private static void startGL() {
    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.disableDepth();
    GlStateManager.disableLighting();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.disableTexture2D();
  }

  private static void endGL() {
    GlStateManager.enableTexture2D();
    GlStateManager.enableDepth();
    GlStateManager.disableBlend();
    GlStateManager.resetColor();
    GlStateManager.popMatrix();
  }

  private static void drawLine(double mix, double miy, double miz, double max, double may, double maz, float red, float green, float blue, float alpha, float scale) {
    GL11.glLineWidth(scale);
    GL11.glColor4f(red, green, blue, alpha);
    GL11.glBegin(GL11.GL_LINES);
    GL11.glVertex3d(mix, miy, miz);
    GL11.glVertex3d(max, may, maz);
    GL11.glEnd();
    GL11.glLineWidth(1.0f);
  }

  private static void outline(double mix, double miy, double miz, double max, double may, double maz, float red, float green, float blue, float alpha, float scale) {
    GL11.glLineWidth(scale);
    GL11.glColor4f(red, green, blue, alpha);
    GL11.glBegin(GL11.GL_LINES);

    GL11.glVertex3d(mix, miy, miz); GL11.glVertex3d(max, miy, miz);
    GL11.glVertex3d(max, miy, miz); GL11.glVertex3d(max, miy, maz);
    GL11.glVertex3d(max, miy, maz); GL11.glVertex3d(mix, miy, maz);
    GL11.glVertex3d(mix, miy, maz); GL11.glVertex3d(mix, miy, miz);

    GL11.glVertex3d(mix, may, miz); GL11.glVertex3d(max, may, miz);
    GL11.glVertex3d(max, may, miz); GL11.glVertex3d(max, may, maz);
    GL11.glVertex3d(max, may, maz); GL11.glVertex3d(mix, may, maz);
    GL11.glVertex3d(mix, may, maz); GL11.glVertex3d(mix, may, miz);

    GL11.glVertex3d(mix, miy, miz); GL11.glVertex3d(mix, may, miz);
    GL11.glVertex3d(max, miy, miz); GL11.glVertex3d(max, may, miz);
    GL11.glVertex3d(max, miy, maz); GL11.glVertex3d(max, may, maz);
    GL11.glVertex3d(mix, miy, maz); GL11.glVertex3d(mix, may, maz);

    GL11.glEnd();
    GL11.glLineWidth(1.0f);
  }

  public static void drawLine(Vec3 start, Vec3 end, Color color) {
    RenderManager renderManager = mc.getRenderManager();
    double vx = renderManager.viewerPosX;
    double vy = renderManager.viewerPosY;
    double vz = renderManager.viewerPosZ;
    startGL();
    drawLine(start.xCoord - vx, start.yCoord - vy, start.zCoord - vz, end.xCoord - vx, end.yCoord - vy, end.zCoord - vz, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0f, color.getAlpha() / 255.0f, 1.5f);
    endGL();
  }

  public static void outlineBlock(BlockPos pos, Color color){
    outlineBox(new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1), color);
  }

  public static void outlineBox(AxisAlignedBB bb, Color color) {
    RenderManager renderManager = mc.getRenderManager();
    bb = bb.offset(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);
    startGL();
    outline(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0f, color.getAlpha() / 255.0f, 2f);
    endGL();
  }

  public static void drawBlock(BlockPos blockPos, Color color) {
    double x = blockPos.getX();
    double y = blockPos.getY();
    double z = blockPos.getZ();
    drawBox(new AxisAlignedBB(x, y, z, x + 1, y  + 1, z + 1), color);
  }

  public static void drawBox(AxisAlignedBB aabb, Color color) {
    startGL();
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    RenderManager renderManager = mc.getRenderManager();
    aabb = aabb.offset(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);
    double miX = aabb.minX;
    double miY = aabb.minY;
    double miZ = aabb.minZ;
    double maX = aabb.maxX;
    double maY = aabb.maxY;
    double maZ = aabb.maxZ;
    float a = color.getAlpha() / 255.0F;
    float r = color.getRed() / 255.0F;
    float g = color.getGreen() / 255.0F;
    float b = color.getBlue() / 255.0F;

    GlStateManager.color(r, g, b, a);
    worldrenderer.begin(7, DefaultVertexFormats.POSITION);
    worldrenderer.pos(miX, miY, miZ).endVertex();
    worldrenderer.pos(maX, miY, miZ).endVertex();
    worldrenderer.pos(maX, miY, maZ).endVertex();
    worldrenderer.pos(miX, miY, maZ).endVertex();
    worldrenderer.pos(miX, maY, maZ).endVertex();
    worldrenderer.pos(maX, maY, maZ).endVertex();
    worldrenderer.pos(maX, maY, miZ).endVertex();
    worldrenderer.pos(miX, maY, miZ).endVertex();
    worldrenderer.pos(miX, miY, maZ).endVertex();
    worldrenderer.pos(miX, maY, maZ).endVertex();
    worldrenderer.pos(miX, maY, miZ).endVertex();
    worldrenderer.pos(miX, miY, miZ).endVertex();
    worldrenderer.pos(maX, miY, miZ).endVertex();
    worldrenderer.pos(maX, maY, miZ).endVertex();
    worldrenderer.pos(maX, maY, maZ).endVertex();
    worldrenderer.pos(maX, miY, maZ).endVertex();
    worldrenderer.pos(miX, maY, miZ).endVertex();
    worldrenderer.pos(maX, maY, miZ).endVertex();
    worldrenderer.pos(maX, miY, miZ).endVertex();
    worldrenderer.pos(miX, miY, miZ).endVertex();
    worldrenderer.pos(miX, miY, maZ).endVertex();
    worldrenderer.pos(maX, miY, maZ).endVertex();
    worldrenderer.pos(maX, maY, maZ).endVertex();
    worldrenderer.pos(miX, maY, maZ).endVertex();
    tessellator.draw();
    outline(miX, miY, miZ, maX, maY, maZ, r, g, b, a, 2f);
    endGL();
  }

  public static void drawMultiLineText(ArrayList<String> lines, RenderGameOverlayEvent event, Color color, float scale) {
    ScaledResolution scaledResolution = event.resolution;
    int scaledWidth = scaledResolution.getScaledWidth();
    GlStateManager.pushMatrix();
    GlStateManager.translate((float) (scaledWidth / 2), 50, 0.0F);
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.scale(scale, scale, scale);
    FontRenderer fontRenderer = mc.fontRendererObj;
    int yOffset = 0;
    for (String line : lines) {
      fontRenderer.drawString(line, (-fontRenderer.getStringWidth(line) / 2f), yOffset, color.getRGB(), true);
      yOffset += fontRenderer.FONT_HEIGHT * 2;
    }

    GlStateManager.popMatrix();
  }

  public static void drawCenterTopText(String text, RenderGameOverlayEvent event, Color color) {
    drawCenterTopText(text, event, color, 3);
  }

  public static void drawCenterTopText(String text, RenderGameOverlayEvent event, Color color, float scale) {
    ScaledResolution scaledResolution = event.resolution;
    int scaledWidth = scaledResolution.getScaledWidth();
    GlStateManager.pushMatrix();
    GlStateManager.translate((float) (scaledWidth / 2), 50, 0.0F);
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.scale(scale, scale, scale);
    mc.fontRendererObj.drawString(text, (-mc.fontRendererObj.getStringWidth(text) / 2f), 0, color.getRGB(), true);
    GlStateManager.popMatrix();
  }

  public static void drawText(String str, double X, double Y, double Z, float scale) {
    float lScale = scale;
    FontRenderer fontRenderer = mc.fontRendererObj;

    double renderPosX = X - mc.getRenderManager().viewerPosX;
    double renderPosY = Y - mc.getRenderManager().viewerPosY;
    double renderPosZ = Z - mc.getRenderManager().viewerPosZ;

    double distance = Math.sqrt(renderPosX * renderPosX + renderPosY * renderPosY + renderPosZ * renderPosZ);
    double multiplier = Math.max(distance / 150f, 0.1f);
    lScale *= (float) (0.45f * multiplier);

    float xMultiplier = mc.gameSettings.thirdPersonView == 2 ? -1 : 1;

    GlStateManager.pushMatrix();
    GlStateManager.translate(renderPosX, renderPosY, renderPosZ);
    RenderManager renderManager = mc.getRenderManager();
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

    fontRenderer.drawString(str, -textWidth / 2, 0, 553648127);
    GlStateManager.depthMask(true);
    fontRenderer.drawString(str, -textWidth / 2, 0, -1);

    GlStateManager.enableDepth();
    GlStateManager.enableBlend();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.popMatrix();
  }

  public static void drawTracer(Vec3 to, Color color) {
    drawLine(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), to, color);
  }
}
