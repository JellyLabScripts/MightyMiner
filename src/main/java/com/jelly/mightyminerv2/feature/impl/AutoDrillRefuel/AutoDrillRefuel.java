package com.jelly.mightyminerv2.feature.impl.AutoDrillRefuel;

import com.jelly.mightyminerv2.failsafe.AbstractFailsafe.Failsafe;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.feature.impl.AutoDrillRefuel.states.AutoDrillRefuelState;
import com.jelly.mightyminerv2.feature.impl.AutoDrillRefuel.states.StartingState;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class AutoDrillRefuel extends AbstractFeature {
    @Getter
    private static final AutoDrillRefuel instance = new AutoDrillRefuel();

    public enum AutoDrillRefuelError {
        NONE,
        NO_DRILL,
        NO_FUEL,
        NO_ABIPHONE,
        NO_GREATFORGE_CONTACT
    }
    @Setter
    @Getter
    private AutoDrillRefuelError error = AutoDrillRefuelError.NONE;

    public enum FuelType{
        VOLTA ("Volta"),
        OIL_BARREL ("Oil Barrel");

        @Getter
        private final String name;

        FuelType(String name) {
            this.name = name;
        }
    }
    @Getter
    private FuelType fuelType;

    @Getter
    private String drillName;

    private AutoDrillRefuelState currentState;

    @Override
    public String getName() {
        return "AutoDrillRefuel";
    }

    @Override
    public void resetStatesAfterStop() {
        this.failsafesToIgnore.remove(Failsafe.ITEM_CHANGE);
    }

    public void start(String drillName, FuelType fuelType) {
        this.enabled = true;
        this.drillName = drillName;
        this.fuelType = fuelType;
        this.error = AutoDrillRefuelError.NONE;
        currentState = new StartingState();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (!this.enabled || event.phase == TickEvent.Phase.END) {
            return;
        }

        if (currentState == null)
            return;

        AutoDrillRefuelState nextState = currentState.onTick(this);
        transitionTo(nextState);
    }

    private void transitionTo(AutoDrillRefuelState nextState){
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

}
