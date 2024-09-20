package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.event.UpdateEntityEvent;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.util.EntityUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MobTracker extends AbstractFeature {

  private static MobTracker instance = new MobTracker();

  public static MobTracker getInstance() {
    return instance;
  }

  @Override
  public String getName() {
    return "MobTracker";
  }

  private final Long2ObjectMap<IntSet> entities = new Long2ObjectOpenHashMap<>();
  private final Object2ObjectMap<String, ObjectSet<EntityLivingBase>> mobs = new Object2ObjectOpenHashMap<>();
  private final Int2ObjectMap<String> locatedMobs = new Int2ObjectOpenHashMap<>();

  public Set<EntityLivingBase> getEntity(String name) {
    Set<EntityLivingBase> list = mobs.get(name);
    if (list == null) {
      return new HashSet<>();
    }
    return list;
  }

  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    entities.clear();
    mobs.clear();
    locatedMobs.clear();
  }

  // i love writing shitcode
  // i wrote a much cleaner readable version before but enjoy this >_<
  // hash is just baritones BetterBlockPos::longHash
  // assuming mixin works properly and doesnt add anything other than armorstands and entitylivings
  @SubscribeEvent
  public void onEntitySpawn(UpdateEntityEvent event) {
    byte updateType = event.updateType;
    EntityLivingBase entity = event.entity;
    String name = "";
    int entityId = -1;
    long hash = pack((int) Math.round(entity.posX), (int) Math.round(entity.posZ));
    if (entity instanceof EntityArmorStand) {
      if (updateType != 0) {
        return;
      }
      if ((name = EntityUtil.getEntityNameFromArmorStand(entity.getCustomNameTag())).isEmpty()) {
        return;
      }
      EntityLivingBase mob = null;
      IntSet mobs = entities.get(hash);
      if (mobs != null) {
        double minDist = Double.MAX_VALUE;
        for (int it : mobs) {
          EntityLivingBase curr = (EntityLivingBase) mc.theWorld.getEntityByID(it);
          double dist = entity.getDistanceSq(curr.posX, curr.posY + curr.height, curr.posZ);
          if (dist < minDist) {
            mob = curr;
            minDist = dist;
            entityId = it;
          }
        }
        mobs.remove(entityId);
        if (mobs.isEmpty()) {
          entities.remove(hash);
        }
      }
      if (mob != null && entityId != -1) {
        this.mobs.computeIfAbsent(name, b -> new ObjectOpenHashSet<>()).add(mob);
        locatedMobs.put(entityId, name);
      }
    } else {
      boolean wasCached = locatedMobs.containsKey(entity.getEntityId());
      entityId = entity.getEntityId();
      if (updateType == 2) {
        if (!wasCached && entities.containsKey(hash) && entities.get(hash).remove(entityId)) {
          entities.computeIfAbsent(event.newHash, k -> new IntOpenHashSet()).add(entityId);
        }
        return;
      }
      if (updateType == 0) {
        if (!wasCached) {
          entities.computeIfAbsent(hash, k -> new IntOpenHashSet()).add(entityId);
        }
        mobs.computeIfAbsent(entity.getName(), k -> new ObjectOpenHashSet<>()).add(entity);
      } else {
        IntSet sett = entities.get(hash);
        if (sett != null) {
          sett.remove(entityId);
          if (sett.isEmpty()) {
            entities.remove(hash);
          }
        }
        if (wasCached) {
          this.mobs.computeIfPresent(locatedMobs.remove(entity.getEntityId()), (key, val) -> {
            val.remove(entity);
            return val.isEmpty() ? null : val;
          });
          this.mobs.computeIfPresent(entity.getName(), (key, val) -> {
            val.remove(entity);
            return val.isEmpty() ? null : val;
          });
        }
      }
    }
  }

  private long pack(int x, int z) {
    return ((long) x << 32) | (z & 0xFFFFFFFFL);
  }
}
