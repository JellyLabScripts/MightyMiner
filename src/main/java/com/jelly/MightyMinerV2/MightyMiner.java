package com.jelly.MightyMinerV2;

import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jelly.MightyMinerV2.Command.OsamaTestCommandNobodyTouchPleaseLoveYou;
import com.jelly.MightyMinerV2.Command.RouteBuilderCommand;
import com.jelly.MightyMinerV2.Config.MightyMinerConfig;
import com.jelly.MightyMinerV2.Feature.FeatureManager;
import com.jelly.MightyMinerV2.Handler.GameStateHandler;
import com.jelly.MightyMinerV2.Handler.RotationHandler;
import com.jelly.MightyMinerV2.Util.LogUtil;
import com.jelly.MightyMinerV2.Util.ReflectionUtils;
import com.jelly.MightyMinerV2.Util.ScoreboardUtil;
import com.jelly.MightyMinerV2.Util.TablistUtil;
import com.jelly.MightyMinerV2.Handler.RouteHandler;
import com.jelly.MightyMinerV2.Handler.Waypoints.BaritoneWaypointHandler;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.opengl.Display;

import javax.naming.Name;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Mod(modid = "MightyMinerV2", useMetadata = true)
public class MightyMiner {
    public final String VERSION = "%%VERSION%%";
    public static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
    public static MightyMinerConfig config;
    public static boolean isDebug = false;
    public static boolean sendNotSupportedMessage = false;
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final Path routePath = Paths.get("./config/MightyMinerV2/mighty_miner_routes.json");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File coordFile = routePath.toFile();
        if (!coordFile.exists()) {
            coordFile.getParentFile().mkdirs();
            try {
                coordFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Reader reader = Files.newBufferedReader(routePath);
            RouteHandler.instance = gson.fromJson(reader, RouteHandler.class);
        } catch (Exception e) {
            System.out.println("Something is wrong with Routes. Please Notify the devs.");
            e.printStackTrace();
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        initializeFields();
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

    private void initializeFields() {
        config = new MightyMinerConfig();
    }

    private void initializeListeners() {
        // Initialize Listeners
        FeatureManager.getInstance().getFeatures().forEach(MinecraftForge.EVENT_BUS::register);

        MinecraftForge.EVENT_BUS.register(GameStateHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(RotationHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(RouteHandler.getInstance());

        // Create a new instance of the BaritoneWaypointHandler class
        BaritoneWaypointHandler handler = new BaritoneWaypointHandler();

        // Get the names of all the graphs in the BaritoneWaypointHandler object
        Set<String> graphNames = handler.getGraphNames();

        // Create a new instance of the OsamaTestCommandNobodyTouchPleaseLoveYou class for each graph
        for (String graphName : graphNames) {
            // Pass the BaritoneWaypointHandler object, the graph name, and the graph object to the getInstance method
            MinecraftForge.EVENT_BUS.register(OsamaTestCommandNobodyTouchPleaseLoveYou.getInstance(handler, graphName, handler.getGraph(graphName)));
        }

        MinecraftForge.EVENT_BUS.register(new ScoreboardUtil());
        MinecraftForge.EVENT_BUS.register(new TablistUtil());
    }



    private void initializeCommands() {
        // Initialize Commands

        // Create a new instance of the BaritoneWaypointHandler class
        BaritoneWaypointHandler handler = new BaritoneWaypointHandler();

        // Get the names of all the graphs in the BaritoneWaypointHandler object
        Set<String> graphNames = handler.getGraphNames();

        // Create a new instance of the OsamaTestCommandNobodyTouchPleaseLoveYou class for each graph
        for (String graphName : graphNames) {
            // Pass the BaritoneWaypointHandler object, the graph name, and the graph object to the constructor
            CommandManager.register(OsamaTestCommandNobodyTouchPleaseLoveYou.getInstance(handler, graphName, handler.getGraph(graphName)));
        }

        CommandManager.register(new RouteBuilderCommand());
    }







}
