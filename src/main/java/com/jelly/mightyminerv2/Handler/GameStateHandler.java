package com.jelly.mightyminerv2.Handler;

import com.jelly.mightyminerv2.Event.UpdateScoreboardLineEvent;
import com.jelly.mightyminerv2.Event.UpdateScoreboardListEvent;
import com.jelly.mightyminerv2.Event.UpdateTablistEvent;
import com.jelly.mightyminerv2.Event.UpdateTablistFooterEvent;
import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.Util.InventoryUtil;
import com.jelly.mightyminerv2.Util.ScoreboardUtil;
import com.jelly.mightyminerv2.Util.helper.location.Location;
import com.jelly.mightyminerv2.Util.helper.location.SubLocation;
import com.jelly.mightyminerv2.pathfinder.helper.BlockStateAccessor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class GameStateHandler {

  private static GameStateHandler instance;

  public static GameStateHandler getInstance() {
    if (instance == null) {
      instance = new GameStateHandler();
    }
    return instance;
  }

  private final Minecraft mc = Minecraft.getMinecraft();
  private final Pattern areaPattern = Pattern.compile("Area:\\s(.+)");

  @Getter
  private String serverIp = "";

  // Todo: Do More Testing.
  @Getter
  private Location currentLocation = Location.KNOWHERE;
  @Getter
  private SubLocation currentSubLocation = SubLocation.KNOWHERE;

  @Getter
  private boolean godpotActive = false;
  @Getter
  private boolean cookieActive = false;

//  @SubscribeEvent
//  public void onTick(ClientTickEvent event) {
//    if (mc.theWorld != null) {
//      try {
//        MightyMiner.instance.bsa = new BlockStateAccessor(MightyMiner.instance);
//      } catch (Exception e) {
//        MightyMiner.instance.bsa = null;
//        e.printStackTrace();
//      }
//    }
//  }

  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    currentLocation = Location.KNOWHERE;
    currentSubLocation = SubLocation.KNOWHERE;
  }

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null) {
      this.serverIp = mc.getCurrentServerData().serverIP;
    }
  }

  @SubscribeEvent
  public void onTablistUpdate(UpdateTablistEvent event) {
    if (event.tablist.isEmpty()) {
      return;
    }
    final List<String> tabList = event.tablist;
    final List<String> scoreboard = ScoreboardUtil.getScoreboardLines(true);

    if (tabList.size() == 1 && InventoryUtil.isInventoryEmpty()) {
      this.currentLocation = Location.LIMBO;
      this.currentSubLocation = SubLocation.KNOWHERE;
      return;
    }

    for (String tabline : tabList) {
      if (!tabline.startsWith("Area: ")) {
        continue;
      }
      final Matcher matcher = this.areaPattern.matcher(tabline);
      if (!matcher.find()) {
        return;
      }

      final String area = matcher.group(1);
      this.currentLocation = Location.fromName(area);
      return;
    }

    if (!ScoreboardUtil.getScoreboardTitle().contains("SKYBLOCK")
        && !scoreboard.isEmpty()
        && scoreboard.get(scoreboard.size() - 1).equalsIgnoreCase("www.hypixel.com")) {
      this.currentLocation = Location.LOBBY;
      return;
    }
    this.currentLocation = Location.KNOWHERE;
  }

  // Todo: Consider Changing Logic. Its very simple because i cant test rn.
  //       I doubt it will cause any problems tho
  @SubscribeEvent
  public void onTablistFooterUpdate(UpdateTablistFooterEvent event) {
    final List<String> footer = event.footer;
    for (int i = 0; i < footer.size(); i++) {
      if (footer.get(i).contains("Active Effects")) {
        this.godpotActive = footer.get(++i).contains("You have a God Potion active!");
      }
      if (footer.get(i).contains("Cookie Buff")) {
        this.cookieActive = !footer.get(++i).contains("Not active!");
        break;
      }
    }
  }

  @SubscribeEvent
  public void onScoreboardLineUpdate(UpdateScoreboardLineEvent event) {
    if (!(event.getDirtyLine().contains("⏣") || event.getDirtyLine().contains("ф"))) {
      return;
    }

    this.currentSubLocation = SubLocation.fromName(event.getCleanLine().trim());
  }

  @SubscribeEvent
  public void onScoreboardListUpdate(UpdateScoreboardListEvent event) {
    for (int i = 0; i < event.scoreboardLines.size(); i++) {
      final String line = event.scoreboardLines.get(i);
      if (!(line.contains("⏣") || line.contains("ф"))) {
        continue;
      }

      this.currentSubLocation = SubLocation.fromName(event.cleanScoreboardLines.get(i).trim());
      break;
    }
  }
}