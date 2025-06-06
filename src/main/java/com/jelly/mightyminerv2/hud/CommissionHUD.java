package com.jelly.mightyminerv2.hud;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.handler.GameStateHandler;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;
import lombok.Getter;

import java.util.List;

public class CommissionHUD extends TextHud {

    @Getter
    private final static CommissionHUD instance = new CommissionHUD();
    private final transient CommissionMacro commissionMacro = CommissionMacro.getInstance();
    @Switch(
            name = "Reset Stats When Disabled"
    )
    public boolean commHudResetStats = false;

    public CommissionHUD() {
        super(true,
                5f,
                5f,
                1.0f,
                true,
                true,
                2,
                8,
                6,
                new OneColor(0, 0, 0, 150),
                true,
                0.5F,
                new OneColor(80, 80, 240, 150));
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        // Get data from the CommissionMacro instance
        long uptime = commissionMacro.uptime.getTimePassed() / 1000;
        int totalComms = commissionMacro.getCommissionCounter();
        long hotmExpGained = (long) totalComms * 400;

        // Calculate commissions per hour
        int commsPerHour = 0;
        if (uptime > 0) {
            commsPerHour = (int) ((float) totalComms / uptime * 3600);
        }

        lines.add("§9§lDwarven Commission Macro");
        lines.add("§8§m---------------");
        lines.add("§8» §7Runtime: §b" + formatElapsedTime(uptime));
        lines.add("§8» §7Commissions completed: §3" + totalComms);
        lines.add("§8» §7Commissions/hour: §3" + commsPerHour);
        lines.add("§8» §7HOTM XP gained: §5" + formatNumberWithK(hotmExpGained));
        lines.add("§8» §7HOTM XP/hour: §5" + formatNumberWithK(commsPerHour * 400L));
        lines.add("§8§m---------------");
        if (commissionMacro.isEnabled()) {
            lines.add("§8» §7Current Commission:");
            lines.add("§8» §f" + commissionMacro.getCurrentCommission().getName());
            lines.add("§8§m---------------");
        }
        lines.add("§9MightyMiner v" + MightyMiner.instance.VERSION);
    }

    // Helper function to format large numbers with "k" for thousands
    private String formatNumberWithK(long number) {
        if (number >= 1000) {
            double dividedNumber = number / 1000.0;
            if (dividedNumber % 1 == 0) {
                return String.format("%.0fk", dividedNumber);
            } else if (dividedNumber * 10 % 1 == 0) {
                return String.format("%.1fk", dividedNumber);
            } else {
                return String.format("%.2fk", dividedNumber);
            }
        }
        return String.valueOf(number);
    }

    // Helper function to format time into a shortened format, ex: 2h54m22s, 5m54s
    private String formatElapsedTime(long elapsedTimeSeconds) {
        long seconds = elapsedTimeSeconds % 60;
        long minutes = (elapsedTimeSeconds / 60) % 60;
        long hours = (elapsedTimeSeconds / 3600) % 24;
        long days = elapsedTimeSeconds / 86400;

        StringBuilder formattedTime = new StringBuilder();

        // should never happen, because banned if you macro so long :(
        if (days > 0) {
            formattedTime.append(days).append("d");
        }
        if (hours > 0 || days > 0) {
            formattedTime.append(hours).append("h");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            formattedTime.append(minutes).append("m");
        }
        formattedTime.append(seconds).append("s");

        return formattedTime + (!commissionMacro.isEnabled() ? " §7(Paused)" : "");
    }

    @Override
    protected boolean shouldShow() {
        if (!super.shouldShow()) return false;
        boolean macroTypeCondition = (MightyMinerConfig.macroType == 0);
        boolean locationCondition = GameStateHandler.getInstance().inDwarvenMines() || MightyMinerConfig.showDwarvenCommHUDOutside;

        return macroTypeCondition && locationCondition;
    }
}
