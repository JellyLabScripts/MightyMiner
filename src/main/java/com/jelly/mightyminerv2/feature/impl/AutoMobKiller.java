package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.command.OsamaTestCommandNobodyTouchPleaseLoveYou;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.event.UpdateEntityEvent;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.pathfinder.calculate.PathNode;
import com.jelly.mightyminerv2.util.EntityUtil;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.RenderUtil;
import com.jelly.mightyminerv2.util.ScoreboardUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.Target;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

// Todo: Make it a universal mob killer perhaps?
//  idk its not a combat macro
public class AutoMobKiller extends AbstractFeature {

  private static AutoMobKiller instance;

  public static AutoMobKiller getInstance() {
    if (instance == null) {
      instance = new AutoMobKiller();
    }
    return instance;
  }

  private State state = State.STARTING;
  private MKError mkError = MKError.NONE;
  private Clock shutdownTimer = new Clock();
  private Clock queueTimer = new Clock();
  private Set<EntityLiving> mobQueue = new HashSet<>();
  private Optional<EntityLiving> targetMob = Optional.empty();
  private Optional<Vec3> entityLastPosition = Optional.empty();
  private Set<String> mobToKill = new HashSet<>();
//  private Set<String> mobToKill = ImmutableSet.of("Glacite Walker", "Goblin", "Knifethrower", "Zombie", "Crypt Ghoul", "");

  @Override
  public String getName() {
    return "MobKiller";
  }

  public void start(Collection<String> mobsToKill, String weaponName) {
    // this should NEVER happen
    if (!InventoryUtil.holdItem(weaponName)) {
      error("Could not hold MobKiller Weapon");
      this.stop(MKError.NO_ENTITIES);
      return;
    }
    this.mobToKill.addAll(mobsToKill);
    this.mkError = MKError.NONE;
    this.enabled = true;

    this.start();
    log("Started");
  }

  @Override
  public void stop() {
    if (!this.enabled) {
      return;
    }
    this.enabled = false;
    this.mobToKill.clear();
    this.timer.reset();
    this.shutdownTimer.reset();
    this.targetMob = Optional.empty();
    this.resetStatesAfterStop();
    Pathfinder.getInstance().stop();

    log("stopped");
  }

  public void stop(MKError error) {
    this.mkError = error;
    this.stop();
  }

  @Override
  public void resetStatesAfterStop() {
    this.state = State.STARTING;
  }

  public boolean succeeded() {
    return !this.enabled && this.mkError == MKError.NONE;
  }

  public MKError getMkError() {
    return this.mkError;
  }

  @SubscribeEvent
  protected void onTick(ClientTickEvent event) {
    if (!this.enabled) {
      return;
    }

    if (this.shutdownTimer.isScheduled() && this.shutdownTimer.passed()) {
      this.stop(MKError.NO_ENTITIES);
      log("Entities did not spawn");
      return;
    }

    if (this.queueTimer.passed()) {
      log("Queue celared");
      this.mobQueue.clear();
      this.queueTimer.schedule(2000);
    }

    switch (this.state) {
      case STARTING:
        this.targetMob.ifPresent(this.mobQueue::add);
        this.changeState(State.FINDING_MOB, 0);
        log("Starting");
      case FINDING_MOB:
        List<EntityLiving> mobs = EntityUtil.getEntities(this.mobToKill, this.mobQueue);
        if (mobs.isEmpty()) {
          if (!this.shutdownTimer.isScheduled()) {
            log("Cannot find mobs. Starting a 10 second timer");
            this.shutdownTimer.schedule(10_000);
          }
          return;
        } else if (this.shutdownTimer.isScheduled()) {
          this.shutdownTimer.reset();
        }

        EntityLiving best = mobs.get(0);
        this.targetMob = Optional.ofNullable(best);
        this.entityLastPosition = Optional.ofNullable(best.getPositionVector());
        this.changeState(State.WALKING_TO_MOB, 0);
        break;
      case WALKING_TO_MOB:
        if (!this.targetMob.isPresent() || !this.entityLastPosition.isPresent()) {
          this.stop(MKError.NO_ENTITIES);
          log("no target mob || no last position saved"); // idk why this would happen but better safe than sorry (im schizophrenic)
          return;
        }
        Vec3 mobPos = this.targetMob.get().getPositionVector();

        Pathfinder.getInstance().queue(new BlockPos(mobPos.xCoord, Math.ceil(mobPos.yCoord) - 1, mobPos.zCoord));
        log("Queued new to " + mobPos);
        if (!Pathfinder.getInstance().isRunning()) {
          log("Pathfinder wasnt enabled. starting");
          Pathfinder.getInstance().setSprintState(MightyMinerConfig.commMobKillerSprint);
          Pathfinder.getInstance().setInterpolationState(MightyMinerConfig.commMobKillerInterpolate);
          Pathfinder.getInstance().start();
        }
        this.changeState(State.WAITING_FOR_MOB, 0);
        break;
      case WAITING_FOR_MOB:
        // next tick pos makes hits more accurate
        if (PlayerUtil.getNextTickPosition().squareDistanceTo(this.targetMob.get().getPositionVector()) < 8) { // 8 cuz why not
          this.changeState(State.LOOKING_AT_MOB, 0);
          return;
        }

        if (!this.targetMob.get().isEntityAlive()) {
          Pathfinder.getInstance().stop();
          this.changeState(State.STARTING, 0);
          return;
        }

        if (this.targetMob.get().getPositionVector().squareDistanceTo(this.entityLastPosition.get()) > 9) {
          this.changeState(State.STARTING, 0);
          log("target mob is far away. repathing");
          Pathfinder.getInstance().stop();
          return;
        }

        if (!Pathfinder.getInstance().isRunning()) {
          log("Pathfinder not enabled");
          this.changeState(State.STARTING, 0);
          return;
        }
        break;
      case LOOKING_AT_MOB:
        if (!Pathfinder.getInstance().isRunning()) {
          RotationHandler.getInstance().easeTo(new RotationConfiguration(new Target(this.targetMob.get()), 400, null));
          this.changeState(State.KILLING_MOB, 0);

          log("Rotating");
        }
      case KILLING_MOB:
        if (!Objects.equals(mc.objectMouseOver.entityHit, this.targetMob.get())) {
          if (mc.thePlayer.getDistanceSqToEntity(this.targetMob.get()) < 9 && Pathfinder.getInstance().isRunning()) {
            Pathfinder.getInstance().stop();
            return;
          }
          if (!Pathfinder.getInstance().isRunning() && !RotationHandler.getInstance().isEnabled()) {
            this.changeState(State.STARTING, 0);
          }
          return;
        }

        KeyBindUtil.leftClick();
        RotationHandler.getInstance().reset();
        this.changeState(State.STARTING, 0);
        break;
    }
  }

  @SubscribeEvent
  protected void onRender(RenderWorldLastEvent event) {
    if (!this.enabled || !this.targetMob.isPresent()) {
      return;
    }
    Vec3 pos = this.targetMob.get().getPositionVector();

    RenderUtil.drawBox(
        new AxisAlignedBB(pos.xCoord - 0.5, pos.yCoord, pos.zCoord - 0.5, pos.xCoord + 0.5, pos.yCoord, pos.zCoord + 0.5),
        new Color(255, 0, 241, 150)
    );
  }

  private void changeState(State state, int time) {
    this.state = state;
    this.timer.schedule(time);
  }

  enum State {
    STARTING, FINDING_MOB, WALKING_TO_MOB, WAITING_FOR_MOB, LOOKING_AT_MOB, KILLING_MOB,
//    STALLING, LOOKING - Todo: make this
  }

  public enum MKError {
    NONE, NO_ENTITIES
  }

  // EntityTracker
  Long2ObjectMap<String> stands = new Long2ObjectOpenHashMap<>();
  Long2ObjectMap<EntityLiving> entities = new Long2ObjectOpenHashMap<>();
  Object2ObjectMap<String, Set<EntityLiving>> mobs = new Object2ObjectOpenHashMap<>();

  // testing
  @SubscribeEvent
  public void onRenderEvent(RenderWorldLastEvent event) {
    mobs.forEach((a, b) -> {
      b.forEach(it -> {
        RenderUtil.drawText(a, it.posX, it.posY + 2.5, it.posZ, 1f);
        RenderUtil.drawBox(new AxisAlignedBB(it.posX - 0.5, it.posY, it.posZ - 0.5, it.posX + 0.5, it.posY, it.posZ + 0.5), new Color(123, 0, 234, 150));
      });
    });
  }

  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    stands.clear();
    entities.clear();
    mobs.clear();
  }


  // i love writing shitcode
  // i wrote a much cleaner readable version before but enjoy this >_<
  @SubscribeEvent
  public void onEntitySpawn(UpdateEntityEvent event) {
    if (!OsamaTestCommandNobodyTouchPleaseLoveYou.getInstance().allowed) {
      return;
    }
//    if (!this.enabled) {
//      return;
//    }

    // hash is just baritones BetterBlockPos::longHash
    // assuming mixin works properly and doesnt add anything other than armorstands and entitylivings
    String name = "";
    Entity entity = event.entity;
    int updateType = event.updateType;
    EntityLiving mob = null;
    Long hash;
    if (entity instanceof EntityArmorStand) {
      if (!entity.hasCustomName()) {
        return;
      }
      if ((name = EntityUtil.getEntityNameFromArmorStand(entity.getCustomNameTag())).isEmpty()) {
        return;
      }
      hash = PathNode.Companion.longHash(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ));
      mob = entities.remove(hash);
      if (mob == null) {
        stands.put(hash, name);
      } else {
        if(updateType == 0){
          mobs.computeIfAbsent(name, a -> new HashSet<>()).add(mob);
        } else {
          mobs.get(name).removeIf(mob::equals);
        }
      }
    } else {
      hash = PathNode.Companion.longHash(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY + Math.ceil(entity.height)), MathHelper.floor_double(entity.posZ));
      name = stands.remove(hash);
      if (name == null) {
        entities.put(hash, (EntityLiving) entity);
      } else {
        if(updateType == 0){
          mobs.computeIfAbsent(name, a -> new HashSet<>()).add((EntityLiving) entity);
        } else {
          mobs.get(name).removeIf(entity::equals);
        }
      }
    }
  }
}
