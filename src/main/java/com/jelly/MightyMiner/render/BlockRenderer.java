package com.jelly.MightyMiner.render;

import com.jelly.MightyMiner.MightyMiner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BlockRenderer {
    public static final HashMap<Integer, Boolean> glCapMap = new HashMap<>();
    public static HashMap<BlockPos, Color> renderMap = new HashMap<>();

    @SubscribeEvent
    public void renderAABB(RenderWorldLastEvent e) {
        try {
            if (!renderMap.isEmpty()) {
                for (Map.Entry<BlockPos, Color> entry : renderMap.entrySet()) {
                    if (MightyMiner.coordsConfig.getSelectedRoute().valueList().contains(entry.getKey())) {
                        // dont render if on crystal hollows
                    }
                    BlockPos blockPos = entry.getKey();
                    Color color = entry.getValue();

                    RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
                    double x = (double) blockPos.getX() - renderManager.viewerPosX;
                    double y = (double) blockPos.getY() - renderManager.viewerPosY;
                    double z = (double) blockPos.getZ() - renderManager.viewerPosZ;
                    AxisAlignedBB aabb = new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0);

                    int width = 3;
                    GL11.glBlendFunc(770, 771);

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
                    GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() != 255 ? color.getAlpha() : 26);

                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    worldrenderer.begin(3, DefaultVertexFormats.POSITION);
                    worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
                    worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
                    worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
                    worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
                    worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
                    worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
                    worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
                    worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
                    worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
                    worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
                    worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
                    worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
                    worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
                    worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
                    worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
                    worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
                    tessellator.draw();

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
            }
        } catch (Exception ignored) {
        }
    }
}
