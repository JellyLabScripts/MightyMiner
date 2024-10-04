package com.jelly.mightyminerv2.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.jelly.mightyminerv2.macro.commissionmacro.CommissionMacro;
import net.minecraft.client.Minecraft;
import java.util.List;

public class CommissionHUD extends TextHud {

    private transient static CommissionHUD instance = new CommissionHUD();

    public static CommissionHUD getInstance() {
        return instance;
    }

    private transient Minecraft mc = Minecraft.getMinecraft();

    public CommissionHUD() {
        super(true, 1f, 10f, 1.0f, true, true, 1, 5, 5,
                new OneColor(0, 0, 0, 150),
                false, 2, new OneColor(0, 0, 0, 127));
    }

    private double getCommissionsPerHour() {
        CommissionMacro macro = CommissionMacro.getInstance();

        double elapsedTimeHours = (double) macro.getElapsedTimeInSeconds() / 3600.0;

        if (elapsedTimeHours > 0) {
            return (double) macro.commissionCounter / elapsedTimeHours;
        } else {
            return 0;
        }
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        CommissionMacro macro = CommissionMacro.getInstance();

        lines.add("§6§lCommission Macro Stats");
        lines.add("§7Runtime: §f" + formatElapsedTime(macro.getElapsedTimeInSeconds()));
        lines.add("§7Total Commissions: §f" + macro.commissionCounter);
        lines.add("§7Commissions per Hour: §f" + String.format("%.2f", getCommissionsPerHour()));
    }

    private String formatElapsedTime(long elapsedTimeSeconds) {
        long seconds = elapsedTimeSeconds % 60;
        long minutes = (elapsedTimeSeconds / 60) % 60;
        long hours = (elapsedTimeSeconds / 3600);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
