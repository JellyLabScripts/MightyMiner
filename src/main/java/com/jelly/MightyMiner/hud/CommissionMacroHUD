package com.jelly.MightyMiner.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.jelly.MightyMiner.features.MobKiller;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.macros.CommissionMacro;

import java.util.Arrays;
import java.util.List;

public class CommissionMacroHUD extends TextHud {
    public CommissionMacroHUD() {
        super(true, 0, 0, 1.0f, true, true, 10, 8, 8, new OneColor(0, 0, 0, 150), true, 2, new OneColor(0, 0, 0, 240));
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        if (example) {
            String[] exampleLines = new String[]{
                    "§l Commission Macro:",
                    "§rCommissions done: §f" + "0",
                    "§rCommission per Hour: §f" + "0/h",
                    "§rCurrent Commission: §f" + "None",
            };
            lines.addAll(Arrays.asList(exampleLines));
        } else if (MacroHandler.macros.get(4).isEnabled()) {
            String[] commissionMacroLines = CommissionMacro.drawInfo();
            lines.addAll(Arrays.asList(commissionMacroLines));
        }
    }
}
