package com.jelly.mightyminerv2.failsafe;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.event.BlockChangeEvent;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe.Failsafe;
import com.jelly.mightyminerv2.failsafe.impl.*;
import com.jelly.mightyminerv2.feature.FeatureManager;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.StrafeUtil;
import com.jelly.mightyminerv2.util.helper.AudioManager;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.*;

public class FailsafeManager {

    private static FailsafeManager instance;
    public final List<AbstractFailsafe> failsafes = new ArrayList<>();
    public final Queue<AbstractFailsafe> emergencyQueue = new PriorityQueue<>(Comparator.comparing(AbstractFailsafe::getPriority));
    private final Minecraft mc = Minecraft.getMinecraft();
    public Optional<AbstractFailsafe> triggeredFailsafe = Optional.empty();
    public Set<Failsafe> failsafesToIgnore = new HashSet<>();
    private final Clock timer = new Clock();

    // TODO: Implement all failsafe later!
    public FailsafeManager() {
        this.failsafes.addAll(Arrays.asList(
            DisconnectFailsafe.getInstance(),
            KnockbackFailsafe.getInstance(),
            WorldChangeFailsafe.getInstance(),
            ProfileFailsafe.getInstance(),
            ItemChangeFailsafe.getInstance(),
            NameMentionFailsafe.getInstance()
        ));
    }

    public static FailsafeManager getInstance() {
        if (instance == null) {
            instance = new FailsafeManager();
        }
        return instance;
    }

    public void stopFailsafes() {
        triggeredFailsafe = Optional.empty();
        emergencyQueue.clear();
    }

    public boolean shouldNotCheckForFailsafe() {
        return !MacroManager.getInstance().isRunning() || this.triggeredFailsafe.isPresent();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTick(ClientTickEvent event) {
        if (this.shouldNotCheckForFailsafe()) {
            return;
        }

        List<AbstractFailsafe> failsafeCopy = new ArrayList<>(failsafes);
        this.failsafesToIgnore.clear();
        this.failsafesToIgnore.addAll(FeatureManager.getInstance().getFailsafesToIgnore());

        for (AbstractFailsafe failsafe : failsafeCopy) {
            if (!this.failsafesToIgnore.contains(failsafe.getFailsafeType()) && failsafe.onTick(event)) {
                this.emergencyQueue.add(failsafe);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockChange(BlockChangeEvent event) {
        if (this.shouldNotCheckForFailsafe()) {
            return;
        }

        failsafes.forEach(failsafe -> {
            if (!this.failsafesToIgnore.contains(failsafe.getFailsafeType()) && failsafe.onBlockChange(event)) {
                this.emergencyQueue.add(failsafe);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketReceive(PacketEvent.Received event) {
        if (this.shouldNotCheckForFailsafe()) {
            return;
        }

        failsafes.forEach(failsafe -> {
            if (!this.failsafesToIgnore.contains(failsafe.getFailsafeType()) && failsafe.onPacketReceive(event)) {
                this.emergencyQueue.add(failsafe);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ClientChatReceivedEvent event) {
        if (this.shouldNotCheckForFailsafe()) {
            return;
        }

        failsafes.forEach(failsafe -> {
            if (!this.failsafesToIgnore.contains(failsafe.getFailsafeType()) && failsafe.onChat(event)) {
                this.emergencyQueue.add(failsafe);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldUnload(WorldEvent.Unload event) {
        if (this.shouldNotCheckForFailsafe()) {
            return;
        }

        failsafes.forEach(failsafe -> {
            if (!this.failsafesToIgnore.contains(failsafe.getFailsafeType()) && failsafe.onWorldUnload(event)) {
                this.emergencyQueue.add(failsafe);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        if (this.shouldNotCheckForFailsafe()) {
            return;
        }

        failsafes.forEach(failsafe -> {
            if (!this.failsafesToIgnore.contains(failsafe.getFailsafeType()) && failsafe.onDisconnect(event)) {
                this.emergencyQueue.add(failsafe);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(GuiOpenEvent event) {
        if (this.shouldNotCheckForFailsafe()) {
            return;
        }

        for (AbstractFailsafe failsafe : this.failsafes) {
            if (!this.failsafesToIgnore.contains(failsafe.getFailsafeType()) && failsafe.onGuiOpen(event)) {
                this.emergencyQueue.add(failsafe);
            }
        }
    }

    public void removeFailsafeFromQueue(AbstractFailsafe failsafe) {
        boolean removed = emergencyQueue.remove(failsafe);
        if (removed) {
            System.out.println("Successfully removed failsafe: " + failsafe.getFailsafeType());
        } else {
            System.out.println("Failsafe not found in the queue: " + failsafe.getFailsafeType());
        }
    }

    @SubscribeEvent
    public void onTickChooseEmergency(ClientTickEvent event) {
        if (this.shouldNotCheckForFailsafe()) {
            return;
        }
        if (this.triggeredFailsafe.isPresent()) {
            return;
        }
        if (this.emergencyQueue.isEmpty()) {
            return;
        }

        StrafeUtil.forceStop = true;
        if (!this.timer.isScheduled()) {
            this.timer.schedule(MightyMinerConfig.failsafeToggleDelay);
        } else if (this.timer.passed()) {
            AudioManager.getInstance().playSound();
            this.triggeredFailsafe = Optional.ofNullable(this.emergencyQueue.peek());
            this.emergencyQueue.clear();
            this.timer.reset();
        }
    }

    @SubscribeEvent
    public void onTickReact(ClientTickEvent event) {
        if (!this.triggeredFailsafe.isPresent()) {
            return;
        }

        // make a reset method
        if (this.triggeredFailsafe.get().react()) {
            StrafeUtil.forceStop = false;
            this.triggeredFailsafe = Optional.empty();
            this.emergencyQueue.clear();
            this.failsafes.forEach(AbstractFailsafe::resetStates);
        }
    }

    public boolean isFailsafeActive(Failsafe failsafe) {
        System.out.println("failsafequeue: " + this.emergencyQueue);
        return this.emergencyQueue.stream().anyMatch(it -> it.getFailsafeType().equals(failsafe));
    }
}
