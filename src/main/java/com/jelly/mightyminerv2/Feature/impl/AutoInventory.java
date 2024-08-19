package com.jelly.mightyminerv2.Feature.impl;

import com.jelly.mightyminerv2.Config.MightyMinerConfig;
import com.jelly.mightyminerv2.Feature.IFeature;
import com.jelly.mightyminerv2.Feature.impl.MithrilMiner.BoostState;
import com.jelly.mightyminerv2.Util.InventoryUtil;
import com.jelly.mightyminerv2.Util.InventoryUtil.ClickMode;
import com.jelly.mightyminerv2.Util.InventoryUtil.ClickType;
import com.jelly.mightyminerv2.Util.helper.Clock;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kotlin.Pair;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerChest;
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
    this.moveState = MoveState.STARTING;
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

    switch (this.mainTask) {
      case NONE:
        this.stop();
        break;
      case GET_SPEED_BOOST:
        this.handleGetSpeedBoost();
        break;
      case MOVE_ITEMS_TO_HOTBAR:
        this.handleMoveItems();
        break;
    }
  }

  enum Task {
    NONE, GET_SPEED_BOOST, MOVE_ITEMS_TO_HOTBAR
  }

  //<editor-fold desc="Get Mining Speed And Mining Speed Boost (Gemstone Later)">
  private int[] speedBoostValues = new int[2]; // [Mining Speed, Mining Speed Boost]
  private SB sbState = SB.STARTING;
  private SBError sbError = SBError.NONE;

  public void retrieveSpeedBoost() {
    this.mainTask = Task.GET_SPEED_BOOST;
    this.sbError = SBError.NONE;
    this.speedBoostValues = new int[2];

    this.enabled = true;
  }

  public int[] getSpeedBoostValues() {
    return this.speedBoostValues;
  }

  public boolean sbSucceeded() {
    return !this.enabled && this.sbError == SBError.NONE;
  }

  public SBError getSbError() {
    return this.sbError;
  }

  private void swapSbState(final SB state, final int time) {
    this.sbState = state;
    this.timer.schedule(time);
  }

  private void handleGetSpeedBoost() {
    switch (this.sbState) {
      case STARTING:
        this.swapSbState(SB.OPEN_MENU, MightyMinerConfig.getRandomGuiWaitDelay());
        break;
      case OPEN_MENU:
        mc.thePlayer.sendChatMessage("/sbmenu");
        this.swapSbState(SB.GET_SPEED, 2000);
        break;
      case GET_SPEED:
        if (this.hasTimerEnded()) {
          this.stop();
          this.sbError = SBError.CANNOT_GET_VALUE;
          error("Could Not Get Speed In Time.");
          break;
        }

        if (!(mc.thePlayer.openContainer instanceof ContainerChest)
            || !InventoryUtil.getInventoryName().equals("SkyBlock Menu")
            || !InventoryUtil.isInventoryLoaded()) {
          break;
        }

        List<String> loreList = InventoryUtil.getLoreOfItemInContainer(InventoryUtil.getSlotIdOfItemInContainer("Your SkyBlock Profile"));
        for (String lore : loreList) {
          if (!lore.contains("Mining Speed")) {
            continue;
          }

          try {
            String[] splitValues = lore.replace(",", "").split(" ");
            this.speedBoostValues[0] = Integer.parseInt(splitValues[splitValues.length - 1]);
            this.swapSbState(SB.OPEN_HOTM_MENU, MightyMinerConfig.getRandomGuiWaitDelay());
            return;
          } catch (Exception e) {
            e.printStackTrace();
            log("Couldn't parse value properly.");
          }
        }

        this.stop();
        this.sbError = SBError.CANNOT_GET_VALUE;
        error("Could not get mining speed from tab. Make sure its enabled.");
        break;
      case OPEN_HOTM_MENU:
        if (!this.hasTimerEnded()) {
          break;
        }

        if (InventoryUtil.getInventoryName().equals("SkyBlock Menu")) {
          InventoryUtil.clickContainerSlot(
              InventoryUtil.getSlotIdOfItemInContainer("Heart of the Mountain"),
              ClickType.LEFT,
              ClickMode.PICKUP
          );
        } else {
          log("Menu Name Is NOT SkyBlock Menu");
          mc.thePlayer.sendChatMessage("/hotm"); // should never be a thing
        }

        this.swapSbState(SB.GET_SPEED_BOOST, 2000);
        break;
      case GET_SPEED_BOOST:
        if (this.hasTimerEnded()) {
          this.stop();
          this.sbError = SBError.CANNOT_OPEN_INV;
          error("Could Not Open HOTM Inventory in Time.");
          break;
        }
        if (!(mc.thePlayer.openContainer instanceof ContainerChest)
            || !InventoryUtil.getInventoryName().equals("Heart of the Mountain")
            || !InventoryUtil.isInventoryLoaded()) {
          break;
        }

        final int speedBoostSlot = InventoryUtil.getSlotIdOfItemInContainer("Mining Speed Boost");
        final String speedBoostLore = String.join(" ", InventoryUtil.getLoreOfItemInContainer(speedBoostSlot));

        final Matcher matcher = Pattern.compile("\\+(\\d+)%").matcher(speedBoostLore);
        if (matcher.find()) {
          this.speedBoostValues[1] = Integer.parseInt(matcher.group(1));
          if (MithrilMiner.getBoostState() == BoostState.ACTIVE) {
            this.speedBoostValues[0] /= this.speedBoostValues[1] / 100.0;
          }
          this.swapSbState(SB.END, 500);
          break;
        }

        this.stop();
        this.sbError = SBError.CANNOT_GET_VALUE;
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

  enum SB {
    STARTING, OPEN_MENU, GET_SPEED, OPEN_HOTM_MENU, GET_SPEED_BOOST, END
  }

  public enum SBError {
    NONE, CANNOT_OPEN_INV, CANNOT_GET_VALUE
  }
  //</editor-fold>

  //<editor-fold desc="Move Specific Items To Hotbar">
  private MoveState moveState = MoveState.STARTING;
  private MoveError moveError = MoveError.NONE;
  private Queue<String> elementsToSwap = new LinkedList<>();
  private Queue<Integer> availableSlots = new LinkedList<>();

  public void moveItems(List<String> items) {
    if (items.isEmpty()) {
      return;
    }

    this.availableSlots.clear();
    this.elementsToSwap.clear();

    Pair<List<Integer>, List<String>> itemsToMove = InventoryUtil.getAvailableHotbarSlots(items);
    this.availableSlots.addAll(itemsToMove.getFirst());
    this.elementsToSwap.addAll(itemsToMove.getSecond());

    if (this.elementsToSwap.isEmpty()) {
      log("No Elements to move");
      return;
    }

    if (this.availableSlots.size() < this.elementsToSwap.size()) {
      error("Not enough slots to move items to. Disabling"); // should never happen
      this.moveError = MoveError.NOT_ENOUGH_HOTBAR_SPACE;
      return;
    }

    this.mainTask = Task.MOVE_ITEMS_TO_HOTBAR;
    this.moveError = MoveError.NONE;
    this.enabled = true;

    log("Started moving items into hotbar");
  }

  private void changeMoveState(MoveState to, int time) {
    this.moveState = to;
    this.timer.schedule(time);
  }

  public boolean moveFailed() {
    return !this.enabled && this.moveError != MoveError.NONE;
  }

  public MoveError getMoveError() {
    return this.moveError;
  }

  private void handleMoveItems() {
    if (this.timer.isScheduled() && !this.timer.passed()) {
      return;
    }

    switch (this.moveState) {
      case STARTING:
        InventoryUtil.openInventory();
        this.changeMoveState(MoveState.SWAP_SLOTS, MightyMinerConfig.getRandomGuiWaitDelay());

        log("Opened Inventory");
        break;
      case SWAP_SLOTS:
        if (!(this.elementsToSwap.isEmpty() || this.availableSlots.isEmpty())) {
          InventoryUtil.swapSlots(InventoryUtil.getSlotIdOfItemInContainer(this.elementsToSwap.poll()), this.availableSlots.poll());
          this.changeMoveState(MoveState.SWAP_SLOTS, MightyMinerConfig.getRandomGuiWaitDelay());
        } else {
          this.changeMoveState(MoveState.FINISH, 0);
        }

        log("Swapped Item");
        break;
      case FINISH:
        InventoryUtil.closeScreen();

        if (!this.elementsToSwap.isEmpty()) {
          this.moveError = MoveError.NOT_ENOUGH_HOTBAR_SPACE;
        }
        this.stop();
        log("Closed");
        break;
    }
  }

  enum MoveState {
    STARTING, SWAP_SLOTS, FINISH
  }

  public enum MoveError {
    NONE, NOT_ENOUGH_HOTBAR_SPACE
  }
  //</editor-fold>

  private boolean hasTimerEnded() {
    return this.timer.isScheduled() && this.timer.passed();
  }
}
