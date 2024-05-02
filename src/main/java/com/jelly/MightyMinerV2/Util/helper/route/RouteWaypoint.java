package com.jelly.MightyMinerV2.Util.helper.route;

import com.google.gson.annotations.Expose;
import lombok.Data;
import net.minecraft.util.BlockPos;

@Data
public class RouteWaypoint {
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
}
