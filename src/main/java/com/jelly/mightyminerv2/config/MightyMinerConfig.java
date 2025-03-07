package com.jelly.mightyminerv2.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.data.OptionSize;
import cc.polyfrost.oneconfig.libs.common.value.qual.DoubleVal;
import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import com.jelly.mightyminerv2.feature.impl.RouteBuilder;
import com.jelly.mightyminerv2.hud.DebugHUD;
import com.jelly.mightyminerv2.hud.CommissionHUD;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.AudioManager;
import com.jelly.mightyminerv2.macro.commissionmacro.helper.Commission;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
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

  @Switch(
      name = "Mute Game", description = "Mute Game",
      category = GENERAL
  )
  public static boolean muteGame = true;

  //</editor-fold>

  //<editor-fold desc="Mithril">
  public static String mithrilMiningTool = "Pickonimbus 2000"; // Default Tool

  // Button to set the mithril mining tool to the name of the item the player is holding
  @Button(name = "Set Mining Tool", text = "Set mining tool", description = "Set the Mining Tool to the currently held item for Mithril mining", category = MITHRIL)
  public static void setMithrilMiningTool() {
    ItemStack currentItem = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];
    if (currentItem != null) {
      mithrilMiningTool = currentItem.getDisplayName();  // Set the mining tool name to the held item
      Logger.sendLog("Mining tool set to: " + mithrilMiningTool);  // Log the result
    } else {
      Logger.sendLog("Select the hotbar slot containing your item."); // Log for empty hotbar slot
    }
  }

  @Switch(
      name = "Strafe While Mining", description = "Walk Around The Vein While Mining",
      category = MITHRIL
  )
  public static boolean mithrilStrafe = false;

  @Switch(
      name = "Sneak While Mining",
      category = MITHRIL
  )
  public static boolean mithrilMinerSneakWhileMining = false;

  @Switch(
      name = "Precision Miner", description = "Looks at particles spawned by precision miner perk (Might/Will Mess Up TickGLide)",
      category = MITHRIL
  )
  public static boolean mithrilMinerPrecisionMiner = false;

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

  @Slider(
      name = "Sneak Time",
      category = MITHRIL,
      min = 0, max = 2000
  )
  public static int mithrilMinerSneakTime = 500;

  @Slider(
      name = "Sneak Time Randomizer",
      category = MITHRIL,
      min = 0, max = 2000
  )
  public static int mithrilMinerSneakTimeRandomizer = 300;

  @Dropdown(
          name = "Ore Type",
          category = MITHRIL,
          options = {
                  "Diamond",
                  "Emerald",
                  "Redstone",
                  "Lapis",
                  "Gold",
                  "Iron",
                  "Coal",
                  "Mithril"
          }
  )
  public static int oreType = 0;

  @Slider(
          name = "Gray Mithril Priority",
          category = MITHRIL,
          min = 1,
          max = 10,
          step = 1
  )
  public static int grayMithrilPriority = 5;

  @Slider(
          name = "Green Mithril Priority",
          category = MITHRIL,
          min = 1,
          max = 10,
          step = 1
  )
  public static int greenMithrilPriority = 5;

  @Slider(
          name = "Blue Mithril Priority",
          category = MITHRIL,
          min = 1,
          max = 10,
          step = 1
  )
  public static int blueMithrilPriority = 5;

  @Slider(
          name = "Titanium Priority",
          category = MITHRIL,
          min = 1,
          max = 10,
          step = 1
  )
  public static int titaniumPriority = 5;

  //</editor-fold>

  //<editor-fold desc="Commission Macro">
  // Button to set the mining tool to the name of the item the player is holding
  @Button(name = "Set Mining Tool", text = "Set mining tool", description = "Set the Mining Tool to the currently held item", category = "Commission")
  public static void setCommMiningTool() {
    ItemStack currentItem = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];
    if (currentItem != null) {
      Commission.commMiningTool = currentItem.getDisplayName();  // Set the mining tool name to the held item
    }
    Logger.sendLog("Mining Tool set to: " + Commission.commMiningTool);  // Log the result
  };

  // Button to set the slayer weapon to the name of the item the player is holding
  @Button(name = "Set Slayer Weapon", text = "Set slayer weapon", description = "Set the Slayer Weapon to the currently held item", category = "Commission")
  public static void setSlayerWeapon() {
    ItemStack currentItem = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];
    if (currentItem != null) {
      Commission.commSlayerWeapon = currentItem.getDisplayName();  // Set the slayer weapon name to the held item
    }
    Logger.sendLog("Slayer Weapon set to: " + Commission.commSlayerWeapon);  // Log the result
  };

  @Dropdown(
      name = "Claim Method", category = COMMISSION,
      options = {"NPC", "Royal Pigeon", "Abiphone"}
  )
  public static int commClaimMethod = 0;

  @Switch(
      name = "Always Mine Titanium", description = "Mines titanium even if it isnt a titanium commission",
      category = COMMISSION
  )
  public static boolean commMineTitanium = false;

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

  @Switch(
      name = "Refuel Drill",
      category = COMMISSION,
      subcategory = "Refuel"
  )
  public static boolean commDrillRefuel = false;

  @Dropdown(
      name = "Machine Fuel",
      category = COMMISSION,
      subcategory = "Refuel",
      options = {"Enchanted Poppy", "Goblin Egg", "Green Goblin Egg", "Yellow Goblin Egg", "Red Goblin Egg", "Blue Goblin Egg", "Volta", "Oil Barrel"}
  )
  public static int commMachineFuel = 6;

  @DualOption(name= "Fuel Retrieval Method",
      category = COMMISSION,
      subcategory = "Refuel",
      left = "Buy From Bazaar", right = "Get From Sack"
  )
  public static boolean commFuelRetrievalMethod = false;

  @DualOption(
      name = "Mechanics GUI Access Method",
      category = COMMISSION,
      subcategory = "Refuel",
      left = "Abiphone", right = "From NPC"
  )
  public static boolean commMechaGuiAccessMethod = false;

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

  @Slider(
      name = "Server Side Rotation Time",
      description = "Rotation time to look at next block with client side rotation",
      category = DELAY,
      subcategory = "AutoAotv",
      min = 0, max = 2000
  )
  public static int delayAutoAotvServerRotation = 500;
  //</editor-fold>
  //</editor-fold>

  //<editor-fold desc="Dev">
  @Slider(
      name = "Rotation",
      category = "Dev",
      subcategory = "MithrilMiner",
      min = 0, max = 10
  )
  public static int devMithRot = 3;

  @Slider(
      name = "MobKiller Dist Cost",
      category = "Dev",
      subcategory = "MobKiller",
      min = 0, max = 1000
  )
  public static int devMKillDist = 100;

  @Slider(
      name = "MobKiller Rot Cost",
      category = "Dev",
      subcategory = "MobKiller",
      min = 0, max = 1000
  )
  public static int devMKillRot = 5;

  @Text(
      name = "MobKiller Mob Name",
      category = "Dev",
      subcategory = "MobKiller",
      placeholder = "Enter Mobname Here"
  )
  public static String devMKillerMob = "Goblin";

  @Text(
      name = "MobKiller Weapon",
      category = "Dev",
      subcategory = "MobKiller",
      placeholder = "Enter Weapon Name Here"
  )
  public static String devMKillerWeapon = "Aspect of the Dragon";

  @Slider(
      name = "MobKiller Check Timer",
      category = "Dev",
      subcategory = "MobKiller",
      min = 0, max = 5000
  )
  public static int devMKillTimer = 1000;

  @Slider(
      name = "Rotation Curve",
      category = "Dev",
      subcategory = "Cost",
      min = 0, max = 5
  )
  public static int cost4 = 1;

  @Switch(
      name = "Use Fixed Rotation Time",
      category = "Dev",
      subcategory = "Path"
  )
  public static boolean fixrot = false;

  @Slider(
      name = "Rotation time" ,
      category = "Dev",
      subcategory = "Path",
      min = 0, max = 2000
  )
  public static int rottime = 500;

  @Slider(
      name = "Rota mult",
      category = "Dev",
      subcategory = "Path",
      min = 0f, max = 10f
  )
  public static float rotmult = 2f;

  //</editor-fold>

  //<editor-fold desc="Debug">
  @Switch(
      name = "Debug Mode",
      category = DEBUG,
      description = "Enable debug mode"
  )
  public static boolean debugMode = false;

  @HUD(
      name = "DebugHUD",
      category = DEBUG
  )
  public static DebugHUD debugHUD = DebugHUD.getInstance();

  @HUD(
          name = "CommissionHUD",
          category = COMMISSION
  )
  public static CommissionHUD commissionHUD = CommissionHUD.getInstance();

  //</editor-fold>

  //<editor-fold desc="Failsafe">

  @Switch(
          name = "Enable Failsafe Trigger Sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound", size = OptionSize.DUAL,
          description = "Makes a sound when a failsafe has been triggered"
  )
  public static boolean enableFailsafeSound = true;
  @DualOption(
          name = "Failsafe Sound Type", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
          description = "The failsafe sound type to play when a failsafe has been triggered",
          left = "Minecraft",
          right = "Custom",
          size = 2
  )
  public static boolean failsafeSoundType = false;
  @Dropdown(
          name = "Minecraft Sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
          description = "The Minecraft sound to play when a failsafe has been triggered",
          options = {
                  "Ping", // 0
                  "Anvil" // 1
          }
  )
  public static int failsafeMcSoundSelected = 1;

  @Dropdown(
          name = "Custom Sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
          description = "The custom sound to play when a failsafe has been triggered",
          options = {
                  "Custom", // 0
                  "Voice", // 1
                  "Metal Pipe", // 2
                  "AAAAAAAAAA", // 3
                  "Loud Buzz", // 4
          }
  )
  public static int failsafeSoundSelected = 1;
  @Number(
          name = "Number of times to play custom sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
          description = "The number of times to play custom sound when a failsafe has been triggered",
          min = 1, max = 10
  )
  public static int failsafeSoundTimes = 13;

  @Slider(
          name = "Failsafe Sound Volume (in %)", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
          description = "The volume of the failsafe sound",
          min = 0, max = 100
  )
  public static float failsafeSoundVolume = 50.0f;
  @Switch(
          name = "Max out Master category sounds while pinging", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
          description = "Maxes out the sounds while failsafe"
  )
  public static boolean maxOutMinecraftSounds = false;

  @Button(
          name = "", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
          description = "Plays the selected sound",
          text = "Play"
  )
  Runnable _playFailsafeSoundButton = () -> AudioManager.getInstance().playSound();

  @Button(
          name = "", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
          description = "Stops playing the selected sound",
          text = "Stop"
  )
  Runnable _stopFailsafeSoundButton = () -> AudioManager.getInstance().resetSound();

  //</editor-fold>

  //<editor-fold desc="Misc">
  @Info(
          text = "Will probaply get you banned",
          type = InfoType.WARNING,
          category = EXPERIMENTAL
  )
  public static boolean ignore;

  @KeyBind(
          name = "Nuker",
          category = EXPERIMENTAL
  )
  public static OneKeyBind nuker_keyBind = new OneKeyBind(UKeyboard.KEY_COMMA);

  @Switch(
          name = "Nuker Toggle",
          category = EXPERIMENTAL
  )
public static boolean nuker_toggle = false;

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
