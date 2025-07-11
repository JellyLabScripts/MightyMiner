package com.jelly.mightyminerv2.macro.impl.CommissionMacro;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.event.UpdateTablistEvent;
import com.jelly.mightyminerv2.failsafe.impl.NameMentionFailsafe;
import com.jelly.mightyminerv2.feature.FeatureManager;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.hud.CommissionHUD;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.states.CommissionMacroState;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.states.NewLobbyState;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.states.StartingState;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.states.WarpingState;
import com.jelly.mightyminerv2.util.CommissionUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;

public class CommissionMacro extends AbstractMacro {

    @Getter
    private static final CommissionMacro instance = new CommissionMacro();

    private CommissionMacroState currentState;

    @Getter
    private int commissionCounter = 0;

    @Getter
    @Setter
    private int miningSpeed = 0;

    @Getter
    @Setter
    private BlockMiner.PickaxeAbility pickaxeAbility = BlockMiner.PickaxeAbility.NONE;

    @Getter
    @Setter
    private Commission currentCommission;


    @Override
    public String getName() {
        return "Commission Macro";
    }

    @Override
    public void onEnable() {
        currentState = new StartingState();
        log("CommMacro::onEnable");
    }

    @Override
    public void onDisable() {
        if (currentState != null) {
            currentState.onEnd(this);
        }
        this.miningSpeed = 0;
        if (CommissionHUD.getInstance().commHudResetStats) {
            this.commissionCounter = 0;
        }
        log("CommMacro::onDisable");
        FeatureManager.getInstance().disableAll();
    }

    @Override
    public void onPause() {
        FeatureManager.getInstance().pauseAll();
        log("CommMacro::onPause");
    }

    @Override
    public void onResume() {
        FeatureManager.getInstance().resumeAll();
        log("CommMacro::onResume");
    }

    @Override
    public List<String> getNecessaryItems() {
        List<String> items = new ArrayList<>();
        items.add(MightyMinerConfig.miningTool);
        items.add(MightyMinerConfig.slayerWeapon);

        if (MightyMinerConfig.commClaimMethod == 1) {
            items.add("Royal Pigeon");
        }

        if (MightyMinerConfig.drillRefuel) {
            items.add("Abiphone");
        }
        return items;
    }

    public void onTick(ClientTickEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        if (NameMentionFailsafe.getInstance().isLobbyChangeRequested()) {
            log("Name mention detected inside CommissionMacro onTick, changing lobbies");
            NameMentionFailsafe.getInstance().resetStates();
            transitionTo(new NewLobbyState());
        }

        if (this.isTimerRunning()) {
            return;
        }

        if (currentState == null)
            return;

        CommissionMacroState nextState = currentState.onTick(this);
        transitionTo(nextState);
    }


    private void transitionTo(CommissionMacroState nextState){
        // Skip if no state change
        if (currentState == nextState)
            return;

        currentState.onEnd(this);
        currentState = nextState;

        if (currentState == null) {
            log("null state, returning");
            return;
        }

        currentState.onStart(this);
    }

    @Override
    public void onChat(String message) {
        if (!this.isEnabled()) {
            return;
        }

        if (message.contains("Commission Complete")) {
            this.commissionCounter++;
            log("Commission Complete Detected");
        }
    }

    @Override
    public void onTablistUpdate(UpdateTablistEvent event) {
        if (!this.isEnabled() || currentState instanceof WarpingState || currentState instanceof NewLobbyState) {
            return;
        }

        List<Commission> comms = CommissionUtil.getCurrentCommissionsFromTablist();
        if (comms.isEmpty()) {
            log("Cannot find commissions!");
            return;
        }
        setCurrentCommission(comms.get(0));
    }
}
