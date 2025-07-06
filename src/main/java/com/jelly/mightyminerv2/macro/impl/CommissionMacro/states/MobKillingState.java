package com.jelly.mightyminerv2.macro.impl.CommissionMacro.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.AutoMobKiller.AutoMobKiller;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.Commission;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;
import com.jelly.mightyminerv2.util.CommissionUtil;

import java.util.Set;

public class MobKillingState implements CommissionMacroState {

    @Override
    public void onStart(CommissionMacro macro) {
        log("Starting mob killing state");
        Set<String> mobName = CommissionUtil.getMobForCommission(macro.getCurrentCommission());

        if (mobName == null) {
            logError("Current commission: " + macro.getCurrentCommission());
            macro.disable("Mob name not found! Please send the logs to the developer ");
            return;
        }

        AutoMobKiller.getInstance().start(mobName, macro.getCurrentCommission().getName().startsWith("Glacite") ?
                MightyMinerConfig.miningTool : MightyMinerConfig.slayerWeapon);
    }

    @Override
    public CommissionMacroState onTick(CommissionMacro macro) {
        if (macro.getCurrentCommission() == Commission.COMMISSION_CLAIM){
            return new PathingState();
        }

        if (AutoMobKiller.getInstance().isRunning() || AutoMobKiller.getInstance().succeeded()) {
            return this;
        }

        switch (AutoMobKiller.getInstance().getError()) {
            case NONE:
                macro.disable("Mob killer failed, but no error is detected. Please contact the developer.");
                break;
            case NO_ENTITIES:
                log("No entities found in Mob Killer. Restarting");
                return new StartingState();
        }

        return null;
    }

    @Override
    public void onEnd(CommissionMacro macro) {
        AutoMobKiller.getInstance().stop();
        log("Ending mob killing state");
    }
}
