package com.jelly.mightyminerv2.Macro.commissionmacro;

import com.jelly.mightyminerv2.Feature.impl.AutoWarp;
import com.jelly.mightyminerv2.Handler.GameStateHandler;
import com.jelly.mightyminerv2.Macro.AbstractMacro;
import com.jelly.mightyminerv2.Util.InventoryUtil;
import com.jelly.mightyminerv2.Util.helper.location.Location;
import com.jelly.mightyminerv2.Util.helper.location.SubLocation;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class CommissionMacro extends AbstractMacro {

  private static CommissionMacro instance = new CommissionMacro();

  public static CommissionMacro getInstance() {
    return instance;
  }

  private MainState mainState = MainState.NONE;

  private int miningSpeed = 0;
  private int miningSpeedBoost = 0;

  @Override
  public String getName() {
    return "CommissionMacro";
  }

  @Override
  public void onEnable() {
  }

  @Override
  public void onDisable() {
    this.mainState = MainState.NONE;
    this.warpState = WarpState.STARTING;
    this.macroState = MacroState.STARTING;

    this.warpRetries = 0;
  }

  @Override
  public List<String> getNecessaryItems() {
    return new ArrayList<>();
  }

  private void changeMainState(MainState to, int timeToWait) {
    this.mainState = to;
    this.timer.schedule(timeToWait);
  }

  private void changeMainState(MainState to) {
    this.mainState = to;
  }

  @SubscribeEvent
  public void onTick(ClientTickEvent event) {
    if (!this.isEnabled()) {
      return;
    }

    if (!this.hasTimerEnded()) {
      return;
    }

    if (this.mainState == MainState.MACRO) {
      if (GameStateHandler.getInstance().getCurrentLocation() != Location.DWARVEN_MINES) {
        this.changeMainState(MainState.WARP, 0);
      } else if (!InventoryUtil.areItemsInInventory(this.getNecessaryItems())) {
        this.changeMainState(MainState.NONE, 0);
      } else if (!InventoryUtil.areItemsInHotbar(this.getNecessaryItems())) {
        this.changeMainState(MainState.ITEMS, 0);
      }
    }

    switch (this.mainState) {
      case NONE:
        this.disable();
        break;
      case WARP:
        this.handleWarp();
        break;
      case ITEMS:
        this.handleItems();
        break;
      case MACRO:
        this.handleMacro();
        break;
    }
  }

  // <editor-fold desc="Handle Macro">
  private MacroState macroState = MacroState.STARTING;

  private void changeMacroState(MacroState to, int timeToWait) {
    this.macroState = to;
    this.timer.schedule(timeToWait);
  }

  private void changeMacroState(MacroState to) {
    this.macroState = to;
  }

  public void handleMacro() {
    switch (this.macroState) {
      case STARTING:
//        if (this.miningSpeed ==) {
//        }
        break;
      case CHECKING_STATS:
        break;
      case GETTING_STATS:
        break;
      case CHECKING_COMMISSION:
        break;
      case PATHING:
        break;
      case PATHING_VERIFY:
        break;
      case ENABING_FEATURE:
        break;
      case FEATURE_VERIFY:
        break;
      case CLAIMING_COMMISSION:
        break;
      case CLAIM_VERIFY:
        break;
    }
  }
  // </editor-fold>


  public void handleItems() {

  }

  // <editor-fold desc="Handle Warp">
  private WarpState warpState = WarpState.STARTING;
  private int warpRetries = 0;

  private void changeWarpState(WarpState to, int timeToWait) {
    this.warpState = to;
    this.timer.schedule(timeToWait);
  }

  private void changeWarpState(WarpState to) {
    this.warpState = to;
  }

  public void handleWarp() {
    switch (this.warpState) {
      case STARTING:
        this.changeWarpState(WarpState.TRIGGERING_AUTOWARP);
        break;

      case TRIGGERING_AUTOWARP:
        AutoWarp.getInstance().enable(null, SubLocation.THE_FORGE);
        this.changeWarpState(WarpState.WAITING_FOR_AUTOWARP);
        break;

      case WAITING_FOR_AUTOWARP:
        if (AutoWarp.getInstance().isRunning()) {
          return;
        }

        if (AutoWarp.getInstance().hasSucceeded()) {
          log("AutoWarp Completed");
          this.changeMainState(MainState.MACRO);
          return;
        }

        if (++this.warpRetries > 3) {
          this.changeMainState(MainState.NONE);
          error("Tried to warp 3 times but didn't reach destination. Disabling.");
        } else {
          log("Something went wrong while warping. Trying to fix!");
          this.changeWarpState(WarpState.HANDLE_ERRORS);
        }
        break;

      case HANDLE_ERRORS:
        switch (AutoWarp.getInstance().getFailReason()) {
          case NONE:
            throw new IllegalStateException("AutoWarp Failed But FailReason is NONE.");
          case FAILED_TO_WARP:
            log("Retrying AutoWarp");
            this.changeWarpState(WarpState.STARTING);
            break;
          case NO_SCROLL:
            log("No Warp Scroll. Disabling");
            this.changeMainState(MainState.NONE);
            break;
        }
    }
  }
  // </editor-fold>

  enum MainState {
    NONE, WARP, ITEMS, MACRO,
  }

  enum WarpState {
    STARTING, TRIGGERING_AUTOWARP, WAITING_FOR_AUTOWARP, HANDLE_ERRORS
  }

  enum MacroState {
    STARTING, CHECKING_STATS, GETTING_STATS, CHECKING_COMMISSION, PATHING, PATHING_VERIFY, ENABING_FEATURE, FEATURE_VERIFY, CLAIMING_COMMISSION, CLAIM_VERIFY
  }
}
