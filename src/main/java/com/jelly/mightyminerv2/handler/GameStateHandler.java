package com.jelly.mightyminerv2.handler;

import com.jelly.mightyminerv2.event.UpdateScoreboardEvent;
import com.jelly.mightyminerv2.event.UpdateTablistEvent;
import com.jelly.mightyminerv2.event.UpdateTablistFooterEvent;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.ScoreboardUtil;
import com.jelly.mightyminerv2.util.helper.location.Location;
import com.jelly.mightyminerv2.util.helper.location.SubLocation;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameStateHandler {

    @Getter
    private static final GameStateHandler instance = new GameStateHandler();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Pattern areaPattern = Pattern.compile("Area:\\s(.+)");
    @Getter
    private String serverIp = "";
    @Getter
    private Location currentLocation = Location.KNOWHERE;
    @Getter
    private SubLocation currentSubLocation = SubLocation.KNOWHERE;
    @Getter
    private boolean godpotActive = false;
    @Getter
    private boolean cookieActive = false;

    public boolean isPlayerInSkyBlock() {
        return this.currentLocation.ordinal() < Location.values().length - 3;
    }

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
        final List<String> scoreboard = ScoreboardUtil.getScoreboard();

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

        if (!ScoreboardUtil.getScoreboardTitle().contains("SKYBLOCK") && !scoreboard.isEmpty() && scoreboard.get(scoreboard.size() - 1).equalsIgnoreCase("www.hypixel.net")) {
            this.currentLocation = Location.LOBBY;
            return;
        }
        this.currentLocation = Location.KNOWHERE;
    }
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
    public void onScoreboardListUpdate(UpdateScoreboardEvent event) {
        for (int i = 0; i < event.scoreboard.size(); i++) {
            final String line = event.scoreboard.get(i);
            if (!(line.contains("⏣") || line.contains("ф"))) {
                continue;
            }

            this.currentSubLocation = SubLocation.fromName(ScoreboardUtil.sanitizeString(line).trim());
            break;
        }
    }
}