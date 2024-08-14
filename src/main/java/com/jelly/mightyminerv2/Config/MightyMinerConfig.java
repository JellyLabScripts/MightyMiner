package com.jelly.mightyminerv2.Config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import com.jelly.mightyminerv2.Feature.impl.RouteBuilder;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.io.File;

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
  private transient static final String ROUTE_BUILDER = "Route Builder";
  private transient static final String DELAY = "Delays";
  private transient static final String AUTO_SELL = "Auto Sell";
  private transient static final String FAILSAFE = "Failsafe";
  private transient static final String HUD = "HUD";
  private transient static final String DEBUG = "Debug";
  private transient static final String DISCORD_INTEGRATION = "Discord Integration";
  private transient static final String EXPERIMENTAL = "Experimental";

  private transient static final File WAYPOINTS_FILE = new File(mc.mcDataDir, "mm_waypoints.json");

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

  //<editor-fold desc="Mithril">
  @Slider(
      name = "Rotation Time",
      category = MITHRIL,
      description = "Time it takes to rotate to the next block while mining mithril",
      min = 50, max = 1000
  )
  public static int mithrilMinerRotationTime = 300;

  @Slider(
      name = "Rotation Time Randomization",
      category = MITHRIL,
      min = 50, max = 1000
  )
  public static int mithrilMinerRotationTimeRandomizer = 300;

  @Slider(
      name = "Tick Glide Offset",
      category = MITHRIL,
      min = 0, max = 10
  )
  public static int mithrilMinerTickGlideOffset = 4;

  @Switch(
      name = "Sneak While Mining",
      category = MITHRIL
  )
  public static boolean mithrilMinerSneakWhileMining = false;

  @Switch(
      name = "Gives Titanium a higher priority",
      category = MITHRIL
  )
  public static boolean mithrilMinerTitaniumHighPriority = false;
  //</editor-fold>

  //<editor-fold desc="Commission Macro">
  @Switch(
      name = "Sprint During MobKiller", description = "Allow Sprinting while mobkiller is active (looks sussy with sprint)",
      category = COMMISSION,
      subcategory = "MobKiller"
  )
  public static boolean commMobKillerSprint = true;

  @Slider(
      name = "Distance cost",
      category = COMMISSION,
      subcategory = "MobKiller",
      min = 1, max = 100
  )
  public static int commDistCost = 50;


  @Slider(
      name = "Rotation cost",
      category = COMMISSION,
      subcategory = "MobKiller",
      min = 1, max = 100
  )
  public static int commRotCost = 25;

  //</editor-fold>

  //<editor-fold desc="Route Builder">
  @KeyBind(
      name = "Enable RouteBuilder",
      description = "They key to click to enable RouteBuilder",
      category = ROUTE_BUILDER,
      size = 2
  )
  public static OneKeyBind routeBuilder = new OneKeyBind(Keyboard.KEY_LBRACKET);

  @KeyBind(
      name = "Add Block To Route(AOTV)",
      description = "The Key to click to add the block player is standing on block to the route",
      category = ROUTE_BUILDER
  )
  public static OneKeyBind routeBuilderAotvAddKeybind = new OneKeyBind(Keyboard.KEY_P);

  @KeyBind(
      name = "Add Block To Route(ETHERWARP)",
      description = "The Key to click to add the block player is standing on block to the route",
      category = ROUTE_BUILDER
  )
  public static OneKeyBind routeBuilderEtherwarpAddKeybind = new OneKeyBind(Keyboard.KEY_I);

  @KeyBind(
      name = "Remove Block From Route",
      description = "The Key To Remove the block player is standing on from the route",
      category = ROUTE_BUILDER
  )
  public static OneKeyBind routeBuilderRemoveKeybind = new OneKeyBind(Keyboard.KEY_O);

  @Color(
      name = "Route Node Color",
      description = "The Color of The Blocks On a Route",
      category = ROUTE_BUILDER
  )
  public static OneColor routeBuilderNodeColor = new OneColor(0, 255, 255, 100);

  @Color(
      name = "Route Tracer Color",
      description = "The Color of The Line Between Blocks On a Route",
      category = ROUTE_BUILDER
  )
  public static OneColor routeBuilderTracerColor = new OneColor(0, 255, 255, 100);
  //</editor-fold>


  //<editor-fold desc="Delays">
  //<editor-fold desc="AutoAotv">
  @Slider(
      name = "Aotv Look Delay (Right Click)",
      description = "Rotation time to look at next block while aotving",
      category = DELAY,
      subcategory = "AutoAotv",
      min = 50, max = 1000
  )
  public static int delayAutoAotvLookDelay = 250;

  @Slider(
      name = "Aotv Look Delay (Etherwarp)",
      description = "Rotation time to look at next block while Etherwarping",
      category = DELAY,
      subcategory = "AutoAotv",
      min = 50, max = 2000
  )
  public static int delayAutoAotvEtherwarpLookDelay = 500;
  //</editor-fold>
  //</editor-fold>

//
//    DEBUG SETTINGS
//

  @Switch(
      name = "Debug Mode",
      category = DEBUG,
      description = "Enable debug mode"
  )
  public static boolean debugMode = false;

  @Switch(
      name = "Full Blocks",
      category = MISCELLANEOUS,
      description = "Gives a full block hitbox to blocks without a full block hitbox"
  )
  public static boolean fullblock = false;

  public MightyMinerConfig() {
    super(new Mod("Mighty Miner", ModType.HYPIXEL), "/MightMinerV2/config.json");
    initialize();
    registerKeyBind(routeBuilder, RouteBuilder.getInstance()::toggle);

    save();
  }
}
