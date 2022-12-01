package com.jelly.MightyMiner.waypoints;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.utils.DrawUtils;
import gg.essential.api.commands.SubCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

public class WaypointHandler {

    private static final List<Waypoint> waypoints = new ArrayList<>();

    private final Minecraft mc = Minecraft.getMinecraft();
    public static void setupWaypoints() {
        waypoints.clear();
        waypoints.addAll(MightyMiner.waypointConfig.waypoints);
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {

    }

    public void drawWaypoint(Waypoint waypoint, float partialTicks) {
         Triple viewerX, viewerY, viewerZ = DrawUtils.viewerPosition(partialTicks);
         BlockPos blockPos = new BlockPos(waypoint.getX(), waypoint.getY(), waypoint.getZ());
    }
}
