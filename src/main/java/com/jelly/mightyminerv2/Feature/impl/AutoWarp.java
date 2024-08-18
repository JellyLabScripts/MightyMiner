package com.jelly.mightyminerv2.Feature.impl;

import com.google.common.collect.ImmutableSet;
import com.jelly.mightyminerv2.Feature.IFeature;
import com.jelly.mightyminerv2.Handler.GameStateHandler;
import com.jelly.mightyminerv2.Util.helper.Clock;
import com.jelly.mightyminerv2.Util.helper.location.Location;
import com.jelly.mightyminerv2.Util.helper.location.SubLocation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class AutoWarp implements IFeature {

  private static AutoWarp instance = new AutoWarp();

  public static AutoWarp getInstance() {
    return instance;
  }

  private final Minecraft mc = Minecraft.getMinecraft();
  private boolean enabled = false;
  private boolean failed = false;
  private boolean succeeded = false;
  private Error failReason = Error.NONE;
  private Clock timer = new Clock();

  private int attempts = 0;
  private boolean nosbError = false; // Not On SkyBlock Error
  private Location targetLocation = null;
  private SubLocation targetSubLocation = null;

  // Message
  private final Set<String> waitMessages = ImmutableSet.of(
      "Couldn't warp you! Try again later.",
      "Cannot join SkyBlock for a moment!",
      "You are sending commands too fast! Please slow down.",
      "You were kicked while joining that server!",
      "You tried to rejoin too fast, please try again in a moment."
  );
  private final String playerNotOnSkyBlock = "Oops! You are not on SkyBlock so we couldn't warp you!";
  private final String noWarpScroll = "You haven't unlocked this fast travel destination!";

  @Override
  public String getName() {
    return "AutoWarp";
  }

  @Override
  public boolean isEnabled() {
    return true;
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

  }

  @Override
  public void resetStatesAfterStop() {

  }

  @Override
  public boolean shouldCheckForFailsafe() {
    return false;
  }


  public void enable(Location targetLocation, SubLocation targetSubLocation) {
    if ((targetLocation != null && !LOCATION_WARP_COMMANDS.containsKey(targetLocation)) || (targetSubLocation != null && !SUBLOCATION_WARP_COMMANDS.containsKey(targetSubLocation))) {
      error("Warp Scroll for " + targetLocation + " or " + targetSubLocation + " does not exist.");
      this.failed = true;
      this.succeeded = false;
      return;
    }
    this.targetLocation = targetLocation;
    this.targetSubLocation = targetSubLocation;
    this.failed = this.succeeded = false;
    this.failReason = Error.NONE;
    this.enabled = true;

    log("Enabled");
  }

  public void disable() {
    this.enabled = false;
    this.attempts = 0;
    this.targetLocation = null;
    this.targetSubLocation = null;
    this.nosbError = false;

    log("Disabled");
  }

  public void disable(boolean failed, Error failReason){
    this.failed = failed;
    this.succeeded = !failed;
    this.failReason = failReason;
    this.disable();
  }

  public boolean hasFailed(){
    return !this.enabled && this.failed;
  }

  public boolean hasSucceeded(){
    return !this.enabled && this.succeeded;
  }

  public Error getFailReason(){
    return this.failReason;
  }

  @SubscribeEvent
  public void onTick(ClientTickEvent event) {
    if (mc.thePlayer == null || mc.theWorld == null || !this.enabled) {
      return;
    }

    if (this.attempts > 10) {
      this.disable(true, Error.FAILED_TO_WARP);

      log("Failed to Auto Warp.");
      return;
    }

    if (this.isDoneWarping()) {
      this.disable();

      log("Done Warping");
      return;
    }

    if (this.timer.isScheduled() && !this.timer.passed()) {
      return;
    }

    this.attempts++;
    this.timer.schedule(5000); // 5s between each rewrap make customizable or change
    Location currentLocation = GameStateHandler.getInstance().getCurrentLocation();
    SubLocation currentSubLocation = GameStateHandler.getInstance().getCurrentSubLocation();

    if (this.nosbError) {
      this.nosbError = false;
      sendCommand(getWarpCommand(Location.LOBBY));

      log("Not On SkyBlock error");
      return;
    }

    if (!GameStateHandler.getInstance().isPlayerInSkyBlock()) {
      if (currentLocation == Location.LIMBO) {
        sendCommand(getWarpCommand(Location.LOBBY));
      } else {
        sendCommand("/play sb");
      }

      log("Player is not on skyblock.");
      return;
    }

    // dont need to warp if im warping to a sub location
    if (this.targetLocation != null && this.targetSubLocation == null && this.targetLocation != currentLocation) {
      String warpCommand = getWarpCommand(this.targetLocation);
      sendCommand(warpCommand);

      log("Player not at island. Sending Command: " + warpCommand);
      return;
    }

    if (this.targetSubLocation != null && this.targetSubLocation != currentSubLocation) {
      String warpCommand = getWarpCommand(this.targetSubLocation);
      sendCommand(warpCommand);

      log("Player not at SubLocation. Sending: " + warpCommand);
      return;
    }

    this.disable(true, Error.FAILED_TO_WARP);
  }

  @SubscribeEvent
  public void onChat(ClientChatReceivedEvent event) {
    if (!this.enabled || event.type != 0) {
      return;
    }

    String message = event.message.getUnformattedText();
    if (this.waitMessages.contains(message)) {
      this.timer.schedule(10000); // Wait time
    }
    if (message.contains(this.playerNotOnSkyBlock)) {
      this.nosbError = true;
    }
    if (message.contains(this.noWarpScroll)) {
      this.disable(true, Error.NO_SCROLL);
      error("Please consume the " + this.targetLocation.getName() + " and " + this.targetSubLocation.getName() + " travel scrolls.");
    }
  }

  public boolean isDoneWarping() {
    Location currentIsland = GameStateHandler.getInstance().getCurrentLocation();
    SubLocation currentSubLocation = GameStateHandler.getInstance().getCurrentSubLocation();

    return (this.targetLocation == null || currentIsland == this.targetLocation) &&
        (this.targetSubLocation == null || currentSubLocation == this.targetSubLocation);
  }

  private void sendCommand(String command) {
    mc.thePlayer.sendChatMessage(command);
  }

  private String getWarpCommand(Location location) {
    String command = LOCATION_WARP_COMMANDS.get(location);
    if (command.startsWith("/")) {
      return command;
    }
    return "/warp " + command;
  }

  private String getWarpCommand(SubLocation subLocation) {
    return "/warp " + SUBLOCATION_WARP_COMMANDS.get(subLocation);
  }

  private final Map<Location, String> LOCATION_WARP_COMMANDS = new HashMap<Location, String>() {{
    put(Location.PRIVATE_ISLAND, "island");
    put(Location.HUB, "hub");
    put(Location.THE_PARK, "park");
    put(Location.THE_FARMING_ISLANDS, "barn");
    put(Location.SPIDER_DEN, "spider");
    put(Location.THE_END, "end");
    put(Location.CRIMSON_ISLE, "isle");
    put(Location.GOLD_MINE, "gold");
    put(Location.DEEP_CAVERNS, "deep");
    put(Location.DWARVEN_MINES, "mines");
    put(Location.CRYSTAL_HOLLOWS, "ch");
    put(Location.JERRY_WORKSHOP, "/savethejerrys");
    put(Location.DUNGEON_HUB, "dhub");
    put(Location.LOBBY, "/l");
    put(Location.GARDEN, "garden");
  }};

  private final Map<SubLocation, String> SUBLOCATION_WARP_COMMANDS = new HashMap<SubLocation, String>() {{
    put(SubLocation.MUSEUM, "museum");
//    put(SubLocation.SIRIUS_SHACK, "da");
    put(SubLocation.RUINS, "castle");
    put(SubLocation.MUSHROOM_DESERT, "desert");
    put(SubLocation.TRAPPERS_DEN, "trapper");
    put(SubLocation.HOWLING_CAVE, "howl");
    put(SubLocation.JUNGLE_ISLAND, "jungle");
    put(SubLocation.THE_FORGE, "forge");
    put(SubLocation.CRYSTAL_NUCLEUS, "nucleus");
    put(SubLocation.SPIDER_MOUND, "top");
    put(SubLocation.ARACHNES_SANCTUARY, "arachne");
    put(SubLocation.DRAGONS_NEST, "drag");
    put(SubLocation.VOID_SEPULTURE, "void");
    put(SubLocation.FORGOTTEN_SKULL, "skull");
    put(SubLocation.SMOLDERING_TOMB, "tomb");
    put(SubLocation.THE_WASTELAND, "wasteland");
    put(SubLocation.DRAGONTAIL, "dragontail");
    put(SubLocation.SCARLETON, "scarleton");
  }};

  public enum Error{
    NONE,
    FAILED_TO_WARP,
    NO_SCROLL,
  }
}
