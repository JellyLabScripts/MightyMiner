package com.jelly.MightyMinerV2.Failsafe;

import com.jelly.MightyMinerV2.Event.BlockChangeEvent;
import com.jelly.MightyMinerV2.Util.helper.Clock;
import jdk.nashorn.internal.objects.annotations.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FailsafeManager {
    private final Minecraft mc = Minecraft.getMinecraft();
    private static FailsafeManager instance;

    public static FailsafeManager getInstance() {
        if (instance == null) {
            instance = new FailsafeManager();
        }
        return instance;
    }

    public List<Failsafe> failsafes = new ArrayList<>();
    public Optional<Failsafe> triggeredFailsafe = Optional.empty();
    public final ArrayList<Failsafe> emergencyQueue = new ArrayList<>();
    public final Clock DelayChooseFailsafe = new Clock();
    private final Clock onTickDelay = new Clock();
    private final Clock restartMacroAfterFailsafeDelay = new Clock();

    private boolean sendingFailsafeInfo = false;
    private boolean hadEmergency = false;

    public FailsafeManager() {
        DelayChooseFailsafe.schedule(1000);
        onTickDelay.schedule(1000);
        restartMacroAfterFailsafeDelay.schedule(1000);
    }

    public void stopFailsafes() {
        triggeredFailsafe = Optional.empty();
        emergencyQueue.clear();
        sendingFailsafeInfo = false;
        hadEmergency = false;
        DelayChooseFailsafe.reset();
        onTickDelay.reset();
//        failsafes.forEach(Failsafe::);
    }

    public void resetAfterMacroDisable() {
        stopFailsafes();
        restartMacroAfterFailsafeDelay.reset();
    }


//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public void onBlockChange(BlockChangeEvent event) {
//        if (mc.thePlayer == null || mc.theWorld == null) return;
//
//        failsafes.forEach(failsafe -> failsafe.onBlockChange(event));
//    }




}
