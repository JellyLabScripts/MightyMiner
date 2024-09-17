package com.jelly.mightyminerv2.event;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.Event;

public class UpdateEntityEvent extends Event {

  public final Entity entity;
  public final int type;
  public final UpdateType updateType;
  public Vec3 oldPos;
  public Vec3 newPos;

  public UpdateEntityEvent(Entity entity, int type) {
    this.entity = entity;
    this.type = type;
    this.updateType = UpdateType.SPAWN;
  }

  public UpdateEntityEvent(Entity entity, int type, UpdateType updateType) {
    this.entity = entity;
    this.type = type;
    this.updateType = updateType;
  }

  public UpdateEntityEvent(Entity entity, int type, Vec3 oldPos, Vec3 newPos) {
    this.entity = entity;
    this.type = type;
    this.updateType = UpdateType.MOVEMENT;
    this.oldPos = oldPos;
    this.newPos = newPos;
  }

  public enum UpdateType {
    SPAWN, DESPAWN, MOVEMENT // movement only if it changes the block it was on
  }
}
