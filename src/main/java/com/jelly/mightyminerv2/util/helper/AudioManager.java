package com.jelly.mightyminerv2.util.helper;


import cc.polyfrost.oneconfig.utils.Multithreading;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.util.Logger;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.sound.sampled.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class AudioManager {

    private static volatile AudioManager instance;
    private final Minecraft mc = Minecraft.getMinecraft();

    @Getter
    @Setter
    private boolean minecraftSoundEnabled = false;

    private final Clock delayBetweenPings = new Clock();
    private int numSounds = 15;
    @Setter
    private float soundBeforeChange = 0;
    private static Clip clip;

    private AudioManager() {
        // Private constructor for Singleton
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            synchronized (AudioManager.class) {
                if (instance == null) {
                    instance = new AudioManager();
                }
            }
        }
        return instance;
    }


    public void resetSound() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
        minecraftSoundEnabled = false;
        if (MightyMinerConfig.maxOutMinecraftSounds) {
            mc.gameSettings.setSoundLevel(SoundCategory.MASTER, soundBeforeChange);
        }
    }

    public void playSound() {
        if (!MightyMinerConfig.failsafeSoundType) {
            if (minecraftSoundEnabled) return;
            startMinecraftSound();
        } else {
            playCustomSound();
        }
    }


    private void startMinecraftSound() {
        numSounds = 15;
        minecraftSoundEnabled = true;
        if (MightyMinerConfig.maxOutMinecraftSounds) {
            mc.gameSettings.setSoundLevel(SoundCategory.MASTER, 1.0f);
        }
    }


    private void playCustomSound() {
        Multithreading.schedule(() -> {
            try {
                AudioInputStream inputStream = getAudioStreamForSelectedSound();
                if (inputStream == null) {
                    Logger.sendError("[Audio Manager] Failed to load sound file!");
                    return;
                }

                clip = AudioSystem.getClip();
                clip.open(inputStream);
                setSoundVolume(MightyMinerConfig.failsafeSoundVolume);
                clip.start();

                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) {
                Logger.sendError("[Audio Manager] Error playing sound: " + e.getMessage());
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    private AudioInputStream getAudioStreamForSelectedSound() throws Exception {
        switch (MightyMinerConfig.failsafeSoundSelected) {
            case 0: // Custom sound file
                File audioFile = new File(mc.mcDataDir.getAbsolutePath() + "/farmhelper_sound.wav");
                if (audioFile.exists() && audioFile.isFile()) {
                    return AudioSystem.getAudioInputStream(audioFile);
                }
                break;
            case 1:
                return AudioSystem.getAudioInputStream(getClass().getResource("/farmhelper/sounds/staff_check_voice_notification.wav"));
            case 2:
                return AudioSystem.getAudioInputStream(getClass().getResource("/farmhelper/sounds/metal_pipe.wav"));
            case 3:
                return AudioSystem.getAudioInputStream(getClass().getResource("/farmhelper/sounds/AAAAAAAAAA.wav"));
            case 4:
                return AudioSystem.getAudioInputStream(getClass().getResource("/farmhelper/sounds/loud_buzz.wav"));
            default:
                Logger.sendError("[Audio Manager] Invalid sound selection!");
        }
        return null;
    }

    private void setSoundVolume(float volumePercentage) {
        FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (float) (Math.log(volumePercentage / 100f) / Math.log(10.0) * 20.0);
        volume.setValue(dB);
    }

    public boolean isSoundPlaying() {
        return (clip != null && clip.isRunning()) || minecraftSoundEnabled;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (MightyMinerConfig.failsafeSoundType || !minecraftSoundEnabled) return;
        handleMinecraftSoundTick();
    }

    private void handleMinecraftSoundTick() {
        if (delayBetweenPings.isScheduled() && !delayBetweenPings.passed()) return;

        if (numSounds <= 0) {
            resetSound();
            return;
        }

        String soundEvent = MightyMinerConfig.failsafeMcSoundSelected == 0 ? "random.orb" : "random.anvil_land";
        mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, soundEvent, 10.0F, 1.0F, false);

        delayBetweenPings.schedule(100);
        numSounds--;
    }
}

