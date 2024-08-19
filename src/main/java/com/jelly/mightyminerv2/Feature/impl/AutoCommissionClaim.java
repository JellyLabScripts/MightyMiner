package com.jelly.mightyminerv2.Feature.impl;

import com.jelly.mightyminerv2.Feature.IFeature;
import com.jelly.mightyminerv2.Handler.RotationHandler;
import com.jelly.mightyminerv2.Util.CommissionUtil;
import com.jelly.mightyminerv2.Util.EntityUtil;
import com.jelly.mightyminerv2.Util.InventoryUtil;
import com.jelly.mightyminerv2.Util.InventoryUtil.ClickMode;
import com.jelly.mightyminerv2.Util.InventoryUtil.ClickType;
import com.jelly.mightyminerv2.Util.KeyBindUtil;
import com.jelly.mightyminerv2.Util.helper.Clock;
import com.jelly.mightyminerv2.Util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.Util.helper.RotationConfiguration.RotationType;
import com.jelly.mightyminerv2.Util.helper.Target;
import java.util.Optional;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class AutoCommissionClaim implements IFeature {

  private static AutoCommissionClaim instance;

  public static AutoCommissionClaim getInstance() {
    if (instance == null) {
      instance = new AutoCommissionClaim();
    }
    return instance;
  }

  private final Minecraft mc = Minecraft.getMinecraft();
  @Getter
  private boolean enabled = false;
  private State state = State.STARTING;
  private ClaimError claimError = ClaimError.NONE;
  private Clock timer = new Clock();
  private Optional<EntityPlayer> emissary = Optional.empty();

  @Override
  public String getName() {
    return "AutoCommissionClaim";
  }

  @Override
  public boolean isRunning() {
    return this.enabled;
  }

  @Override
  public boolean shouldPauseMacroExecution() {
    return false;
  }

  @Override
  public boolean shouldStartAtLaunch() {
    return false;
  }

  @Override
  public void start() {
    this.enabled = true;
    this.claimError = ClaimError.NONE;
  }

  @Override
  public void stop() {
    if(!this.enabled) return;

    this.enabled = false;
    this.emissary = Optional.empty();
    this.timer.reset();
    this.resetStatesAfterStop();
    send("AutoCommissionClaim Stopped");
  }

  public void stop(ClaimError error) {
    this.claimError = error;
    this.stop();
  }

  public boolean succeeded() {
    return !this.enabled && this.claimError == ClaimError.NONE;
  }

  public ClaimError claimError() {
    return this.claimError;
  }

  @Override
  public void resetStatesAfterStop() {
    this.state = State.STARTING;
  }

  @Override
  public boolean shouldCheckForFailsafe() {
    return false;
  }

  @SubscribeEvent
  public void onTick(final ClientTickEvent event) {
    if (mc.thePlayer == null || mc.theWorld == null || !this.isRunning()) {
      return;
    }

    switch (this.state) {
      case STARTING:
        this.swapState(State.ROTATING, 0);
        break;
      case ROTATING:
        this.emissary = CommissionUtil.getClosestEmissary();
        if (!this.emissary.isPresent()) {
          this.stop(ClaimError.INACCESSIBLE_NPC);
          error("Cannot Find Emissary. Stopping");
          break;
        }

        if (mc.thePlayer.getDistanceSqToEntity(this.emissary.get()) > 16) {
          this.stop(ClaimError.INACCESSIBLE_NPC);
          error("Emissary is too far away.");
          break;
        }

        RotationHandler.getInstance().easeTo(new RotationConfiguration(new Target(this.emissary.get()), 500, RotationType.CLIENT, null));

        this.swapState(State.OPENING, 1000);
        break;
      case OPENING:
        if (this.hasTimerEnded()) {
          this.stop(ClaimError.TIMEOUT);
          error("Could not finish rotation in time.");
          break;
        }
        final Optional<Entity> entityLookingAt = EntityUtil.getEntityLookingAt();
        if (RotationHandler.getInstance().isEnabled() || !entityLookingAt.isPresent()) {
          return;
        }

        // because why not
        if (entityLookingAt.equals(this.emissary)) {
          KeyBindUtil.leftClick();
        } else {
          mc.playerController.interactWithEntitySendPacket(mc.thePlayer, this.emissary.get());
        }

        this.swapState(State.CLAIMING, 500);

        break;
      case CLAIMING:
        if (!this.hasTimerEnded()) {
          return;
        }

        if (!InventoryUtil.getInventoryName().contains("Commissions")) {
          this.stop(ClaimError.INACCESSIBLE_NPC);
          error("Opened a Different Inventory.");
          break;
        }

        final int slotToClick = CommissionUtil.getClaimableCommissionSlot();
        if (slotToClick != -1) {
          InventoryUtil.clickContainerSlot(slotToClick, ClickType.LEFT, ClickMode.PICKUP);
        } else {
          send("No Commission To Claim");
        }
        this.swapState(State.ENDING, 500);
        break;
      case ENDING:
        if (!this.hasTimerEnded()) {
          return;
        }
        InventoryUtil.closeScreen();
        this.stop();
        break;
    }
  }

  private boolean hasTimerEnded() {
    return this.timer.isScheduled() && this.timer.passed();
  }

  private void swapState(final State state, final int time) {
    this.state = state;
    this.timer.schedule(time);
  }

  enum State {
    STARTING, ROTATING, OPENING, CLAIMING, ENDING
  }

  public enum ClaimError {
    NONE, INACCESSIBLE_NPC, TIMEOUT
  }
}
