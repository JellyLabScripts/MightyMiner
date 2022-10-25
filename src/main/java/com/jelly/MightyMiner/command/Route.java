package com.jelly.MightyMiner.command;

import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;

import static com.jelly.MightyMiner.MightyMiner.coordsConfig;


public class Route extends CommandBase {
    Minecraft mc = Minecraft.getMinecraft();

    private final String CLEAN = "" + EnumChatFormatting.RESET + EnumChatFormatting.AQUA + EnumChatFormatting.ITALIC;

    @Override
    public String getCommandName() {
        return "route";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Jeff Bezos";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            mc.thePlayer.addChatMessage(new ChatComponentText((""+EnumChatFormatting.DARK_AQUA + EnumChatFormatting.BOLD + "---------------------------------------------")));
            LogUtils.addMessage("Usage: " + CLEAN + "/route <create - remove - select - listroutes - listpoints - addpoint - removepoint>");
            LogUtils.addMessage("First create a route with " + CLEAN + "/route create <name>");
            LogUtils.addMessage("Then select the route with " + CLEAN + "/route select <name>");
            LogUtils.addMessage("Then add points with " + CLEAN + "/route addpoint <order-of-point> (starting at 1)");
            LogUtils.addMessage("After that, you can start the macro");
            mc.thePlayer.addChatMessage(new ChatComponentText((""+EnumChatFormatting.DARK_AQUA + EnumChatFormatting.BOLD + "---------------------------------------------")));

            LogUtils.addMessage("You can remove your route with " + CLEAN + "/route remove <name>");
            LogUtils.addMessage("You can remove a point with " + CLEAN + "/route removepoint <name> <order-of-point>");
            LogUtils.addMessage("You can list all routes with " + CLEAN + "/route listroutes");
            LogUtils.addMessage("You can list all points from selected route " + CLEAN + "/route listpoints");
            mc.thePlayer.addChatMessage(new ChatComponentText((""+EnumChatFormatting.DARK_AQUA + EnumChatFormatting.BOLD + "---------------------------------------------")));
            return;
        }

        String name = args[0];
        String arguments = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (args.length == 1 && args[0].matches("create|remove|select")) {
            LogUtils.addMessage("Usage: " + CLEAN + "/route" + name + " <route-name>");
            return;
        }


        switch (name) {
            case "create":
                if (coordsConfig.getRoute(arguments) != null) {
                    LogUtils.addMessage("Route already exists!");
                } else {
                    coordsConfig.addRoute(arguments);
                    coordsConfig.getSelectedRoute().forEach((k, v) -> BlockRenderer.renderMap.remove(v));
                    coordsConfig.setSelectedRoute(arguments);
                    LogUtils.addMessage("Created route and selected it: " + CLEAN + arguments);
                }
                break;

            case "select":
                if (coordsConfig.getRoute(arguments) == null) {
                    LogUtils.addMessage("Route does not exist!");
                } else {
                    coordsConfig.getSelectedRoute().forEach((k, v) -> BlockRenderer.renderMap.remove(v));
                    coordsConfig.setSelectedRoute(arguments);
                    LogUtils.addMessage("Selected route: " + CLEAN + arguments);
                }
                break;

            case "remove":
                if (coordsConfig.getRoute(arguments) == null) {
                    LogUtils.addMessage("Route does not exist!");
                } else if (coordsConfig.getRoutes().size() == 1) {
                    LogUtils.addMessage("You cannot remove the only route you have!");
                } else {
                    if (coordsConfig.getRoute(arguments) == coordsConfig.getSelectedRoute()) {
                        LogUtils.addMessage("Change to a different route before removing this!");
                        return;
                    }
                    coordsConfig.removeRoute(arguments);
                    LogUtils.addMessage("Removed route: " + CLEAN + arguments);
                }
                break;

            case "listroutes":
                LogUtils.addMessage("Routes: " + CLEAN + String.join(", ", coordsConfig.getRoutes().keySet()));
                break;

            case "addpoint":
                if (coordsConfig.getSelectedRoute() == null) {
                    LogUtils.addMessage("You must select a route first!");
                } else {
                    BlockPos toAdd = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);

                    if (coordsConfig.getSelectedRoute().containsValue(toAdd)) {
                        LogUtils.addMessage("This point is a duplicate, won't add it");
                        return;
                    }
                    int number = Integer.parseInt(arguments);
                    if (number < 1) {
                        LogUtils.addMessage("Number must be greater than 0");
                        return;
                    }
                    // if current point already exists, remove it from render
                    if (coordsConfig.getSelectedRoute().containsKey(number)) {
                        BlockRenderer.renderMap.remove(coordsConfig.getSelectedRoute().get(Integer.valueOf(arguments)));
                    }
                    coordsConfig.addCoord(Integer.parseInt(arguments), new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ));

                    LogUtils.addMessage("Added point at " + CLEAN + (new Vec3(toAdd)));
                }
                break;

            case "removepoint":
                if (coordsConfig.getSelectedRoute() == null) {
                    LogUtils.addMessage("You must select a route first!");
                } else {
                    BlockPos b = coordsConfig.getSelectedRoute().get(Integer.valueOf(arguments));
                    if (b == null) {
                        LogUtils.addMessage("Point does not exist!");
                    } else {
                        BlockRenderer.renderMap.remove(b);
                        LogUtils.addMessage("Removed point at " + CLEAN + (new Vec3(b)));
                        coordsConfig.removeCoord(Integer.parseInt(arguments));
                    }

                }
                break;
            case "listpoints":
                if (coordsConfig.getSelectedRoute() == null) {
                    LogUtils.addMessage("You must select a route first!");
                } else {
                    LogUtils.addMessage("Points in the following route: " + CLEAN + coordsConfig.getSelectedRouteName());
                    coordsConfig.getSelectedRoute().entrySet().stream().sorted()
                            .forEach((e) -> LogUtils.addMessage("Point " + e.getKey() + CLEAN +  ": " + e.getValue()));
                }
                break;
        }

        coordsConfig.save();
    }

    @SubscribeEvent
    public void tick(RenderWorldLastEvent ev) {
        if (coordsConfig.getSelectedRoute() == null) return;
        Map<Integer, BlockPos> c = coordsConfig.getSelectedRoute();
        c.forEach((name, coords) -> {
            if (!BlockRenderer.renderMap.containsKey(coords)) {
                BlockRenderer.renderMap.put(coords, Color.CYAN);
            }
            if (coords == null) return;
            drawSpots(coords, ev.partialTicks, name);
        });
    }

    private void drawSpots(BlockPos pos, float partialTicks, int name) {
    /*    GlStateManager.alphaFunc(516, 0.1f);
        GlStateManager.pushMatrix();
        Entity viewer = mc.getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;
        double x = pos.getX() + 0.5 - viewerX;
        double y = pos.getY() + 1.6 - viewerY - viewer.getEyeHeight();
        double z = pos.getZ() + 0.5 - viewerZ;
        double distSq = x * x + y * y + z * z;
        double dist = Math.sqrt(distSq);
        if (distSq > 144) {
            x *= 12 / dist;
            y *= 12 / dist;
            z *= 12 / dist;
        }
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0f, viewer.getEyeHeight(), 0f);
        drawNametag(String.valueOf(name));
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        GlStateManager.translate(0f, -0.25f, 0f);
        GlStateManager.rotate(-mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.popMatrix();
        GlStateManager.disableLighting();*/
    }
}
