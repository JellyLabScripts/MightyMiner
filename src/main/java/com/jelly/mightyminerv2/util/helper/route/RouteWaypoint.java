package com.jelly.mightyminerv2.util.helper.route;

import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

@Data
@EqualsAndHashCode(exclude = {"vec", "transportMethod"})
public class RouteWaypoint {

  @Expose
  private int x;
  @Expose
  private int y;
  @Expose
  private int z;
  @Expose
  private TransportMethod transportMethod;

  public RouteWaypoint() {
  }

  public RouteWaypoint(int x, int y, int z, TransportMethod transportMethod) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.transportMethod = transportMethod;
  }

  public RouteWaypoint(BlockPos pos, TransportMethod transportMethod) {
    this(pos.getX(), pos.getY(), pos.getZ(), transportMethod);
  }

  public Vec3 toVec3() {
    return new Vec3(this.x, this.y, this.z);
  }

  public BlockPos toBlockPos() {
    return new BlockPos(this.x, this.y, this.z);
  }

  @Override
  public String toString() {
    return x + "," + y + "," + z + "," + transportMethod.name();
  }
}
