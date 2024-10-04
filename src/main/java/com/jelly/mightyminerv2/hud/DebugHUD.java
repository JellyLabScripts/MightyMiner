package com.jelly.mightyminerv2.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.jelly.mightyminerv2.handler.GameStateHandler;
import com.jelly.mightyminerv2.util.ScoreboardUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.Display;

public class DebugHUD extends TextHud {

  private transient static DebugHUD instance = new DebugHUD();

  public static DebugHUD getInstance() {
    return instance;
  }

  private transient Minecraft mc = Minecraft.getMinecraft();

//  public Map<String, DebugList> lists = new HashMap<>();
  public int count = 0;

  public DebugHUD() {
    super(true, 1f, 10f, 0.5f, true, true, 1, 5, 5, new OneColor(0, 0, 0, 150), false, 2, new OneColor(0, 0, 0, 127));
  }

  @Override
  protected void getLines(List<String> lines, boolean example) {
//    lists.forEach((a, b) -> {
//      lines.add(a);
//      b.forEach(it -> lines.add("    " + it));
//    });
    lines.add(ScoreboardUtil.getScoreboardTitle());
    lines.addAll(ScoreboardUtil.getScoreboard());
    lines.add("");
    lines.add("Location: " + GameStateHandler.getInstance().getCurrentLocation().getName());
    lines.add("SubLocation: " + GameStateHandler.getInstance().getCurrentSubLocation().getName());
    lines.add("inGameHasFocus: " + mc.inGameHasFocus);
    lines.add("displayIsActive: " + Display.isActive());
//    lines.add("");
//    lines.add("EntityCount: " + count);
//    lines.add("LeftClickDown: " + mc.gameSettings.keyBindAttack.isPressed());
//    lines.add("IsUsingItems: " + mc.thePlayer.isUsingItem());
  }

//  public void addList(String featureName, DebugList list) {
//    this.lists.put(featureName, list);
//  }
//
//  public void clearList() {
//    this.lists.clear();
//  }
//
//  public class DebugList implements Iterable<String> {
//
//    List<String> debugLines = new ArrayList<>();
//    int current = 0;
//
//    public DebugList(String... lines) {
//      debugLines.addAll(Arrays.asList(lines));
//    }
//
//    public DebugList append(String line) {
//      debugLines.add(line);
//      return this;
//    }
//
//    public DebugList append(int index, String line) {
//      debugLines.add(index, line);
//      return this;
//    }
//
//    public DebugList remove(int index) {
//      debugLines.remove(index);
//      return this;
//    }
//
//    @NotNull
//    @Override
//    public Iterator<String> iterator() {
//      return debugLines.listIterator();
//    }
//
//    @Override
//    public void forEach(Consumer<? super String> action) {
//      Iterable.super.forEach(action);
//    }
//  }
}
