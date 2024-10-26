package com.jelly.mightyminerv2.feature.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.macro.commissionmacro.helper.Commission;
import com.jelly.mightyminerv2.util.CommissionUtil;
import com.jelly.mightyminerv2.util.EntityUtil;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.InventoryUtil.ClickMode;
import com.jelly.mightyminerv2.util.InventoryUtil.ClickType;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration.RotationType;
import com.jelly.mightyminerv2.util.helper.Target;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class AutoCommissionClaim extends AbstractFeature {

  private static AutoCommissionClaim instance;

  public static AutoCommissionClaim getInstance() {
    if (instance == null) {
      instance = new AutoCommissionClaim();
    }
    return instance;
  }

  private State state = State.STARTING;
  private ClaimError claimError = ClaimError.NONE;
  private Optional<EntityPlayer> emissary = Optional.empty();
  private List<Commission> nextComm = new ArrayList<>();
  private int retry = 0;

  @Override
  public String getName() {
    return "AutoCommissionClaim";
  }

  @Override
  public void start() {
    this.enabled = true;
    this.nextComm = null;
    this.claimError = ClaimError.NONE;
  }

  @Override
  public void stop() {
    if (!this.enabled) {
      return;
    }

    this.enabled = false;
    this.emissary = Optional.empty();
    this.timer.reset();
    this.resetStatesAfterStop();
    send("AutoCommissionClaim Stopped");
  }

  @Override
  public void resetStatesAfterStop() {
    this.state = State.STARTING;
    this.retry = 0;
  }

  @Override
  public boolean shouldNotCheckForFailsafe() {
    return true;
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

  public List<Commission> getNextComm() {
    return this.nextComm;
  }

  @SubscribeEvent
  protected void onTick(final ClientTickEvent event) {
    if (!this.enabled) {
      return;
    }

    if (this.retry > 3) {
      log("Tried too many times but failed. stopping");
      this.stop(ClaimError.INACCESSIBLE_NPC);
      return;
    }

    switch (this.state) {
      case STARTING:
        int time = 400;
        switch (MightyMinerConfig.commClaimMethod) {
          case 0:
            time = 0;
            break;
          case 1:
            if (!InventoryUtil.holdItem("Royal Pigeon")) {
              this.stop(ClaimError.NO_ITEMS);
              break;
            }
            break;
          case 2:
            if (!InventoryUtil.holdItem("Abiphone")) {
              this.stop(ClaimError.NO_ITEMS);
              break;
            }
            break;
        }
        this.swapState(State.ROTATING, time);
        break;
      case ROTATING:
        if (this.isTimerRunning()) {
          return;
        }
        if (MightyMinerConfig.commClaimMethod == 0) {
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
        }
        this.swapState(State.OPENING, 2000);
        break;
      case OPENING:
        if (this.hasTimerEnded()) {
          this.stop(ClaimError.TIMEOUT);
          error("Could not finish rotation in time.");
          break;
        }
        final Optional<Entity> entityLookingAt = EntityUtil.getEntityLookingAt();
        time = 5000;
        switch (MightyMinerConfig.commClaimMethod) {
          case 0:
            if (RotationHandler.getInstance().isEnabled() || !entityLookingAt.isPresent()) {
              return;
            }

            // because why not
            if (entityLookingAt.equals(this.emissary)) {
              KeyBindUtil.leftClick();
            } else {
              mc.playerController.interactWithEntitySendPacket(mc.thePlayer, this.emissary.get());
            }
            break;
          case 1:
            KeyBindUtil.rightClick();
          case 2:
            time = 0;
            // Toggle AutoAbiphone
            break;
        }

        log("Scheduler timer for : " + time);
        this.swapState(State.VERIFYING_GUI, time);
        break;
      case VERIFYING_GUI:
        if (this.hasTimerEnded()) {
          this.stop(ClaimError.INACCESSIBLE_NPC);
          error("Opened a Different Inventory Named: " + InventoryUtil.getInventoryName());
          break;
        }
        switch (MightyMinerConfig.commClaimMethod) {
          case 0:
          case 1:
            if (!(mc.thePlayer.openContainer instanceof ContainerChest) || !InventoryUtil.getInventoryName().contains("Commissions")) {
              break;
            }
            this.swapState(State.CLAIMING, 500);
            break;
          case 2:
            // verify autoabiphone
            break;
        }
        break;
      case CLAIMING:
        if (this.isTimerRunning()) {
          break;
        }
        final int slotToClick = CommissionUtil.getClaimableCommissionSlot();
        State nextState;
        if (slotToClick != -1) {
          InventoryUtil.clickContainerSlot(slotToClick, ClickType.LEFT, ClickMode.PICKUP);
          nextState = State.CLAIMING;
        } else {
          log("No Commission To Claim");
          nextState = State.NEXT_COMM;
        }
        this.swapState(nextState, MightyMinerConfig.getRandomGuiWaitDelay());
        break;
      case NEXT_COMM:
        if (this.isTimerRunning()) {
          break;
        }
        if (mc.thePlayer.openContainer instanceof ContainerChest) {
          this.nextComm = CommissionUtil.getCommissionFromContainer((ContainerChest) mc.thePlayer.openContainer);
        }
        this.swapState(State.ENDING, 0);
        break;
      case ENDING:
        if (this.isTimerRunning()) {
          return;
        }
        InventoryUtil.closeScreen();
        this.stop();
        break;
    }
  }

  @SubscribeEvent
  protected void onChat(ClientChatReceivedEvent event) {
    if (!this.enabled || this.state != State.CLAIMING || event.type != 0) {
      return;
    }

    String mess = event.message.getUnformattedText();
    if (mess.startsWith("This ability is on cooldown for ")) {
      this.retry++;
      log("Pigeon Cooldown Detected, Waiting for 5 Seconds");
      this.swapState(State.OPENING, 5000);
    }
  }

  private void swapState(final State state, final int time) {
    this.state = state;
    if (time == 0) {
      this.timer.reset();
    } else {
      this.timer.schedule(time);
    }
  }

  enum State {
    STARTING, ROTATING, OPENING, VERIFYING_GUI, CLAIMING, NEXT_COMM, ENDING
  }

  public enum ClaimError {
    NONE, INACCESSIBLE_NPC, NO_ITEMS, TIMEOUT
  }
}
