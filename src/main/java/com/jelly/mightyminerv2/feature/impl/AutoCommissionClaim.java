package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.Commission;
import com.jelly.mightyminerv2.util.CommissionUtil;
import com.jelly.mightyminerv2.util.EntityUtil;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.InventoryUtil.ClickMode;
import com.jelly.mightyminerv2.util.InventoryUtil.ClickType;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration.RotationType;
import com.jelly.mightyminerv2.util.helper.Target;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AutoCommissionClaim extends AbstractFeature {

    private static AutoCommissionClaim instance;
    private State state = State.STARTING;
    private ClaimError claimError = ClaimError.NONE;
    private Optional<EntityPlayer> emissary = Optional.empty();
    @Getter
    private List<Commission> nextComm = new ArrayList<>();
    private int retry = 0;
    private int commClaimMethod = 0;

    public static AutoCommissionClaim getInstance() {
        if (instance == null) {
            instance = new AutoCommissionClaim();
        }

        return instance;
    }

    @Override
    public String getName() {
        return "AutoCommissionClaim";
    }

    @Override
    public void start() {
        commClaimMethod = MightyMinerConfig.commClaimMethod;
        if (MightyMinerConfig.macroType == 1) {
            commClaimMethod = 1;        // Always use Royal Pigeon for Glacial Macro
        }
        this.enabled = true;
        this.nextComm = null;
        this.claimError = ClaimError.NONE;
    }

    @Override
    public void stop() {
        if (!this.enabled) {
            return;
        }

        this.enabled = false;
        this.emissary = Optional.empty();
        this.timer.reset();
        this.resetStatesAfterStop();
        send("AutoCommissionClaim Stopped");
    }

    @Override
    public void resetStatesAfterStop() {
        this.state = State.STARTING;
        this.retry = 0;
    }

    @Override
    public boolean shouldNotCheckForFailsafe() {
        return true;
    }

    public void stop(ClaimError error) {
        this.claimError = error;
        this.stop();
    }

    public boolean succeeded() {
        return !this.enabled && this.claimError == ClaimError.NONE;
    }

    public ClaimError claimError() {
        return this.claimError;
    }

    @SubscribeEvent
    protected void onTick(final ClientTickEvent event) {
        if (!this.enabled) {
            return;
        }

        if (this.retry > 3) {
            log("Tried too many times but failed. stopping");
            this.stop(ClaimError.INACCESSIBLE_NPC);
            return;
        }

        switch (this.state) {
            case STARTING:
                int time = 400;
                switch (commClaimMethod) {
                    case 0:
                        time = 0;
                        break;
                    case 1:
                        if (!InventoryUtil.holdItem("Royal Pigeon")) {
                            this.stop(ClaimError.NO_ITEMS);
                            break;
                        }
                        break;
                }
                this.swapState(State.ROTATING, time);
                break;
            case ROTATING:
                if (this.isTimerRunning()) {
                    return;
                }
                if (commClaimMethod == 0) {
                    this.emissary = CommissionUtil.getClosestEmissary();

                    if (this.emissary.isPresent()) {
                        EntityPlayer emissaryEntity = this.emissary.get();
                        log("Found Emissary: " + emissaryEntity.getName());

                        if (mc.thePlayer.getDistanceSqToEntity(emissaryEntity) > 16) {
                            log("Emissary " + emissaryEntity.getName() + " is too far away.");
                            sendError("Emissary is too far away.");
                            this.stop(ClaimError.INACCESSIBLE_NPC);
                            return;
                        } else {
                            log("Rotating to Emissary: " + emissaryEntity.getName());
                            RotationHandler.getInstance().easeTo(new RotationConfiguration(new Target(emissaryEntity), 500, RotationType.CLIENT, null));
                        }
                    } else {
                        log("Could not find nearby Emissary. Current position: " + mc.thePlayer.getPositionVector());
                        this.stop(ClaimError.NPC_NOT_UNLOCKED);
                        return;
                    }
                }
                this.swapState(State.SWAPPING_TO_ALT, 1000);
                break;

            case SWAPPING_TO_ALT:
                if (this.isTimerRunning()) {
                    return;
                }

                if(MightyMinerConfig.commSwapBeforeClaiming) {
                    if (!InventoryUtil.holdItem(MightyMinerConfig.altMiningTool)) {
                        this.stop(ClaimError.NO_ITEMS);
                        sendError("Cannot hold Alt Mining Tool: " + MightyMinerConfig.altMiningTool);
                        break;
                    }
                } else {
                    this.swapState(State.OPENING, 1000);

                }
                this.swapState(State.OPENING, 0);

                break;
            case OPENING:
                final Optional<Entity> entityLookingAt = EntityUtil.getEntityLookingAt();
                time = 5000;
                switch (commClaimMethod) {
                    case 0:
                        if (RotationHandler.getInstance().isEnabled() || !entityLookingAt.isPresent()) {
                            return;
                        }

                        // because why not
                        if (entityLookingAt.equals(this.emissary)) {
                            KeyBindUtil.leftClick();
                        } else {
                            mc.playerController.interactWithEntitySendPacket(mc.thePlayer, this.emissary.get());
                        }
                        break;
                    case 1:
                        KeyBindUtil.rightClick();
                }

                log("Scheduler timer for : " + time);
                this.swapState(State.VERIFYING_GUI, time);
                break;
            case VERIFYING_GUI:
                if (this.hasTimerEnded()) {
                    this.stop(ClaimError.INACCESSIBLE_NPC);
                    sendError("Opened a Different Inventory Named: " + InventoryUtil.getInventoryName());
                    break;
                }
                switch (commClaimMethod) {
                    case 0:
                    case 1:
                        if (!(mc.thePlayer.openContainer instanceof ContainerChest) || !InventoryUtil.getInventoryName().contains("Commissions")) {
                            break;
                        }
                        this.swapState(State.CLAIMING, 500);
                        break;
                }
                break;
            case CLAIMING:
                if (this.isTimerRunning()) {
                    break;
                }
                final int slotToClick = CommissionUtil.getClaimableCommissionSlot();
                State nextState;
                if (slotToClick != -1) {
                    InventoryUtil.clickContainerSlot(slotToClick, ClickType.LEFT, ClickMode.PICKUP);
                    nextState = State.CLAIMING;
                } else {
                    log("No Commission To Claim");
                    nextState = State.NEXT_COMM;
                }
                this.swapState(nextState, MightyMinerConfig.getRandomGuiWaitDelay());
                break;
            case NEXT_COMM:
                if (this.isTimerRunning()) {
                    break;
                }



                if (mc.thePlayer.openContainer instanceof ContainerChest) {
                    this.nextComm = CommissionUtil.getCommissionFromContainer((ContainerChest) mc.thePlayer.openContainer);
                }
                this.swapState(State.ENDING, 1000);
                break;

            case ENDING:
                if (this.isTimerRunning()) {
                    return;
                }
                InventoryUtil.closeScreen();

                if(MightyMinerConfig.commSwapBeforeClaiming) {
                    this.swapState(State.SWAPPING_BACK, 1000);
                    break;
                }
                this.stop();
                break;
            case SWAPPING_BACK:
                if (this.isTimerRunning()) {
                    return;
                }
                if(MightyMinerConfig.commSwapBeforeClaiming) {
                    if (!InventoryUtil.holdItem(MightyMinerConfig.miningTool)) {
                        this.stop(ClaimError.NO_ITEMS);
                        sendError("Cannot hold Mining Tool: " + MightyMinerConfig.miningTool);
                        break;
                    }
                }
                this.stop();
                break;
        }
    }

    @SubscribeEvent
    protected void onChat(ClientChatReceivedEvent event) {
        if (!this.enabled || this.state != State.CLAIMING || event.type != 0) {
            return;
        }

        String mess = event.message.getUnformattedText();
        if (mess.startsWith("This ability is on cooldown for ")) {
            this.retry++;
            log("Pigeon Cooldown Detected, Waiting for 5 Seconds");
            this.swapState(State.OPENING, 5000);
        }
    }

    private void swapState(final State state, final int time) {
        this.state = state;
        if (time == 0) {
            this.timer.reset();
        } else {
            this.timer.schedule(time);
        }
    }

    enum State {
        STARTING, ROTATING, SWAPPING_TO_ALT, OPENING, VERIFYING_GUI, CLAIMING, NEXT_COMM, SWAPPING_BACK, ENDING
    }

    public enum ClaimError {
        NONE, INACCESSIBLE_NPC, NO_ITEMS, TIMEOUT, NPC_NOT_UNLOCKED
    }
}
