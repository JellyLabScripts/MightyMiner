package com.jelly.mightyminerv2.event;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.Event;

public class UpdateEntityEvent extends Event {

  public final Entity entity;
  public final int updateType; // 0 = entity spawned, 1 = entity despawned, 2 = entity moved
  public long newHash; // maybe instead of saving new hash i should just save the coord - but i dont have a use for the coord so L

  public UpdateEntityEvent(Entity entity) {
    this.entity = entity;
    this.updateType = 0;
  }

  public UpdateEntityEvent(Entity entity, int updateType) {
    this.entity = entity;
    this.updateType = updateType;
  }

  public UpdateEntityEvent(Entity entity, long newHash) {
    this.entity = entity;
    this.updateType = 2;
    this.newHash = newHash;
  }
}
