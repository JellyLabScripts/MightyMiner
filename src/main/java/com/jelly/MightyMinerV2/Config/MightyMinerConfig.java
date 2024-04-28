package com.jelly.MightyMinerV2.Config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.migration.VigilanceName;
import com.jelly.MightyMinerV2.Config.Struct.WayPoint;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class MightyMinerConfig extends Config {

    private transient static final Minecraft mc = Minecraft.getMinecraft();
    private transient static final String GENERAL = "General";
    private transient static final String MISCELLANEOUS = "Miscellaneous";
    private transient static final String SCHEDULER = "Scheduler";
    private transient static final String MITHRIL = "Mithril";
    private transient static final String COMMISSION = "Commission";
    private transient static final String GEMSTONE = "Gemstone";
    private transient static final String POWDER = "Powder";
    private transient static final String AOTV = "AOTV";
    private transient static final String AUTO_SELL = "Auto Sell";
    private transient static final String FAILSAFE = "Failsafe";
    private transient static final String HUD = "HUD";
    private transient static final String DEBUG = "Debug";
    private transient static final String DISCORD_INTEGRATION = "Discord Integration";
    private transient static final String EXPERIMENTAL = "Experimental";

    private transient static final File WAYPOINTS_FILE = new File(mc.mcDataDir, "mm_waypoints.json");

    public static List<WayPoint> wayPoints = new ArrayList<>();

    public static enum MacroType {
        MITHRIL,
        COMMISSION,
        GEMSTONE,
        POWDER,
        AOTV
    }

//
//    GENERAL SETTINGS
//

    @Dropdown(
            name = "Macro Type",
            category = GENERAL,
            description = "Select the macro type you want to use",
            options = {
                    "Mithril",
                    "Commission",
                    "Gemstone",
                    "Powder",
                    "AOTV"
            },
            size = 2
    )
    public static int macroType = 0;

//
//    DEBUG SETTINGS
//

    @Switch(
            name = "Debug Mode",
            category = DEBUG,
            description = "Enable debug mode"
    )
    public static boolean debugMode = false;



    public MightyMinerConfig() {
        super(new Mod("Mighty Miner", ModType.HYPIXEL, "/MightyMinerV2/icon-mod/icon.png"), "/MightMinerV2/config.json");
        initialize();

        this.addDependency("macroType", "Macro Type", () -> {
            return null;
        });

        this.addDependency("debugMode", "Debug Mode", () -> {
            return null;
        });

        save();
    }
}
