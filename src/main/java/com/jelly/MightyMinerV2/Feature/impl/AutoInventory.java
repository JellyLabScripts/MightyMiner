package com.jelly.MightyMinerV2.Feature.impl;

import com.jelly.MightyMinerV2.Feature.IFeature;
import com.jelly.MightyMinerV2.Util.InventoryUtil;
import com.jelly.MightyMinerV2.Util.ScoreboardUtil;
import com.jelly.MightyMinerV2.Util.TablistUtil;
import com.jelly.MightyMinerV2.Util.helper.Clock;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

// A separate class for inventory-related tasks that aren't significant enough to warrant their own feature class
public class AutoInventory implements IFeature {

  private static AutoInventory instance;

  public static AutoInventory getInstance() {
    if (instance == null) {
      instance = new AutoInventory();
    }
    return instance;
  }

  private final Minecraft mc = Minecraft.getMinecraft();
  @Getter
  private boolean enabled = false;
  private Task mainTask = Task.NONE;
  private Clock timer = new Clock();

  @Override
  public String getName() {
    return "AutoInventory";
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

  }

  @Override
  public void stop() {
    this.enabled = false;

    this.resetStatesAfterStop();
  }

  @Override
  public void resetStatesAfterStop() {
    this.mainTask = Task.NONE;
    this.sbState = SB.STARTING;
  }

  @Override
  public boolean isToggle() {
    return false;
  }

  @Override
  public boolean shouldCheckForFailSafe() {
    return false;
  }

  @SubscribeEvent
  public void onTick(final ClientTickEvent event) {
    if (mc.thePlayer == null || mc.theWorld == null || !this.isRunning()) {
      return;
    }

    switch (this.mainTask) {
      case NONE:
        this.stop();
        break;
      case GET_SPEED_BOOST:
        this.handleGetSpeedBoost();
        break;
    }
  }

  enum Task {
    NONE, GET_SPEED_BOOST
  }

  //<editor-fold desc="Get Mining Speed And Mining Speed Boost (Gemstone Later)">

  public void retrieveSpeedBoost() {
    this.mainTask = Task.GET_SPEED_BOOST;
    this.sbState = SB.STARTING;
    this.sbFail = SBFail.NONE;
    this.speedBoostValues = new int[2];

    this.enabled = true;
  }

  @Getter
  private int[] speedBoostValues = new int[2]; // [Mining Speed, Mining Speed Boost]
  private SB sbState = SB.STARTING;
  private SBFail sbFail = SBFail.NONE;

  private void handleGetSpeedBoost() {
    switch (this.sbState) {
      case STARTING:
        this.swapSbState(SB.GET_SPEED, 1000);
        break;
      case GET_SPEED:
        if (this.hasTimerEnded()) {
          this.stop();
          this.sbFail = SBFail.CANNOT_GET_VALUE;
          error("Could Not Get Speed In Time.");
          break;
        }

        for (final String text : TablistUtil.getCachedTablist()) {
          if (!text.contains("Mining Speed")) {
            continue;
          }

          try {
            this.speedBoostValues[0] = Integer.parseInt(
                ScoreboardUtil.sanitizeString(text).split(": ")[1].replace(",", "")
            );
            this.swapSbState(SB.OPEN_HOTM_MENU, 1000);
            mc.thePlayer.sendChatMessage("/hotm");
            log("Speed: " + this.speedBoostValues[0]);
            return;
          } catch (Exception ignored) {
          }
        }

        this.stop();
        this.sbFail = SBFail.CANNOT_GET_VALUE;
        error("Could not get mining speed from tab. Make sure its enabled.");
        break;
      case OPEN_HOTM_MENU:
        if (this.hasTimerEnded()) {
          this.stop();
          this.sbFail = SBFail.CANNOT_OPEN_INV;
          error("Could Not Open Inventory in Time.");
          break;
        }
        if (!InventoryUtil.getInventoryName().contains("Heart of the Mountain") || !InventoryUtil.isInventoryLoaded()) {
          break;
        }

        final int speedBoostSlot = InventoryUtil.getSlotIdOfItemInContainer("Mining Speed Boost");
        final String speedBoostLore = String.join(" ", InventoryUtil.getLoreOfItemInContainer(speedBoostSlot));

        final Matcher matcher = Pattern.compile("\\+(\\d+)%").matcher(speedBoostLore);
        if (matcher.find()) {
          this.speedBoostValues[1] = Integer.parseInt(matcher.group(1));
          this.swapSbState(SB.END, 200);
          break;
        }

        this.stop();
        this.sbFail = SBFail.CANNOT_GET_VALUE;
        error("Could Not Get Speed Boost Value.");
        break;
      case END:
        if (!this.hasTimerEnded()) {
          break;
        }
        InventoryUtil.closeScreen();
        this.stop();
        break;
    }
  }

  private void swapSbState(final SB state, final int time) {
    this.sbState = state;
    this.timer.schedule(time);
  }

  enum SB {
    STARTING, GET_SPEED, OPEN_HOTM_MENU, END
  }

  enum SBFail {
    NONE, CANNOT_OPEN_INV, CANNOT_GET_VALUE
  }

  public boolean SBFailed() {
    return !this.enabled && this.sbFail != SBFail.NONE;
  }
  //</editor-fold>

  private boolean hasTimerEnded() {
    return this.timer.isScheduled() && this.timer.passed();
  }
}
