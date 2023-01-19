package com.jelly.MightyMiner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jelly.MightyMiner.command.AOTVWaypointsCommands;
import com.jelly.MightyMiner.command.BaritoneDebug;
import com.jelly.MightyMiner.command.Route;
import com.jelly.MightyMiner.config.Config;
import com.jelly.MightyMiner.config.aotv.AOTVWaypoints;
import com.jelly.MightyMiner.config.coords.CoordsConfig;
import com.jelly.MightyMiner.config.coords.factory.ConfigurationFactory;
import com.jelly.MightyMiner.config.locations.AOTVConfig;
import com.jelly.MightyMiner.features.Failsafes;
import com.jelly.MightyMiner.features.FuelFilling;
import com.jelly.MightyMiner.config.waypoint.WaypointConfig;
import com.jelly.MightyMiner.features.MobKiller;
import com.jelly.MightyMiner.features.RGANuker;
import com.jelly.MightyMiner.gui.AOTVWaypointsGUI;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.utils.HypixelUtils.SkyblockInfo;
import com.jelly.MightyMiner.waypoints.WaypointHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Mod(name = "MightyMiner", modid = MightyMiner.MODID, version = MightyMiner.VERSION)
public class MightyMiner {
    public static final String MODID = "mightyminer";
    public static final String VERSION = "1.0";
    public static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

    public static Config config;

    //TODO: fix executor service
    //public static ExecutorService pathfindPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("PathFinderPool-%d").build());

    public static List<BlockPos> coords = new ArrayList<>();

    public static ConfigurationFactory configurationFactory = new ConfigurationFactory();

    public static MobKiller mobKiller = new MobKiller();

    public static CoordsConfig coordsConfig;
    public static WaypointConfig waypointConfig;
    public static AOTVConfig aotvConfig;
    //thx pizza for fixing this
    public static void onStartGame(){
        coords.clear();
        coordsConfig = configurationFactory.create(CoordsConfig.class, new File("config/coords.json"));
        waypointConfig = configurationFactory.create(WaypointConfig.class, new File("config/waypoints.json"));
        aotvConfig = configurationFactory.create(AOTVConfig.class, new File("config/aotvlocations.json"));
    }

    public static AOTVWaypoints aotvWaypoints;

    public void createNewWaypointsConfig(FMLPreInitializationEvent event) {
        File directory = new File(event.getModConfigurationDirectory().getAbsolutePath());
        File coordsFile = new File(directory, "aotv_coords_mm.json");

        if (!coordsFile.exists()) {
            try {
                Files.createFile(Paths.get(coordsFile.getPath()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Reader reader = Files.newBufferedReader(Paths.get("./config/aotv_coords_mm.json"));
            aotvWaypoints = gson.fromJson(reader, AOTVWaypoints.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (aotvWaypoints != null) {
            System.out.println(aotvWaypoints.getRoutes());
        } else {
            System.out.println("aotvWaypoints is null");
            System.out.println("Creating new CoordsConfig");
            aotvWaypoints = new AOTVWaypoints();
            AOTVWaypointsGUI.SaveWaypoints();
            System.out.println(aotvWaypoints.getRoutes());
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        createNewWaypointsConfig(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {


        config = new Config();

        MinecraftForge.EVENT_BUS.register(new MacroHandler());
        MinecraftForge.EVENT_BUS.register(new WaypointHandler());
        MinecraftForge.EVENT_BUS.register(new KeybindHandler());
        MinecraftForge.EVENT_BUS.register(new RGANuker());
        MinecraftForge.EVENT_BUS.register(new SkyblockInfo());
        MinecraftForge.EVENT_BUS.register(new FuelFilling());
        MinecraftForge.EVENT_BUS.register(mobKiller);
        MinecraftForge.EVENT_BUS.register(new Failsafes());
        MinecraftForge.EVENT_BUS.register(new AOTVWaypointsCommands());
        KeybindHandler.initializeCustomKeybindings();
        MacroHandler.initializeMacro();

        config = new Config();

        ClientCommandHandler.instance.registerCommand(new Route());
        ClientCommandHandler.instance.registerCommand(new BaritoneDebug());


        Minecraft.getMinecraft().gameSettings.gammaSetting = 100;

    }


}
