package com.jelly.MightyMinerV2.Util.helper;

import com.jelly.MightyMinerV2.Util.AngleUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class Target {

  private Vec3 vec;
  @Getter
  private Entity entity;
  @Getter
  private BlockPos blockPos;
  @Getter
  private Angle angle;
  @Accessors(fluent = true)
  @Setter
  @Getter
  private float additionalY = (float) (1 + Math.random()) * 0.75f;

  public Target(Vec3 vec) {
    this.vec = vec;
  }

  public Target(Entity entity) {
    this.entity = entity;
  }

  public Target(BlockPos blockPos) {
    this.blockPos = blockPos;
  }

  public Target(Angle angle) {
    this.angle = angle;
  }

  // Ensures Rotation Always Ends
  public Angle getTargetAngle() {
    if (blockPos != null) {
      return AngleUtil.getRotation(blockPos);
    }

    if (vec != null) {
      return AngleUtil.getRotation(vec);
    }

    if (entity != null) {
      return AngleUtil.getRotation(entity.getPositionVector().addVector(0, additionalY, 0));
    }

    return angle;
  }
}
