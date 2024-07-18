package com.jelly.MightyMinerV2.Handler;

import com.jelly.MightyMinerV2.Config.MightyMinerConfig;
import com.jelly.MightyMinerV2.Feature.impl.MithrilMiner;
import com.jelly.MightyMinerV2.Util.LogUtil;
import net.minecraft.client.Minecraft;

public class MacroHandler {
    private static final Minecraft client = Minecraft.getMinecraft();  // Forge 1.8.9 uses Minecraft.getMinecraft()
    private boolean isMacroActive = false;  // Track the macro's active state

    public void handleMacro() {
        switch (MightyMinerConfig.macroType) {
            case 0:
                if (!MithrilMiner.getInstance().isRunning()) {
                    int[] p = new int[]{1, 1, 1, 1};
                    if (MightyMinerConfig.mithrilMinerTitaniumHighPriority) {
                        LogUtil.send("Tita", LogUtil.ELogType.SUCCESS);
                        p[3] = 10;
                    }
                    MithrilMiner.getInstance().enable(p);
                } else {
                    MithrilMiner.getInstance().stop();
                }
                break;

            case 1:
                // Implement functionality for macroType 1
                break;

            case 2:
                // Implement functionality for macroType 2
                break;

            default:
                throw new IllegalArgumentException("Unexpected macroType: " + MightyMinerConfig.macroType);
        }
    }

    public void toggleMacro() {
        isMacroActive = !isMacroActive;  // Toggle the macro state
        if (isMacroActive) {
            handleMacro();  // Start or toggle the macro
        } else {
            MithrilMiner.getInstance().stop();  // Stop the macro if it's active
        }
    }

    public boolean isMacroActive() {
        return isMacroActive;
    }
}
