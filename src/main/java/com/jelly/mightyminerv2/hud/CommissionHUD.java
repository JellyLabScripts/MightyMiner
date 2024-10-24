package com.jelly.mightyminerv2.hud;

import cc.polyfrost.oneconfig.config.annotations.Switch;
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

  @Switch(
      name = "Reset Stats When Disabled"
  )
  public boolean commHudResetStats = false;

  public CommissionHUD() {
    super(true, 1f, 10f, 1.0f, true, true, 1, 5, 5, new OneColor(0, 0, 0, 150), false, 2, new OneColor(0, 0, 0, 127));
  }

  private final transient CommissionMacro macro = CommissionMacro.getInstance();

  @Override
  protected void getLines(List<String> lines, boolean example) {
    long uptime = macro.uptime.getTimePassed() / 1000;
    int totalComms = macro.getCompletedCommissions();
    int commsPerHour = 0;
    if (uptime > 0) {
      commsPerHour = (int) ((float) totalComms / uptime * 3600);
    }

    lines.add("§6§lCommission Macro Stats");
    lines.add("§7Runtime: §f" + formatElapsedTime(uptime));
    lines.add("§7Total Commissions: §f" + totalComms);
    lines.add("§7HOTM Exp Gained: §f" + totalComms * 400);
    lines.add("§7Commissions per Hour: §f" + commsPerHour);
    lines.add("§7HOTM Exp/H: §f" + commsPerHour * 400);
    if (macro.isEnabled()) {
      lines.add("§7Active Commissions:");
      macro.getActiveCommissions().forEach(it -> {
        lines.add("  §f" + it.getName());
      });
    }
  }

  private String formatElapsedTime(long elapsedTimeSeconds) {
    long seconds = elapsedTimeSeconds % 60;
    long minutes = (elapsedTimeSeconds / 60) % 60;
    long hours = (elapsedTimeSeconds / 3600);

    return String.format("%02d:%02d:%02d%s", hours, minutes, seconds, (!macro.isEnabled() ? " (Paused)" : ""));
  }
}
