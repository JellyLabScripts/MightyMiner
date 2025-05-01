package com.jelly.mightyminerv2.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.jelly.mightyminerv2.handler.GameStateHandler;
import com.jelly.mightyminerv2.util.ScoreboardUtil;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;

import java.util.List;

public class DebugHUD extends TextHud {

    @Getter
    private final static DebugHUD instance = new DebugHUD();
    private final transient Minecraft mc = Minecraft.getMinecraft();

    public DebugHUD() {
        super(
                true,
                1f,
                10f,
                0.8f,
                true,
                true,
                1,
                5,
                5,
                new OneColor(0, 0, 0, 150),
                false,
                2,
                new OneColor(0, 0, 0, 127)
        );
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {

        lines.add("§6§lScoreboard");
        lines.add("§7Title: §f" + ScoreboardUtil.getScoreboardTitle());
        lines.addAll(ScoreboardUtil.getScoreboard());
        lines.add("Custom Cold: " + ScoreboardUtil.cold);
        lines.add("");

        lines.add("§6§lPlayer Location");
        lines.add("§7Current Location: §f" + GameStateHandler.getInstance().getCurrentLocation().getName());
        lines.add("§7Sub Location: §f" + GameStateHandler.getInstance().getCurrentSubLocation().getName());
        lines.add("");

        // Add display and game state information
        lines.add("§6§lDisplay & Game State");
        lines.add("§7In-Game Focus: §f" + mc.inGameHasFocus);
        lines.add("§7Display Active: §f" + Display.isActive());
    }

    /*
    public void addList(String featureName, DebugList list) {
        this.lists.put(featureName, list);
    }

    public void clearList() {
        this.lists.clear();
    }

    public class DebugList implements Iterable<String> {
        List<String> debugLines = new ArrayList<>();
        int current = 0;

        public DebugList(String... lines) {
            debugLines.addAll(Arrays.asList(lines));
        }

        public DebugList append(String line) {
            debugLines.add(line);
            return this;
        }

        public DebugList append(int index, String line) {
            debugLines.add(index, line);
            return this;
        }

        public DebugList remove(int index) {
            debugLines.remove(index);
            return this;
        }

        @NotNull
        @Override
        public Iterator<String> iterator() {
            return debugLines.listIterator();
        }

        @Override
        public void forEach(Consumer<? super String> action) {
            Iterable.super.forEach(action);
        }
    }
    */
}
