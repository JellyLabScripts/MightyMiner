package com.jelly.mightyminerv2.hud;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.jelly.mightyminerv2.MightyMiner;
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
        // Constructor: Sets up the TextHud properties
        // Parameters:
        // - shadow: true to enable shadow
        // - paddingX: 5f horizontal padding
        // - paddingY: 5f vertical padding
        // - scale: 1.0f text scale
        // - renderBg: true to render background
        // - renderOutline: true to render outline
        // - outlineThickness: 2 outline thickness
        // - bgPaddingX: 8 background horizontal padding
        // - bgPaddingY: 8 background vertical padding
        // - bgColor: new OneColor(0, 0, 0, 200) black with alpha 200 background color
        // - renderBorder: true to render border
        // - borderThickness: 2 border thickness
        // - borderColor: new OneColor(0, 255, 0, 255) bright green border color
        super(true,
                5f,
                5f,
                1.0f,
                true,
                true,
                2,
                8,
                8,
                new OneColor(0, 0, 0, 200),
                true,
                2,
                new OneColor(0, 255, 0, 255));
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

        // Title (Centered, bright green, single line)
        lines.add("  §a§lDwarven Commission Macro");
        // Separator (Darker gray, solid line)
        lines.add("§8§m------------------------");
        // Session Info (Centered, bright green with brighter green solid line and added spaces)
        lines.add("§a§m---- §a  SESSION INFO §a§m----");
        lines.add("  §f§a✦ §fRuntime: §a" + formatElapsedTime(uptime));
        lines.add("  §f§a✔ §fCommissions completed: §a" + totalComms);
        lines.add("  §f§a✔ §fCommissions/hour: §a" + commsPerHour);
        lines.add("  §f§a⚒ §fHOTM XP gained: §a" + formatNumberWithK(hotmExpGained));
        lines.add("  §f§a⚒ §fHOTM XP/hour: §a" + formatNumberWithK(commsPerHour * 400L));
        // Separator (Darker gray, solid line)
        lines.add("§8§m------------------------");
        if (commissionMacro.isEnabled()) {
            // Mining Info (Centered, bright green with brighter green solid line and added spaces)
            lines.add("§a§m---- §a  MINING INFO §a§m----"); // Brighter green separator
            lines.add("  §f§a✰ §fTarget Commission:");
            lines.add("  §f" + commissionMacro.getCurrentCommission().getName());
            // Version (Bright green)
            lines.add(""); // Spacing
        }
        lines.add("§aMightyMiner v" + MightyMiner.instance.VERSION);
    }

    // Helper function to format large numbers with "k" for thousands
    private String formatNumberWithK(long number) {
        if (number >= 1000) {
            return String.format("%.2fk", number / 1000.0);
        }
        return String.valueOf(number);
    }

    // Helper function to format elapsed time (HH:MM:SS) with pause indicator
    private String formatElapsedTime(long elapsedTimeSeconds) {
        long seconds = elapsedTimeSeconds % 60;
        long minutes = (elapsedTimeSeconds / 60) % 60;
        long hours = (elapsedTimeSeconds / 3600);

        return String.format("%02d:%02d:%02d%s",
                hours,
                minutes,
                seconds,
                (!commissionMacro.isEnabled() ? " §7(Paused)" : "")
        );
    }
}
