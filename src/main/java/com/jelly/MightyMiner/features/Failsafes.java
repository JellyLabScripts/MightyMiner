package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.events.BlockChangeEvent;
import com.jelly.MightyMiner.events.ReceivePacketEvent;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.macros.macros.CommissionMacro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import com.jelly.MightyMiner.utils.Timer;
import com.jelly.MightyMiner.utils.UngrabUtils;
import com.jelly.MightyMiner.utils.Utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

import static com.jelly.MightyMiner.handlers.MacroHandler.macros;
import static net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST;

public class Failsafes {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private Timer someoneIsCloseTimer;
    private final Timer lastRotationCheck = new Timer();
    private int rotationChecks = 0;
    private static Macro lastMacro;

    private final Timer lastWorldChange = new Timer();

    private final Timer blockChangeTimer = new Timer();

    private float changesToBedrock = 0;

    private static final String[] teleportItems = new String[] {"Void", "Hyperion", "Aspect"};

    private static void DisableMacros() {
        DisableMacros(false);
    }

    public static void DisableMacros(boolean saveLastMacro) {

        if (MobKiller.isToggled) {
            MightyMiner.mobKiller.toggle();
        }

        for (Macro macro : macros) {
            if (macro.isEnabled()) {
                if (saveLastMacro) {
                    lastMacro = macro;
                }
                macro.toggle();
            }
        }
        UngrabUtils.regrabMouse();
    }

    @SubscribeEvent(receiveCanceled=true, priority=HIGHEST)
    public void onBlockChange(BlockChangeEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (event.update.getBlock().equals(Blocks.bedrock) && new Vec3(event.pos).distanceTo(mc.thePlayer.getPositionVector()) < 6) {
            changesToBedrock++;
        }
        if (blockChangeTimer.hasReached(1000)) {
            if (changesToBedrock > MightyMiner.config.bedrockThreshold) {
                PingAlert.sendPingAlert();
                bedrockFailsafeFake(false);
            }
            blockChangeTimer.reset();
            changesToBedrock = 0;
        }
    }


    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        if (CommissionMacro.isWarping()) return;
        if (mc.thePlayer == null || mc.theWorld == null)
            return;
        if (!MightyMiner.config.disableOnWorldChange) return;
        lastWorldChange.reset();
        if (macros.stream().noneMatch(Macro::isEnabled)) return;

        LogUtils.addMessage("World changed, disabling macros");

        for (Macro macro : macros) {
            if (macro.isEnabled()) {
                macro.toggle();
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (!MightyMiner.config.playerFailsafe) return;

        if (macros.stream().noneMatch(Macro::isEnabled)) return;

        int selfCount = 0;
        for(Entity e :  mc.theWorld.getLoadedEntityList()){
            if (e.getDisplayName().equals(mc.thePlayer.getDisplayName()) && e.getDistanceToEntity(mc.thePlayer) < 10) {
                selfCount++;
            }
        }
        if (selfCount >= 2) {
            if (!lastWorldChange.hasReached(1500)) {
                bedrockFailsafeFake(true);
            }
            return;
        }
        int bedrockCount = 0;
        for (BlockPos bp : BlockPos.getAllInBox(mc.thePlayer.getPosition().add(5, 5, 5), mc.thePlayer.getPosition().add(-5, -5, -5))) {
            if (mc.theWorld.getBlockState(bp).getBlock().equals(Blocks.bedrock)) {
                bedrockCount++;
            }
        }
        if (bedrockCount > MightyMiner.config.bedrockBackupThreshold) {
            bedrockFailsafeFake(false);
            return;
        }

        if (PlayerUtils.isNearPlayer(MightyMiner.config.playerRad) && someoneIsCloseTimer == null){
            someoneIsCloseTimer = new Timer();
        } else if (!PlayerUtils.isNearPlayer(MightyMiner.config.playerRad) && someoneIsCloseTimer != null) {
            someoneIsCloseTimer = null;
        }

        if (PlayerUtils.isNearPlayer(MightyMiner.config.playerRad) && someoneIsCloseTimer != null && someoneIsCloseTimer.hasReached(MightyMiner.config.playerDetectionThreshold)) {
            PingAlert.sendPingAlert();
            DisableMacros();
            KeybindHandler.resetKeybindState();
            LogUtils.addMessage("Someone is close, disabling macros");
            someoneIsCloseTimer = null;
        }
    }

    private long firstRotationCheck = 0;

    @SubscribeEvent
    public void onPacket(ReceivePacketEvent event) {
        if (!MightyMiner.config.stopMacrosOnRotationCheck) return;
        if (macros.stream().noneMatch(Macro::isEnabled)) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!(event.packet instanceof S08PacketPlayerPosLook)) return;
        if (mc.thePlayer.getHeldItem() != null && Arrays.stream(teleportItems).anyMatch(i -> mc.thePlayer.getHeldItem().getDisplayName().contains(i))) return;
        if (CommissionMacro.isWarping()) return;
        if (Macro.brokeBlockUnderPlayer) return;
        if (mc.theWorld != null && mc.thePlayer != null && (BlockUtils.getBlockState(BlockUtils.getPlayerLoc()).getBlock().equals(Blocks.bedrock) || BlockUtils.getBlockState(BlockUtils.getPlayerLoc().down()).getBlock().equals(Blocks.bedrock) || BlockUtils.getBlockState(BlockUtils.getPlayerLoc()).getBlock().equals(Blocks.air) && BlockUtils.getBlockState(BlockUtils.getPlayerLoc().down()).getBlock().equals(Blocks.air))) return;

        if (rotationChecks > 2) {
            DisableMacros();
            LogUtils.addMessage("You've got probably been rotation checked by staff, rotation packets since " + (firstRotationCheck / 1000) + " seconds = " + rotationChecks + ". Disabling macros.");
            PingAlert.sendPingAlert();
            fakeMovement(false);
            rotationChecks = 0;
        } else if (rotationChecks > 1) {
                PingAlert.sendPingAlert();
                LogUtils.addMessage("You've got probably been rotation checked. Pausing macros and faking movement");
                DisableMacros(true);
                lastRotationCheck.reset();
                fakeMovement(true);
                rotationChecks++;
        } else {
            PingAlert.sendPingAlert();
            LogUtils.addMessage("You've got probably been rotation checked or got a resync.");
            lastRotationCheck.reset();
            rotationChecks++;
            firstRotationCheck = System.currentTimeMillis();
        }

        if (lastRotationCheck.hasReached(15_000)) {
            rotationChecks = 0;
            firstRotationCheck = 0;
        }
    }

    private static final Rotation rotation = new Rotation();
    private static Tuple<Float, Float> targetRotation;
    private static Thread fakeMovementThread;

    @SubscribeEvent
    public void onWorldLastRender(RenderWorldLastEvent event) {
        if (rotation.rotating) {
            rotation.update();
        }
    }

    public static void fakeMovement() {
        fakeMovement(false);
    }

    public static TrayIcon createNotification(String text, SystemTray tray) {
        TrayIcon trayIcon = new TrayIcon(new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR), "MightyMiner Failsafe Notification");
        trayIcon.setToolTip("MightyMiner Failsafe Notification");
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

        trayIcon.displayMessage("MightyMiner - Failsafes", text, TrayIcon.MessageType.WARNING);
        return trayIcon;
    }

    public static void bedrockFailsafeFake(boolean resumeMacro) {
        if (fakeMovementThread != null) return;

        DisableMacros();

        fakeMovementThread = new Thread(() -> {
            if(!MightyMiner.config.fakeMovements)
                return;

            try {
                float up1 = 1;
                float up2 = 1;
                if (MathUtils.randomNum(0, 1) == 1) {
                    up1 = -1;
                }
                if (MathUtils.randomNum(0, 1) == 1) {
                    up2 = -1;
                }
                int repetitions = new Random().nextInt(1) + 1;
                double delta_yaw = new Random().nextInt(25) + 25;

                Thread.sleep(new Random().nextInt(150) + 200);
                if (new Random().nextInt(2) == 0)
                    mc.thePlayer.jump();
                else {
                    for (int i = 0; i < new Random().nextInt(3) + 1; i++) {
                        KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);
                        Thread.sleep(100);
                        KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, false);
                        Thread.sleep(150);
                    }
                }
                Thread.sleep(new Random().nextInt(200) + 150);
                targetRotation = new Tuple<>((float) (mc.thePlayer.rotationYaw + delta_yaw + new Random().nextInt(10) + 3), mc.thePlayer.rotationPitch + (new Random().nextInt(2) + 1) * up1);
                int timeToRotate = new Random().nextInt(100) + 150;
                rotation.initAngleLock(targetRotation.getFirst(), targetRotation.getSecond(), timeToRotate);
                Thread.sleep(timeToRotate + 500);
                targetRotation = new Tuple<>((float) (mc.thePlayer.rotationYaw - delta_yaw - new Random().nextInt(10) + 3), mc.thePlayer.rotationPitch + (new Random().nextInt(2) + 1) * up2);
                timeToRotate = new Random().nextInt(100) + 150;
                rotation.initAngleLock(targetRotation.getFirst(), targetRotation.getSecond(), timeToRotate);
                Thread.sleep(timeToRotate + 250);
                if (new Random().nextInt(2) == 0) {
                    for (int j = 0; j < new Random().nextInt(3); j++) {
                        KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);
                        Thread.sleep(100);
                        KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, false);
                        Thread.sleep(150);
                    }
                }
                targetRotation = new Tuple<>(mc.thePlayer.rotationYaw + (new Random().nextInt(10) + 3) * up2, (float) (85 - new Random().nextInt(14)));
                timeToRotate = new Random().nextInt(100) + 150;
                rotation.initAngleLock(targetRotation.getFirst(), targetRotation.getSecond(), timeToRotate);
                Thread.sleep(timeToRotate + 250);
                if (new Random().nextInt(2) == 0) {
                    for (int j = 0; j < new Random().nextInt(3); j++) {
                        KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);
                        Thread.sleep(100);
                        KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, false);
                        Thread.sleep(150);
                    }
                }
                if (new Random().nextInt(2) == 0)
                    mc.thePlayer.jump();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        fakeMovementThread.start();
        new Thread(() -> {
            try {
                fakeMovementThread.join();
                fakeMovementThread = null;

                if (resumeMacro) {
                    lastMacro.toggle();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static void fakeMovement(boolean resumeMacro) {
        if (fakeMovementThread != null) return;

        SystemTray tray = SystemTray.getSystemTray();
        TrayIcon trayIcon;
        if(MightyMiner.config.notifications)
            trayIcon = createNotification("ROTATION CHECKED!!!", tray);
        else {
            trayIcon = null;
        }

        DisableMacros();

        fakeMovementThread = new Thread(() -> {
            if(!MightyMiner.config.fakeMovements)
                return;

            try {
                int numberOfRepeats = new Random().nextInt(2) + 2;
                Thread.sleep(new Random().nextInt(150) + 200);
                if (new Random().nextInt(2) == 0)
                    mc.thePlayer.jump();
                else {
                    for (int i = 0; i < new Random().nextInt(3) + 1; i++) {
                        KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);
                        Thread.sleep(100);
                        KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, false);
                        Thread.sleep(150);
                    }
                }
                Thread.sleep(new Random().nextInt(200) + 150);
                for (int i = 0; i < numberOfRepeats; i++) {
                    Thread.sleep(new Random().nextInt(150) + 200);
                    targetRotation = new Tuple<>(mc.thePlayer.rotationYaw + new Random().nextInt(200) - 100, Math.min(Math.max(mc.thePlayer.rotationPitch + new Random().nextInt(100) - 50, -45), 45));
                    int timeToRotate = new Random().nextInt(100) + 150;
                    rotation.initAngleLock(targetRotation.getFirst(), targetRotation.getSecond(), timeToRotate);
                    Thread.sleep(timeToRotate + 250);
                    if (new Random().nextInt(2) == 0) {
                        for (int j = 0; j < new Random().nextInt(3); j++) {
                            KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);
                            Thread.sleep(100);
                            KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, false);
                            Thread.sleep(150);
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        fakeMovementThread.start();
        new Thread(() -> {
            try {
                fakeMovementThread.join();
                fakeMovementThread = null;
                // With this, also the notification disappears, so maybe good idea would be to create the icon after loading the mc with logo/icon?

                if(MightyMiner.config.notifications)
                     tray.remove(trayIcon);
                if (resumeMacro) {
                    lastMacro.toggle();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
