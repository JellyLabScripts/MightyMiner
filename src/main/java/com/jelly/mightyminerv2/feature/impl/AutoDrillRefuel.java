package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.util.EntityUtil;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.InventoryUtil.ClickMode;
import com.jelly.mightyminerv2.util.InventoryUtil.ClickType;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.Target;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class AutoDrillRefuel extends AbstractFeature {

  private static final AutoDrillRefuel instance = new AutoDrillRefuel();

  public static AutoDrillRefuel getInstance() {
    return instance;
  }

  private String drillToRefill = "";
  private int fuelIndex = -1;
  private boolean getFuelFromSack = false;
  private boolean useNpc = false;
  private Entity npc = null;
  private State state = State.STARTING;
  private Error failReason = Error.NONE;
  private int requiredFuelAmount = 0;
  private boolean movedSuccessfully = false;
  private final String[] fuelList = {"Enchanted Poppy", "Goblin Egg", "Green Goblin Egg", "Yellow Goblin Egg", "Red Goblin Egg", "Blue Goblin Egg", "Volta", "Oil Barrel"};
  private final Map<String, Integer> fuelAmount = new HashMap<String, Integer>() {{
    put("Enchanted Poppy", 1000);
    put("Goblin Egg", 3000);
    put("Green Goblin Egg", 3000);
    put("Yellow Goblin Egg", 3000);
    put("Red Goblin Egg", 3000);
    put("Blue Goblin Egg", 3000);
    put("Volta", 3000);
    put("Oil Barrel", 3000);
  }};

  @Override
  public String getName() {
    return "AutoDrillRefuel";
  }

  @Override
  public boolean shouldNotCheckForFailsafe() {
    return true;
  }

  @Override
  public void resetStatesAfterStop() {
    this.state = State.STARTING;
  }

  public void start(String drillName, int fuelIndex, boolean getFuelFromSack, boolean useNpc) {
    if (InventoryUtil.getSlotIdOfItemInContainer(drillName) == -1) {
      error("Can't refill because no drill - dumbass");
      this.stop(Error.FAILED_REFILL);
      return;
    }
    this.failReason = Error.NONE;
    this.drillToRefill = drillName;
    this.fuelIndex = fuelIndex;
    this.getFuelFromSack = getFuelFromSack;
    this.useNpc = useNpc;
    this.enabled = true;

    log("Started refuel for " + drillToRefill + " with " + this.fuelList[fuelIndex]);
  }

  public void stop() {
    this.enabled = false;
    this.drillToRefill = "";
    this.fuelIndex = -1;
    this.getFuelFromSack = false;
    this.useNpc = false;
    this.npc = null;
    this.requiredFuelAmount = 0;
    this.movedSuccessfully = false;

    this.resetStatesAfterStop();
  }

  public void stop(Error failReason) {
    note("Stopped because " + failReason);
    this.failReason = failReason;
    this.stop();
  }

  public void swapState(State to, int time) {
    this.state = to;
    if (time == 0) {
      this.timer.reset();
    } else {
      this.timer.schedule(time);
    }
  }

  public boolean hasSucceeded() {
    return this.failReason == Error.NONE;
  }

  public Error getFailReason() {
    return this.failReason;
  }

  @SubscribeEvent
  public void onTick(ClientTickEvent event) {
    if (!this.enabled) {
      return;
    }

    switch (this.state) {
      case STARTING:
        this.swapState(State.LOCATING_ITEMS, 0);
        break;
      case LOCATING_ITEMS:
        List<String> loreList = InventoryUtil.getItemLoreFromInventory(this.drillToRefill);
        if (loreList.isEmpty()) {
          error("Can't refill because no drill - dumbass");
          this.stop(Error.FAILED_REFILL);
          return;
        }
        boolean foundFuel = false;
        float capacity = 0;
        for (String lore : loreList) {
          if (!lore.startsWith("Fuel: ")) {
            continue;
          }
          foundFuel = true;
          try {
            capacity = Float.parseFloat(lore.split("/")[1].replace("k", "000"));
          } catch (Exception e) {
            Logger.sendNote("Could not retrieve fuel capacity. Lore: " + lore + ", Splitted: " + Arrays.toString(lore.split("/")));
            e.printStackTrace();
          }
          if (capacity == 0.0f) {
            error("Couldnt find drill capacity");
            this.stop(Error.FAILED_REFILL);
            return;
          }

          break;
        }
        if (!foundFuel) {
          error("Couldnt find drill fuel");
          this.stop(Error.FAILED_REFILL);
          return;
        }

        String chosenFuel = this.fuelList[this.fuelIndex];
        this.requiredFuelAmount = MathHelper.ceiling_float_int(capacity / this.fuelAmount.get(chosenFuel));
        if (InventoryUtil.getAmountOfItemInInventory(chosenFuel) >= this.requiredFuelAmount) {
          this.swapState(State.ROTATING, 0);
        } else {
          this.swapState(State.FETCHING_ITEMS, 0);
        }
        break;
      case FETCHING_ITEMS:
        int time = 2000;
        if (this.getFuelFromSack) {
          mc.thePlayer.sendChatMessage("/gfs " + this.fuelList[this.fuelIndex] + " " + this.requiredFuelAmount);
        } else {
          // Toggle AutoBazaar
          time = 0;
        }
        this.swapState(State.VERIFYING_ITEM_FETCH, time);
        break;
      case VERIFYING_ITEM_FETCH:
        if (this.getFuelFromSack) {
          if (this.hasTimerEnded()) {
            error("Couldnt Get Fuel From Sack in time");
            this.stop(Error.FAILED_REFILL);
            break;
          }
          if (!this.movedSuccessfully) {
            break;
          }
          this.swapState(State.ROTATING, 0);
//          if (InventoryUtil.getAmountOfItemInInventory(this.fuelList[this.fuelIndex]) >= this.requiredFuelAmount) {
//            this.swapState(State.ROTATING, 0);
//          } else {
//            error("Not enough fuel in inventory. amount in inventory: " + InventoryUtil.getAmountOfItemInInventory(this.fuelList[this.fuelIndex]));
//            this.stop(Error.FAILED_REFILL);
//            break;
//          }
        } else {
          // verify auto bazaar
          this.swapState(State.ROTATING, 0);
        }
        break;
      case ROTATING:
        if (this.useNpc) {
          this.npc = mc.theWorld.playerEntities.stream()
              .filter(entity -> entity.posX == -6.5 && entity.posY == 145 && entity.posZ == -18.5
                  && !entity.getName().contains("Sentry") // Just Because; It should never happen
                  && EntityUtil.isNpc(entity))
              .findFirst().orElse(null);

          if (this.npc == null || mc.thePlayer.getDistanceSqToEntity(this.npc) > 9) {
            this.stop(Error.INACCESSIBLE_MECHANIC);
            error("Cannot find NPC");
            break;
          }

          RotationHandler.getInstance().easeTo(new RotationConfiguration(new Target(this.npc), 300, null));
        } else {
          // Toggle AutoAbiphone
        }
        this.swapState(State.OPENING_MECHANICS_GUI, 0);
        break;
      case OPENING_MECHANICS_GUI:
        time = 2000;
        if (this.useNpc) {
          Optional<Entity> entityLookingAt = EntityUtil.getEntityLookingAt();

          if (RotationHandler.getInstance().isEnabled() && !entityLookingAt.isPresent()) {
            return;
          }

          if (entityLookingAt.isPresent() && entityLookingAt.get().equals(this.npc)) {
            KeyBindUtil.leftClick();
          } else {
            mc.playerController.interactWithEntitySendPacket(mc.thePlayer, this.npc);
          }
        } else {
          // Toggle AutoAbiphone
          time = 0;
        }
        this.swapState(State.GUI_VERIFY, time);
        break;
      case GUI_VERIFY:
        if (this.useNpc) {
          if (this.hasTimerEnded()) {
            this.stop(Error.INACCESSIBLE_MECHANIC);
            break;
          }
        } else {
          // Verify AutoAbiphone
        }

        if (InventoryUtil.getInventoryName().contains("Drill Anvil") && InventoryUtil.isInventoryLoaded()) {
          log("Opened Anvil GUI");
          this.swapState(State.PUTTING_ITEMS, 500);
        }
        break;
      case PUTTING_ITEMS:
        if (!this.hasTimerEnded()) {
          break;
        }

        log("IN PUTTING ITEMS");

        int lowerChestSize = ((ContainerChest) mc.thePlayer.openContainer).getLowerChestInventory().getSizeInventory();
        int slotToClick;
        if (((slotToClick = InventoryUtil.getSlotIdOfItemInContainer(this.drillToRefill)) != -1 && slotToClick >= lowerChestSize)
            || ((slotToClick = InventoryUtil.getSlotIdOfItemInContainer(this.fuelList[this.fuelIndex])) != -1 && slotToClick >= lowerChestSize)) {
          InventoryUtil.clickContainerSlot(slotToClick, ClickType.LEFT, ClickMode.QUICK_MOVE);
          log("Put item");
          this.swapState(State.PUTTING_ITEMS, 500);
        } else {
          this.swapState(State.RETRIEVING_DRILL, 500);
        }
        break;
      case RETRIEVING_DRILL:
        if (!this.hasTimerEnded()) {
          break;
        }

        log("retrieving drill");

        int drillSlot = InventoryUtil.getSlotIdOfItemInContainer(this.drillToRefill);
        log("drill slot: " + drillSlot);
        if (drillSlot == -1) {
          error("No drill in inventory for some reason - should never happen but if it does then :thumbsupcat:");
          this.stop(Error.FAILED_REFILL);
          break;
        }

        lowerChestSize = ((ContainerChest) mc.thePlayer.openContainer).getLowerChestInventory().getSizeInventory();
        log("lowerChestSize: " + lowerChestSize);
        if (drillSlot < lowerChestSize) {
          log("drillslot < lowerchestsize. retrieving");
          InventoryUtil.clickContainerSlot(InventoryUtil.getSlotIdOfItemInContainer("Drill Anvil"), ClickType.LEFT, ClickMode.PICKUP);
          this.swapState(State.RETRIEVING_DRILL, 500);
        } else {
          log("ending");
          this.swapState(State.ENDING, 300);
        }
        break;
      case ENDING:
        if (!this.hasTimerEnded()) {
          return;
        }

        InventoryUtil.closeScreen();
        this.stop();
        log("Succeeded or failed");
        break;
    }
  }

  @SubscribeEvent
  public void onChat(ClientChatReceivedEvent event) {
    if (!this.enabled || !this.getFuelFromSack || event.type != 0) {
      return;
    }

    String message = event.message.getUnformattedText();
    if (message.equals("Moved " + this.requiredFuelAmount + " " + this.fuelList[this.fuelIndex] + " from your Sacks to your inventory.")) {
      this.movedSuccessfully = true;
    }
  }

  enum State {
    STARTING, LOCATING_ITEMS, FETCHING_ITEMS, VERIFYING_ITEM_FETCH, ROTATING, OPENING_MECHANICS_GUI, GUI_VERIFY, PUTTING_ITEMS, RETRIEVING_DRILL, ENDING
  }

  public enum Error {
    NONE,
    INACCESSIBLE_MECHANIC,
    FAILED_REFILL
  }
}
