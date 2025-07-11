package com.jelly.mightyminerv2.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.handler.GameStateHandler;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.GlacialMacro;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.GlaciteVeins;
import com.jelly.mightyminerv2.util.TablistUtil;
import com.jelly.mightyminerv2.util.helper.location.SubLocation;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class GlacialCommissionHUD extends TextHud {

    @Getter
    private static final GlacialCommissionHUD instance = new GlacialCommissionHUD();
    private final transient GlacialMacro glacialMacro = GlacialMacro.getInstance();

    public GlacialCommissionHUD() {
        super(true, 5f, 5f, 1.0f, true, true, 2, 8, 6,
                new OneColor(0, 0, 0, 150),
                true, 0.5F, new OneColor(0, 150, 255, 150));
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        long uptime = glacialMacro.uptime.getTimePassed() / 1000;
        int totalComms = glacialMacro.getCommissionCounter();
        long hotmExpGained = (long) totalComms * 750;

        int commsPerHour = 0;
        if (uptime > 0) {
            commsPerHour = (int) ((float) totalComms / uptime * 3600);
        }

        lines.add("§b§lGlacial Commission Macro");
        lines.add("§8§m------------------------");
        lines.add("§8» §7Runtime: §b" + formatElapsedTime(uptime));
        lines.add("§8» §7Commissions completed: §3" + totalComms);
        lines.add("§8» §7Commissions/hour: §3" + commsPerHour);
        lines.add("§8» §7HOTM XP gained: §d" + formatNumberWithK(hotmExpGained));
        lines.add("§8» §7HOTM XP/hour: §d" + formatNumberWithK((long) commsPerHour * 900));
        lines.add("§8§m------------------------");

        if (glacialMacro.isEnabled() && glacialMacro.getCurrentState() != null) {
            lines.add("§8» §7Status: §e" + glacialMacro.getCurrentState().getClass().getSimpleName());

            Map<GlaciteVeins, Double> commPercentages = TablistUtil.getGlaciteComs();
            List<GlaciteVeins> typesToMine = glacialMacro.getTypeToMine();

            if (!typesToMine.isEmpty()) {
                lines.add("§8» §7Commission Info:");
                for (GlaciteVeins veinType : typesToMine) {
                    double percentage = commPercentages.getOrDefault(veinType, 0.0);

                    // Calculate available (non-blacklisted) veins
                    int totalVeins = GlaciteVeins.getVeins(veinType).length;
                    long blacklistedCount = glacialMacro.getPreviousVeins().keySet().stream()
                            .filter(pair -> pair.first() == veinType)
                            .count();
                    long availableCount = totalVeins - blacklistedCount;

                    String line = String.format("   §f- §b%s: §a%d/%d §7(§e%.1f%%§7)", veinType.toString(), availableCount, totalVeins, percentage);
                    lines.add(line);
                }
            }
        }
        lines.add("§bMightyMiner v" + MightyMiner.instance.VERSION);
    }

    private String formatNumberWithK(long number) {
        if (number >= 1000) {
            double dividedNumber = number / 1000.0;
            if (dividedNumber == (long) dividedNumber) {
                return String.format("%dk", (long) dividedNumber);
            } else {
                return String.format("%.1fk", dividedNumber).replace(".0", "");
            }
        }
        return String.valueOf(number);
    }

    private String formatElapsedTime(long elapsedTimeSeconds) {
        long seconds = elapsedTimeSeconds % 60;
        long minutes = (elapsedTimeSeconds / 60) % 60;
        long hours = (elapsedTimeSeconds / 3600);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds) + (!glacialMacro.isEnabled() ? " §7(Paused)" : "");
    }

    @Override
    protected boolean shouldShow() {
        if (!super.shouldShow()) return false;
        boolean macroTypeCondition = (MightyMinerConfig.macroType == 1);
        SubLocation currentSub = GameStateHandler.getInstance().getCurrentSubLocation();
        boolean locationCondition = currentSub == SubLocation.GLACITE_TUNNELS || currentSub == SubLocation.DWARVEN_BASE_CAMP || MightyMinerConfig.showGlacialHUDOutside;

        return macroTypeCondition && locationCondition;
    }
}