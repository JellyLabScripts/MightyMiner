package com.jelly.MightyMinerV2.Util.helper.route;

import com.google.gson.annotations.Expose;
import com.jelly.MightyMinerV2.Config.MightyMinerConfig;
import com.jelly.MightyMinerV2.Util.RenderUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;

@Data
@EqualsAndHashCode(exclude = "routeIndex")
public class RouteWaypoint {
    @Expose
    public int routeIndex = -1;
    @Expose
    private int x;
    @Expose
    private int y;
    @Expose
    private int z;
    @Expose
    private TransportMethod transportMethod;

    public RouteWaypoint(int x, int y, int z, TransportMethod transportMethod) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.transportMethod = transportMethod;
    }

    public RouteWaypoint(BlockPos pos, TransportMethod transportMethod) {
        this(pos.getX(), pos.getY(), pos.getZ(), transportMethod);
    }

    public void draw() {
        RenderUtil.drawBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), MightyMinerConfig.routeBuilderNodeColor.toJavaColor());
        RenderUtil.drawText(String.valueOf(routeIndex + 1), x + 0.5, y + 1, z + 0.5, 1);
    }
}
