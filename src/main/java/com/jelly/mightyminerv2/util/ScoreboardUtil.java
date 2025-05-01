package com.jelly.mightyminerv2.util;

import com.jelly.mightyminerv2.event.UpdateScoreboardLineEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreboardUtil {

    private final Pattern coldRegex = Pattern.compile("Cold: -?(\\d{1,3})");
    public static int cold = 0;

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static Map<String, SortedMap<Integer, String>> scoreboard = new HashMap<>();
    public static String[] scoreObjNames = new String[19];

    public static List<String> getScoreboard() {
        try {
            return new ArrayList<>(scoreboard.get(scoreObjNames[1]).values());
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public static String getScoreboardTitle() {
        if (mc.theWorld == null) {
            return "";
        }
        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) {
            return "";
        }

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) {
            return "";
        }

        return sanitizeString(objective.getDisplayName());
    }

    public static String sanitizeString(String scoreboard) {
        char[] arr = scoreboard.toCharArray();
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            char c = arr[i];
            if (c >= 32 && c < 127) {
                cleaned.append(c);
            }
            if (c == 167) {
                i++;
            }
        }
        return cleaned.toString();
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        cold = 0;
    }

    @SubscribeEvent
    public void onScoreboardUpdate(UpdateScoreboardLineEvent event) {
        if (event.getLine().contains("Cold:")) {
            Matcher coldMatcher = coldRegex.matcher(event.getLine());
            if (coldMatcher.find()) cold = Integer.parseInt(coldMatcher.group(1));
            else cold = 0;
        }

        List<String> scoreboardLines = getScoreboard();
        if (scoreboardLines.stream().noneMatch(line -> sanitizeString(line).contains("Cold:"))) cold = 0;
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onChatDetection(ClientChatReceivedEvent event) {
        if (event.type != 0) return;
        if (event.message == null) return;

        String message = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (message.contains("The warmth of the campfire reduced your") && message.contains("Cold")) {
            cold = 0;
        }
    }

}
