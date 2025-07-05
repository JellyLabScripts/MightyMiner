package com.jelly.mightyminerv2.hud;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.handler.GameStateHandler;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class CommissionHUD extends TextHud {

    @Getter
    private final static CommissionHUD instance = new CommissionHUD();

    private final long updateIntervalMs = 1000; // update every 1 sec
    private long lastUpdateTime = 0;
    private int animatedXPBarProgress = 0;
    private int targetXPBarProgress = 0;

    private final List<String> cachedLines = Collections.synchronizedList(new ArrayList<>());

    private final transient CommissionMacro commissionMacro = CommissionMacro.getInstance();

    @Switch(name = "Reset Stats When Disabled")
    public boolean commHudResetStats = false;

    public CommissionHUD() {
        super(
            true,                // enabled
            5f,                  // x
            5f,                  // y
            1.0f,                // scale
            true,                // editable
            true,                // movable
            2,                   // padding
            8,                   // lineSpacing
            6,                   // textHeight
            new OneColor(0, 0, 0, 150),    // background color
            true,                          // background enabled
            0.5F,                          // border width
            new OneColor(80, 80, 240, 150) // border color
        );
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastUpdateTime >= updateIntervalMs || example) {
            lastUpdateTime = currentTime;

            cachedLines.clear();

            long uptime = commissionMacro.uptime.getTimePassed() / 1000;
            int totalComms = commissionMacro.getCommissionCounter();
            long hotmExpGained = totalComms * 400L;
            int commsPerHour = (uptime > 0) ? (int) ((float) totalComms / uptime * 3600) : 0;

            int xpPerLevel = 4000;
            int barLength = 10;
            long xpThisLevel = hotmExpGained % xpPerLevel;
            targetXPBarProgress = (int) (((float) xpThisLevel / xpPerLevel) * barLength);

            int animationStep = 1;
            if (animatedXPBarProgress < targetXPBarProgress) {
                animatedXPBarProgress = Math.min(animatedXPBarProgress + animationStep, targetXPBarProgress);
            } else if (animatedXPBarProgress > targetXPBarProgress) {
                animatedXPBarProgress = Math.max(animatedXPBarProgress - animationStep, targetXPBarProgress);
            }

            StringBuilder xpBar = new StringBuilder("§8[");
            for (int i = 0; i < barLength; i++) {
                xpBar.append(i < animatedXPBarProgress ? "§a■" : "§7■");
            }
            xpBar.append("§8]");

            cachedLines.add("§9§l⛏️ Dwarven Commission Macro");
            cachedLines.add("§8§m----------------------");
            cachedLines.add("§8» §7🕒 Runtime: §b" + formatElapsedTime(uptime));
            cachedLines.add("§8» §7📜 Commissions: §3" + totalComms);
            cachedLines.add("§8» §7📈 Comm/hour: §3" + commsPerHour);
            cachedLines.add("§8» §7✨ HOTM XP: §5" + formatNumberWithK(hotmExpGained));
            cachedLines.add("§8» §7🔥 HOTM XP/hr: §5" + formatNumberWithK(commsPerHour * 400L));
            cachedLines.add("§8» §7Progress: " + xpBar);
            cachedLines.add("§8§m----------------------");

            if (commissionMacro.isEnabled()) {
                cachedLines.add("§8» §7🧭 Current:");
                cachedLines.add("§8» §f" + commissionMacro.getCurrentCommission().getName());
                cachedLines.add("§8§m----------------------");
            }

            cachedLines.add("§9MightyMiner v" + MightyMiner.instance.VERSION);
        }

        lines.addAll(cachedLines);
    }

    private String formatNumberWithK(long number) {
        if (number >= 1000) {
            double divided = number / 1000.0;
            if (divided % 1 == 0) return String.format("%.0fk", divided);
            else if ((divided * 10) % 1 == 0) return String.format("%.1fk", divided);
            else return String.format("%.2fk", divided);
        }
        return String.valueOf(number);
    }

    private String formatElapsedTime(long elapsedSeconds) {
        long seconds = elapsedSeconds % 60;
        long minutes = (elapsedSeconds / 60) % 60;
        long hours = (elapsedSeconds / 3600) % 24;
        long days = elapsedSeconds / 86400;

        StringBuilder formatted = new StringBuilder();
        if (days > 0) formatted.append(days).append("d");
        if (hours > 0 || days > 0) formatted.append(hours).append("h");
        if (minutes > 0 || hours > 0 || days > 0) formatted.append(minutes).append("m");
        formatted.append(seconds).append("s");

        return formatted + (!commissionMacro.isEnabled() ? " §7(Paused)" : "");
    }

    @Override
    protected boolean shouldShow() {
        if (!super.shouldShow()) return false;
        boolean macroTypeOK = (MightyMinerConfig.macroType == 0);
        boolean locationOK = GameStateHandler.getInstance().inDwarvenMines()
            || MightyMinerConfig.showDwarvenCommHUDOutside;
        return macroTypeOK && locationOK;
    }

    public List<String> getCachedLines() {
        return cachedLines;
    }
}
