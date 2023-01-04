package com.jelly.MightyMiner;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jelly.MightyMiner.baritone.automine.movementgrapth.debug.Visualiser;
import com.jelly.MightyMiner.command.BaritoneDebug;
import com.jelly.MightyMiner.command.Route;
import com.jelly.MightyMiner.config.Config;
import com.jelly.MightyMiner.config.coords.CoordsConfig;
import com.jelly.MightyMiner.config.coords.factory.ConfigurationFactory;
import com.jelly.MightyMiner.config.locations.AOTVConfig;
import com.jelly.MightyMiner.features.Failsafes;
import com.jelly.MightyMiner.features.FuelFilling;
import com.jelly.MightyMiner.config.waypoint.WaypointConfig;
import com.jelly.MightyMiner.features.MobKiller;
import com.jelly.MightyMiner.features.RGANuker;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.utils.SkyblockInfo;
import com.jelly.MightyMiner.waypoints.WaypointHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod(name = "MightyMiner", modid = MightyMiner.MODID, version = MightyMiner.VERSION)
public class MightyMiner {
    public static final String MODID = "mightyminer";
    public static final String VERSION = "1.0";

    public static Config config;

    public static ExecutorService pathfindPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("PathFinderPool-%d").build());

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


    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {


        config = new Config();

        MinecraftForge.EVENT_BUS.register(new MacroHandler());
        MinecraftForge.EVENT_BUS.register(new WaypointHandler());
        MinecraftForge.EVENT_BUS.register(new KeybindHandler());
        MinecraftForge.EVENT_BUS.register(new RGANuker());
        MinecraftForge.EVENT_BUS.register(new SkyblockInfo());
        MinecraftForge.EVENT_BUS.register(Visualiser.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new FuelFilling());
        MinecraftForge.EVENT_BUS.register(mobKiller);
        MinecraftForge.EVENT_BUS.register(new Failsafes());
        KeybindHandler.initializeCustomKeybindings();
        MacroHandler.initializeMacro();

        config = new Config();

        ClientCommandHandler.instance.registerCommand(new Route());
        ClientCommandHandler.instance.registerCommand(new BaritoneDebug());


        Minecraft.getMinecraft().gameSettings.gammaSetting = 100;

    }


}
