package com.jelly.mightyminerv2.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardUtil {

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
}
