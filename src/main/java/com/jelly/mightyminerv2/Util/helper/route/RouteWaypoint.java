package com.jelly.mightyminerv2.Util.helper.route;

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
  private Vec3 vec;

  public RouteWaypoint(){}

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
    if (vec == null) {
      vec = new Vec3(this.x, this.y, this.z);
    }
    return vec;
  }

  @Override
  public String toString() {
    return x + "," + y + "," + z + "," + transportMethod.name();
  }
}
