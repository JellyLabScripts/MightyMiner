package com.jelly.mightyminerv2.macro.impl.GlacialMacro.states;

import com.jelly.mightyminerv2.feature.impl.AutoCommissionClaim;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.GlacialMacro;
import com.jelly.mightyminerv2.util.helper.Clock;

/**
 * State for claiming completed commissions in the Glacial Macro.
 * This state handles the claiming process, including retries and timeouts.
 */
public class ClaimingCommissionState implements GlacialMacroState {

    private final AutoCommissionClaim claimer = AutoCommissionClaim.getInstance();
    private final Clock timeout = new Clock();
    private int retries = 0;

    @Override
    public void onStart(GlacialMacro macro) {
        log("Claiming completed commission.");
        claimer.start();
        timeout.schedule(10000); // 10s timeout
    }

    @Override
    public GlacialMacroState onTick(GlacialMacro macro) {
        if (claimer.isRunning()) {
            if (timeout.passed()) {
                logError("Claiming commission timed out.");
                return handleFailure(macro);
            }
            return this;
        }

        if (claimer.succeeded()) {
            log("Successfully claimed commission.");
            macro.incrementCommissionCounter();
            macro.updateMiningTasks(); // Update tasks after claiming
            return new PathfindingState();
        } else {
            logError("Failed to claim commission. Reason: " + claimer.claimError());
            return handleFailure(macro);
        }
    }

    private GlacialMacroState handleFailure(GlacialMacro macro) {
        if (++retries > 3) {
            return new ErrorHandlingState("Failed to claim commission after 3 attempts.");
        }
        log("Retrying claim... (" + retries + "/3)");
        onStart(macro); // Restart the claim process
        return this;
    }

    @Override
    public void onEnd(GlacialMacro macro) {
        if (claimer.isRunning()) {
            claimer.stop();
        }
    }
}