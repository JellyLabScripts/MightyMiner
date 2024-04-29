package com.jelly.MightyMinerV2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jelly.MightyMinerV2.Config.MightyMinerConfig;
import com.jelly.MightyMinerV2.Util.LogUtil;
import com.jelly.MightyMinerV2.Util.ReflectionUtils;
import com.jelly.MightyMinerV2.Util.ScoreboardUtil;
import com.jelly.MightyMinerV2.Util.TablistUtil;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.opengl.Display;

@Mod(modid = "MightyMinerV2", useMetadata = true)
public class MightyMiner {
    public final String VERSION = "%%VERSION%%";
    public static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

    public static MightyMinerConfig config;
    public static boolean isDebug = false;
    public static boolean sendNotSupportedMessage = false;
    private static final Minecraft mc = Minecraft.getMinecraft();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        initializeFieds();
        initializeListeners();
        initializeCommands();

        mc.gameSettings.gammaSetting = 1000;
        mc.gameSettings.pauseOnLostFocus = false;

        isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;

        if (!ReflectionUtils.hasPackageInstalled("farmhelper")) {
            Display.setTitle("Mighty Miner 〔v" + VERSION + "〕 " + (isDebug ? "wazzadev!" : "Chilling huh?") + " ☛ " + mc.getSession().getUsername());
        }

        LogUtil.sendNotification("Mighty Miner", "Mighty Miner V2 has been loaded!", 5000L);
    }

    @SubscribeEvent
    public void onTickSendNotSupportedMessage(TickEvent.PlayerTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || sendNotSupportedMessage) return;

        if (ReflectionUtils.hasPackageInstalled("feather")) {
            LogUtil.sendNotification("Mighty Miner", "Feather Client is not supported! Might cause issues or a lot of bugs!", 5000L);
            LogUtil.send("Feather Client is not supported! Might cause issues or a lot of bugs!", LogUtil.ELogType.WARNING);
            sendNotSupportedMessage = true;
        }

        if (ReflectionUtils.hasModFile("lunar")) {
            LogUtil.sendNotification("Mighty Miner", "Lunar Client is not supported! Might cause issues or a lot of bugs!", 5000L);
            LogUtil.send("Lunar Client is not supported! Might cause issues or a lot of bugs!", LogUtil.ELogType.WARNING);
            sendNotSupportedMessage = true;
        }
    }

    private void initializeFieds() {
        config = new MightyMinerConfig();
    }

    private void initializeListeners() {
        // Initialize Listeners
        MinecraftForge.EVENT_BUS.register(new ScoreboardUtil());
        MinecraftForge.EVENT_BUS.register(new TablistUtil());
    }

    private void initializeCommands() {
        // Initialize Commands
    }
}
