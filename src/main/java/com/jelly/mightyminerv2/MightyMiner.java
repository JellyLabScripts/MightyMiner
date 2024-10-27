package com.jelly.mightyminerv2;

import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jelly.mightyminerv2.command.OsamaTestCommandNobodyTouchPleaseLoveYou;
import com.jelly.mightyminerv2.command.RouteBuilderCommand;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.failsafe.FailsafeManager;
import com.jelly.mightyminerv2.feature.FeatureManager;
import com.jelly.mightyminerv2.handler.GameStateHandler;
import com.jelly.mightyminerv2.handler.GraphHandler;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.util.*;
import com.jelly.mightyminerv2.handler.RouteHandler;
import com.jelly.mightyminerv2.util.helper.AudioManager;
import com.jelly.mightyminerv2.util.helper.graph.Graph;
import com.jelly.mightyminerv2.util.helper.graph.GraphSerializer;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mod(modid = MightyMiner.modid, useMetadata = true)
public class MightyMiner {

  public final String VERSION = "%%VERSION%%";
  public static final String modid = "mightyminerv2";
  private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(4, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
  public static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(new TypeToken<Graph<RouteWaypoint>>() {}.getType(), new GraphSerializer())
      .excludeFieldsWithoutExposeAnnotation()
      .setPrettyPrinting()
      .create();
  public static MightyMinerConfig config;
  private static final Minecraft mc = Minecraft.getMinecraft();
  private static final List<String> expectedRoutes = Arrays.asList("Commission Macro.json");
  public static final Path routesDirectory = Paths.get("./config/mightyminerv2/predefined");
  public static final Path routePath = Paths.get("./config/mightyminerv2/mighty_miner_routes.json");
//  public static final Path commRoutePath = Paths.get("./config/mightyminerv2/Commission Macro.json");

  @Mod.Instance
  public static MightyMiner instance;

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    File routesDir = routesDirectory.toFile();
    if (!routesDir.exists()) {
      System.out.println("Routes directory not found, creating it now.");
      routesDir.mkdirs();
    }

    File[] files = routesDir.listFiles();
    if (files == null || files.length != expectedRoutes.size()) {
      Arrays.stream(files).forEach(it -> {
        try {
          Files.deleteIfExists(it.toPath());
        } catch (Exception e) {
          System.out.println("Failed to delete " + it);
          e.printStackTrace();
        }
      });
      for (String file : expectedRoutes) {
        Path filePath = routesDir.toPath().resolve(file);
        try {
          Files.copy(getClass().getResourceAsStream("/" + file), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
          System.out.println("Failed to copy " + file);
          continue;
        }

        if(!loadGraph(filePath)){
          System.out.println("Filed to load graph " + file);
        }
      }
      return;
    }

    for (File file : files) {
      if (file.isFile() && expectedRoutes.contains(file.getName())) {
        if(!loadGraph(file.toPath())){
          System.out.println("Couldn't load " + file.getName());
        }
      }
    }
  }

  private boolean loadGraph(Path path) {
    String graphKey = path.getFileName().toString().replace(".json", "");
    try (Reader reader = Files.newBufferedReader(path)) {
      Graph<RouteWaypoint> graph = gson.fromJson(reader, new TypeToken<Graph<RouteWaypoint>>() {
      }.getType());
      GraphHandler.instance.graphs.put(graphKey, graph);
      System.out.println("Loaded graph for: " + graphKey);
      return true;
    } catch (Exception e) {
      System.out.println("Something went wrong while loading the graph for: " + graphKey);
      e.printStackTrace();
      return false;
    }
  }

  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
    initializeFields();
    initializeListeners();
    initializeCommands();

    mc.gameSettings.gammaSetting = 1000;
    mc.gameSettings.pauseOnLostFocus = false;

    Display.setTitle(
        "Mighty Miner 〔v" + VERSION + "〕 " + (MightyMinerConfig.debugMode ? "wazzadev!" : "Chilling huh?") + " ☛ " + mc.getSession().getUsername());
  }

  @Mod.EventHandler
  public void postInit(FMLPostInitializationEvent event) {
    if (ReflectionUtils.hasPackageInstalled("feather")) {
      Logger.sendNotification("Mighty Miner", "Feather Client is not supported! Might cause issues or a lot of bugs!", 5000L);
      Logger.sendWarning("Feather Client is not supported! Might cause issues or a lot of bugs!");
    }
  }

  private void initializeFields() {
    config = new MightyMinerConfig();
  }

  private void initializeListeners() {
    MinecraftForge.EVENT_BUS.register(GameStateHandler.getInstance());
    MinecraftForge.EVENT_BUS.register(RotationHandler.getInstance());
    MinecraftForge.EVENT_BUS.register(RouteHandler.getInstance());
    MinecraftForge.EVENT_BUS.register(GraphHandler.instance);
    MinecraftForge.EVENT_BUS.register(MacroManager.getInstance());
    MinecraftForge.EVENT_BUS.register(FailsafeManager.getInstance());
    MinecraftForge.EVENT_BUS.register(AudioManager.getInstance());
    FeatureManager.getInstance().allFeatures.forEach(MinecraftForge.EVENT_BUS::register);
    MinecraftForge.EVENT_BUS.register(OsamaTestCommandNobodyTouchPleaseLoveYou.getInstance());
    MinecraftForge.EVENT_BUS.register(new ScoreboardUtil());
    MinecraftForge.EVENT_BUS.register(new TablistUtil());
  }

  private void initializeCommands() {
    CommandManager.register(OsamaTestCommandNobodyTouchPleaseLoveYou.getInstance());
    CommandManager.register(new RouteBuilderCommand());
  }

  public static Executor executor() {
    return executor;
  }
}
