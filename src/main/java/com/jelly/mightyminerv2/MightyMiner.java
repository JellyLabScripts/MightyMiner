package com.jelly.mightyminerv2;

import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jelly.mightyminerv2.Command.OsamaTestCommandNobodyTouchPleaseLoveYou;
import com.jelly.mightyminerv2.Command.RouteBuilderCommand;
import com.jelly.mightyminerv2.Config.MightyMinerConfig;
import com.jelly.mightyminerv2.Feature.FeatureManager;
import com.jelly.mightyminerv2.Handler.GameStateHandler;
import com.jelly.mightyminerv2.Handler.GraphHandler;
import com.jelly.mightyminerv2.Handler.RotationHandler;
import com.jelly.mightyminerv2.Util.LogUtil;
import com.jelly.mightyminerv2.Util.ReflectionUtils;
import com.jelly.mightyminerv2.Util.ScoreboardUtil;
import com.jelly.mightyminerv2.Util.TablistUtil;
import com.jelly.mightyminerv2.Handler.RouteHandler;
import com.jelly.mightyminerv2.Util.helper.graph.Graph;
import com.jelly.mightyminerv2.Util.helper.graph.GraphSerializer;
import com.jelly.mightyminerv2.Util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.pathfinder.calculate.path.PathExecutor;
import com.jelly.mightyminerv2.pathfinder.helper.BlockStateAccessor;
import com.jelly.mightyminerv2.pathfinder.helper.player.IPlayerContext;
import com.jelly.mightyminerv2.pathfinder.helper.player.PlayerContext;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mod(modid = "MightyMinerV2", useMetadata = true)
public class MightyMiner {

  private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(4, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
  public final String VERSION = "%%VERSION%%";
  public static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(new TypeToken<Graph<RouteWaypoint>>(){}.getType(), new GraphSerializer<RouteWaypoint>())
      .excludeFieldsWithoutExposeAnnotation()
      .setPrettyPrinting()
      .create();
  public static MightyMinerConfig config;
  public static boolean isDebug = false;
  public static boolean sendNotSupportedMessage = false;
  private static final Minecraft mc = Minecraft.getMinecraft();
  public static final Path routePath = Paths.get("./config/MightyMinerV2/mighty_miner_routes.json");
  public static final Path commRoutePath = Paths.get("./config/MightyMinerV2/comm_routes.json");

  public IPlayerContext playerContext = new PlayerContext(this, Minecraft.getMinecraft());
//  public BlockStateAccessor bsa = null;

  @Mod.Instance
  public static MightyMiner instance;

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    File coordFile = routePath.toFile();
    if (!coordFile.exists()) {
      coordFile.getParentFile().mkdirs();
      try {
        coordFile.createNewFile();
      } catch (IOException e) {
        System.out.println("Something went wrong while creating RouteFile");
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

    File commRouteFile = commRoutePath.toFile();
    if (!commRouteFile.exists()) {
      commRouteFile.getParentFile().mkdirs();
      try {
        Files.copy(getClass().getResourceAsStream("/comm_routes.json"), commRoutePath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        System.out.println("Something went wrong while creating CommRouteFile");
        e.printStackTrace();
      }
    }

    try {
      Reader reader = Files.newBufferedReader(commRoutePath);
      GraphHandler.instance = gson.fromJson(reader, GraphHandler.class);
    } catch (Exception e) {
      System.out.println("Something is wrong with Comm Routes. Please Notify the devs.");
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

    Display.setTitle("Mighty Miner 〔v" + VERSION + "〕 " + (isDebug ? "wazzadev!" : "Chilling huh?") + " ☛ " + mc.getSession().getUsername());
  }

  @SubscribeEvent
  public void onTickSendNotSupportedMessage(TickEvent.PlayerTickEvent event) {
    if (mc.thePlayer == null || mc.theWorld == null || sendNotSupportedMessage) {
      return;
    }

    if (ReflectionUtils.hasPackageInstalled("feather")) {
      LogUtil.sendNotification("Mighty Miner", "Feather Client is not supported! Might cause issues or a lot of bugs!", 5000L);
      LogUtil.warn("Feather Client is not supported! Might cause issues or a lot of bugs!");
      sendNotSupportedMessage = true;
    }
  }

  private void initializeFields() {
    config = new MightyMinerConfig();
  }

  private void initializeListeners() {
    FeatureManager.getInstance().getFeatures().forEach(MinecraftForge.EVENT_BUS::register);

    MinecraftForge.EVENT_BUS.register(GameStateHandler.getInstance());
    MinecraftForge.EVENT_BUS.register(RotationHandler.getInstance());
    MinecraftForge.EVENT_BUS.register(RouteHandler.getInstance());
    MinecraftForge.EVENT_BUS.register(GraphHandler.getInstance());
    MinecraftForge.EVENT_BUS.register(new ScoreboardUtil());
    MinecraftForge.EVENT_BUS.register(new TablistUtil());
    MinecraftForge.EVENT_BUS.register(OsamaTestCommandNobodyTouchPleaseLoveYou.getInstance());
    MinecraftForge.EVENT_BUS.register(PathExecutor.INSTANCE);
  }

  private void initializeCommands() {
    CommandManager.register(OsamaTestCommandNobodyTouchPleaseLoveYou.getInstance());
    CommandManager.register(new RouteBuilderCommand());
  }

  public static Executor executor(){
    return executor;
  }
}
