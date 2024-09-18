package com.jelly.mightyminerv2.event;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Event;

public class UpdateEntityEvent extends Event {

  public final Entity entity;
  public final int updateType; // 0 = entity spawned, else = entity despawned

  public UpdateEntityEvent(Entity entity) {
    this.entity = entity;
    this.updateType = 0;
  }

  public UpdateEntityEvent(Entity entity, int updateType) {
    this.entity = entity;
    this.updateType = updateType;
  }
}
