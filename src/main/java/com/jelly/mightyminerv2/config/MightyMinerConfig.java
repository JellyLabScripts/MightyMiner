package com.jelly.mightyminerv2.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import com.jelly.mightyminerv2.feature.impl.RouteBuilder;
import com.jelly.mightyminerv2.macro.MacroManager;
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

  // <editor-fold desc="General">
  @Dropdown(
      name = "Macro Type",
      category = GENERAL,
      description = "Select the macro type you want to use",
      options = {
          "Commission",
          "Mithril",
          "Gemstone",
          "Powder",
          "AOTV"
      }
  )
  public static int macroType = 0;

  @KeyBind(
      name = "Toggle Macro",
      category = GENERAL,
      description = "The Button To Click To Toggle The Macro"
  )
  public static OneKeyBind toggleMacro = new OneKeyBind(Keyboard.KEY_GRAVE);

  @Switch(
      name = "Ungrab Mouse", description = "Ungrabs Mouse; Duh",
      category = GENERAL
  )
  public static boolean ungrabMouse = true;

  //</editor-fold>

  //<editor-fold desc="Mithril">
  @Switch(
      name = "Strafe While Mining", description = "Walk Around The Vein While Mining",
      category = MITHRIL
  )
  public static boolean mithrilStrafe = false;

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
  @Text(
      name = "Mining Tool", description = "The tool to use during comm macro",
      category = COMMISSION, placeholder = "Pickonimbus 2000", size = 2
  )
  public static String commMiningTool = "Pickonimbus 2000";

  @Switch(
      name = "Sprint During MobKiller", description = "Allow Sprinting while mobkiller is active (looks sussy with sprint)",
      category = COMMISSION,
      subcategory = "MobKiller"
  )
  public static boolean commMobKillerSprint = true;

  @Switch(
      name = "Interpolate During MobKiller", description = "Helps reduce sliding",
      category = COMMISSION,
      subcategory = "MobKiller"
  )
  public static boolean commMobKillerInterpolate = true;

  @Slider(
      name = "Commission Milestone",
      category = COMMISSION,
      min = 1, max = 3
  )
  public static int commMilestone = 1;

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

  //<editor-fold desc="Failsafe">
  @Slider(
      name = "Time to wait before toggling failsafe(in ms)",
      category = FAILSAFE, subcategory = "Delays",
      min = 0, max = 15000, step = 100
  )
  public static int failsafeToggleDelay = 3000;
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

  @KeyBind(
      name = "RouteBuilder Add UNIDI",
      category = ROUTE_BUILDER, subcategory = "Graph"
  )
  public static OneKeyBind routeBuilderUnidi = new OneKeyBind(Keyboard.KEY_NUMPAD7);

  @KeyBind(
      name = "RouteBuilder Add BIDI",
      category = ROUTE_BUILDER, subcategory = "Graph"
  )
  public static OneKeyBind routeBuilderBidi = new OneKeyBind(Keyboard.KEY_NUMPAD8);

  @KeyBind(
      name = "RouteBuilder Select",
      category = ROUTE_BUILDER, subcategory = "Graph"
  )
  public static OneKeyBind routeBuilderSelect = new OneKeyBind(Keyboard.KEY_NUMPAD4);

  @KeyBind(
      name = "RouteBuilder Move",
      category = ROUTE_BUILDER, subcategory = "Graph"
  )
  public static OneKeyBind routeBuilderMove = new OneKeyBind(Keyboard.KEY_NUMPAD5);

  @KeyBind(
      name = "RouteBuilder Move",
      category = ROUTE_BUILDER, subcategory = "Graph"
  )
  public static OneKeyBind routeBuilderDelete = new OneKeyBind(Keyboard.KEY_NUMPAD6);

  //</editor-fold>

  //<editor-fold desc="Delays">
  //<editor-fold desc="GUI Delay">
  @Slider(
      name = "GUI Delay", description = "Time to wait in a gui",
      category = DELAY, subcategory = "GUI",
      min = 50, max = 2000
  )
  public static int delaysGuiDelay = 450;

  @Slider(
      name = "GUI Delay Randomizer", description = "Maximum random time to add to GUI Delay Time",
      category = DELAY, subcategory = "GUI",
      min = 50, max = 1000
  )
  public static int delaysGuiDelayRandomizer = 250;

  //</editor-fold>
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

  //<editor-fold desc="Dev">
  @Slider(
      name = "Rotation",
      category = "Dev",
      subcategory = "MithrilMiner",
      min = 0f, max = 1f
  )
  public static float devMithRot = 0.4f;

  @Slider(
      name = "Distance",
      category = "Dev",
      subcategory = "MithrilMiner",
      min = 0f, max = 1f
  )
  public static float devMithDist = 1f;

  @Slider(
      name = "Hardness",
      category = "Dev",
      subcategory = "MithrilMiner",
      min = 0f, max = 1f
  )
  public static float devMithHard = 1f;

  @Slider(
      name = "MobKiller Dist Cost",
      category = "Dev",
      subcategory = "MobKiller",
      min = 0, max = 100
  )
  public static int devMKillDist = 100;

  @Slider(
      name = "MobKiller Rot Cost",
      category = "Dev",
      subcategory = "MobKiller",
      min = 0, max = 100
  )
  public static int devMKillRot = 5;

  @Text(
      name = "MobKiller Mob Name",
      category = "Dev",
      subcategory = "MobKiller",
      placeholder = "Enter Mobname Here"
  )
  public static String devMKillerMob = "Goblin";
  //</editor-fold>

  //<editor-fold desc="Debug">
  @Switch(
      name = "Debug Mode",
      category = DEBUG,
      description = "Enable debug mode"
  )
  public static boolean debugMode = false;

  //</editor-fold>

  //<editor-fold desc="Misc">
  @Switch(
      name = "Full Blocks",
      category = MISCELLANEOUS,
      description = "Gives a full block hitbox to blocks without a full block hitbox"
  )
  public static boolean miscFullBlock = false;
  //</editor-fold>

  public static int getRandomGuiWaitDelay() {
    return delaysGuiDelay + (int) (Math.random() * delaysGuiDelayRandomizer);
  }

  public MightyMinerConfig() {
    super(new Mod("Mighty Miner", ModType.HYPIXEL), "/MightMinerV2/config.json");
    initialize();

    registerKeyBind(routeBuilder, RouteBuilder.getInstance()::toggle);
    registerKeyBind(toggleMacro, MacroManager.getInstance()::toggle);

    save();
  }
}
