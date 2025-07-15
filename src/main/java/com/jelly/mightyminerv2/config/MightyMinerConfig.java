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
import com.jelly.mightyminerv2.feature.impl.RouteBuilder;
import com.jelly.mightyminerv2.hud.CommissionHUD;
import com.jelly.mightyminerv2.hud.DebugHUD;
import com.jelly.mightyminerv2.hud.GlacialCommissionHUD;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.AudioManager;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.File;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class MightyMinerConfig extends Config {

    // Do not remove the "transient" modifier!
    private transient static final Minecraft mc = Minecraft.getMinecraft();
    private transient static final File WAYPOINTS_FILE = new File(mc.mcDataDir, "mm_waypoints.json");
    private transient static final String GENERAL = "General";
    private transient static final String SCHEDULER = "Scheduler";
    private transient static final String COMMISSION = "Dwarven Commission";
    private transient static final String GLACIAL_COMMISSION = "Glacial Commission";
    private transient static final String MINING_MACRO = "Mining Macro";
    private transient static final String ROUTE_MINER = "Route Miner";
    private transient static final String POWDER = "Gemstone Powder";
    private transient static final String DELAY = "Delays";
    private transient static final String AUTO_SELL = "Auto Sell";
    private transient static final String FAILSAFE = "Failsafe";
    private transient static final String DEBUG = "Debug";
    private transient static final String DISCORD_INTEGRATION = "Discord Integration";


    public MightyMinerConfig() {
        super(new Mod("Mighty Miner", ModType.HYPIXEL), "/MightMinerV2.7/config.json");
        initialize();

        // Register keybinds
        registerKeyBind(routeBuilder, () -> RouteBuilder.getInstance().toggle());
        registerKeyBind(toggleMacro, () -> MacroManager.getInstance().toggle());

        save();
    }

    // <editor-fold desc="General">
    @Dropdown(
            name = "Macro Type",
            category = GENERAL,
            description = "Select the macro type you want to use",
            options = {
                    "Dwarven Commission",           // 0
                    "Glacial Commissions",          // 1
                    "Mining Macro",                 // 2
                    "Route Miner",                  // 3
//                    "Gemstone Powder"
            },
            subcategory = "Macro"
    )
    public static int macroType = 0;

    @KeyBind(
            name = "Toggle Macro",
            category = GENERAL,
            description = "The Button To Click To Toggle The Macro",
            subcategory = "Macro"
    )
    public static OneKeyBind toggleMacro = new OneKeyBind(Keyboard.KEY_GRAVE);

    @Text(
            name = "Mining Tool", description = "Mining tool that you use to mine blocks",
            category = GENERAL,
            placeholder = "Enter here...",
            subcategory = "Mining Tools"
    )
    public static String miningTool = "";

    @Button(
            name = "Set Mining Tool",
            text = "Set Mining tool",
            description = "Set the Mining Tool to the currently held item",
            category = GENERAL,
            subcategory = "Mining Tools"
    )
    Runnable _setMiningTool = () -> {
        ItemStack currentItem = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];

        if (currentItem == null) {
            Logger.sendMessage("Don't hold an empty hand.");
            return;
        }

        miningTool = StringUtils.stripControlCodes(currentItem.getDisplayName());
        Logger.sendMessage("Mining Tool set to: " + currentItem.getDisplayName());
    };

    @Switch(
            name = "Sneak While Mining",
            category = GENERAL,
            subcategory = "Mining Behaviour"
    )
    public static boolean sneakWhileMining = false;

    @Switch(
            name = "Use pickaxe ability", description = "Only disable this if you are below HOTM 3",
            category = GENERAL,
            subcategory = "Mining Behaviour"
    )
    public static boolean usePickaxeAbility = true;

    @Info(
            text = "You may turn off randomized rotations if you want to maximize the efficiency of precision miner",
            type = InfoType.INFO,
            category = GENERAL,
            subcategory = "Mining Behaviour",
            size = 2
    )
    public static boolean precisionMinerInfo;

    @Switch(
            name = "Precision Miner", description = "Looks at particles spawned by precision miner perk",
            category = GENERAL,
            subcategory = "Mining Behaviour"
    )
    public static boolean precisionMiner = false;

    @Switch(
            name = "Randomized rotations", description = "Randomize rotations to make them look more human",
            category = GENERAL,
            subcategory = "Mining Behaviour"
    )
    public static boolean randomizedRotations = true;

    @Slider(
            name = "Ore Respawn Wait Threshold (seconds)",
            category = GENERAL, subcategory = "Mining Behaviour",
            description = "How long to wait (in seconds) when no ores are present before stopping",
            min = 2, max = 10
    )
    public static int oreRespawnWaitThreshold = 5;

    @Switch(
            name = "Enabled (Requires abiphone!)",
            category = GENERAL,
            subcategory = "Auto Refuel"
    )
    public static boolean drillRefuel = false;

    @Dropdown(
            name = "Machine Fuel",
            category = GENERAL,
            subcategory = "Auto Refuel",
            options = {"Volta", "Oil Barrel"}
    )
    public static int refuelMachineFuel = 1;

    @Switch(
            name = "Ungrab Mouse", description = "Does not work for some Mac players",
            category = GENERAL,
            subcategory = "Miscellaneous"
    )
    public static boolean ungrabMouse = false;

    @Switch(
            name = "Mute Game", description = "Mute Game",
            category = GENERAL,
            subcategory = "Miscellaneous"
    )
    public static boolean muteGame = true;

    @Switch(
            name = "Full Block Hitbox",
            category = GENERAL,
            description = "Gives a full block hitbox to blocks without a full block hitbox",
            subcategory = "Miscellaneous"
    )
    public static boolean miscFullBlock = false;


    //</editor-fold>


    //<editor-fold desc="Commission">

    @Info(
            text = "You MUST use Royal Pigeon for Glacial Commissions!",
            type = InfoType.WARNING,
            category = COMMISSION,
            subcategory = "General"
    )
    public static boolean ignored0 = true;

    @Dropdown(
            name = "Claim Method", category = COMMISSION,
            options = {"NPC", "Royal Pigeon"},
            subcategory = "General"
    )
    public static int commClaimMethod = 0;

    @Switch(
            name = "Prioritise Titanium",
            description = "Always mine Titanium, even if it is not the commission",
            category = COMMISSION,
            subcategory = "General"
    )
    public static boolean prioritiseTitanium = false;

    @Text(
            name = "Alt. Mining Tool",
            category = COMMISSION,
            placeholder = "Enter here...",
            subcategory = "Commission claiming"
    )
    public static String altMiningTool = "";

    @Button(
            name = "Set Alt. Mining Tool",
            text = "Set Alt. Mining tool",
            description = "Set the Alternative Mining Tool to the currently held item",
            category = COMMISSION,
            subcategory = "Commission claiming"
    )
    Runnable _setAltMiningTool = () -> {
        ItemStack currentItem = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];

        if (currentItem == null) {
            Logger.sendMessage("Don't hold an empty hand.");
            return;
        }

        altMiningTool = StringUtils.stripControlCodes(currentItem.getDisplayName());
        Logger.sendMessage("Alternative Mining Tool set to: " + currentItem.getDisplayName());
    };

    @Switch(
            name = "Swap before claiming commission",
            description = "Swaps to the alternative mining tool before claiming the commission",
            category = COMMISSION,
            subcategory = "Commission claiming"

    )
    public static boolean commSwapBeforeClaiming = false;

    @Text(
            name = "Slayer Weapon", description = "Weapon used when killing goblins",
            category = COMMISSION,
            subcategory = "Mob Killer",
            placeholder = "Enter here..."
    )
    public static String slayerWeapon = "";

    @Button(
            name = "Set Slayer Weapon",
            text = "Set Slayer Weapon",
            description = "Set the Slayer Weapon to the currently held item",
            category = COMMISSION,
            subcategory = "Mob Killer"
    )
    Runnable _setSlayerWeapon = () -> {
        ItemStack currentItem = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];

        if (currentItem == null) {
            Logger.sendMessage("Don't hold an empty hand.");
            return;
        }

        slayerWeapon = StringUtils.stripControlCodes(currentItem.getDisplayName()).replaceAll("[^\\x20-\\x7E]", "");
        Logger.sendMessage("Slayer Weapon set to: " + currentItem.getDisplayName());
    };

    @Switch(
            name = "Sprint During MobKiller", description = "Allow Sprinting while Mob Killer is active (looks sussy)",
            category = COMMISSION,
            subcategory = "Mob Killer"
    )
    public static boolean mobKillerSprint = true;

    @Switch(
            name = "Interpolate During MobKiller", description = "Allows more natural movement",
            category = COMMISSION,
            subcategory = "Mob Killer"
    )
    public static boolean mobKillerInterpolate = true;

    @Switch(
            name = "Warp to forge during pathing", description = "If next commission is closer from the forge, it will warp and path from there",
            category = COMMISSION,
            subcategory = "Pathing"
    )
    public static boolean forgePathing = true;

    @Switch(
            name = "Show Commission HUD outside mines", description = "Toggle HUD Visibility outside of dwarven mines",
            category = COMMISSION,
            subcategory = "HUD"
    )
    public static boolean showDwarvenCommHUDOutside = true;

    @Exclude
    @HUD(
            name = "CommissionHUD",
            category = COMMISSION,
            subcategory = "HUD"
    )
    public static CommissionHUD commissionHUD = CommissionHUD.getInstance();

    //</editor-fold>

    //<editor-fold desc="Glacial Commission">
    @Info(
            text = "Set the threshold of coldness to where the macro will warp back to the base",
            type = InfoType.INFO,
            category = GLACIAL_COMMISSION,
            subcategory = "General",
            size = 2
    )
    public static boolean coldThresholdInfo;

    @Slider(
            name = "Cold Threshold",
            category = GLACIAL_COMMISSION,
            subcategory = "General",
            description = "The threshold of coldness to where the macro will warp back to the base",
            min = 1, max = 100
    )
    public static int coldThreshold = 50;

    @Switch(
            name = "Reset Stats When Disabled",
            category = GLACIAL_COMMISSION,
            subcategory = "HUD"
    )
    public static boolean glacialHudResetStats = false;

    @Switch(
            name = "Show Glacial HUD Outside Glacial Mines",
            category = GLACIAL_COMMISSION,
            subcategory = "HUD"
    )
    public static boolean showGlacialHUDOutside = true;

    @Exclude
    @HUD(
            name = "Glacial Commission HUD",
            category = GLACIAL_COMMISSION,
            subcategory = "HUD"
    )
    public static GlacialCommissionHUD glacialCommissionHUD = GlacialCommissionHUD.getInstance();

    //</editor-fold>

    //<editor-fold desc="Mining Macro">

    @Dropdown(
            name = "Ore Type",
            category = MINING_MACRO,
            options = {
                    "Mithril",
                    "Diamond",
                    "Emerald",
                    "Redstone",
                    "Lapis",
                    "Gold",
                    "Iron",
                    "Coal",
            }
    )
    public static int oreType = 0;


    @Switch(
            name = "Mine Gray Mithril (Gray Wool)",
            category = MINING_MACRO,
            subcategory = "Mithril Macro"
    )
    public static boolean mineGrayMithril = true;

    @Switch(
            name = "Mine Green Mithril (Prismarine)",
            category = MINING_MACRO,
            subcategory = "Mithril Macro"
    )
    public static boolean mineGreenMithril = true;

    @Switch(
            name = "Mine Blue Mithril (Blue Wool)",
            category = MINING_MACRO,
            subcategory = "Mithril Macro"
    )
    public static boolean mineBlueMithril = true;

    @Switch(
            name = "Mine Titanium",
            category = MINING_MACRO,
            subcategory = "Mithril Macro"
    )
    public static boolean mineTitanium = true;

    //</editor-fold>

    //<editor-fold desc="Route Miner">

    @Info(
            text = "Make sure to report bugs in #bug-reports in the discord server.",
            type = InfoType.ERROR,
            category = ROUTE_MINER,
            size = 2,
            subcategory = "General"
    )
    public static boolean ignored1 = true;

    @Info(
            text = "Run /rb for more information on route building.",
            type = InfoType.INFO,
            category = ROUTE_MINER,
            size = 2,
            subcategory = "General"
    )
    public static boolean ignored2 = true;

    //<editor-fold desc="General">

    @Text(name = "Selected Route", category = ROUTE_MINER, subcategory = "General")
    public static String selectedRoute = "";

    //</editor-fold>
    //<editor-fold desc="Targets">

    @Switch(name = "Mine Gemstone", category = ROUTE_MINER, subcategory = "Targets")
    public static boolean routeMineGemstone = false;

    @Switch(name = "Mine Topaz", category = ROUTE_MINER, subcategory = "Targets")
    public static boolean routeMineTopaz = false;

    @Switch(name = "Mine Ore", category = ROUTE_MINER, subcategory = "Targets")
    public static boolean routeMineOre = false;

    @Switch(name = "Mine Glacite", category = ROUTE_MINER, subcategory = "Targets")
    public static boolean routeMineGlacite = false;

    @Switch(name = "Mine Umber", category = ROUTE_MINER, subcategory = "Targets")
    public static boolean routeMineUmber = false;

    @Switch(name = "Mine Tungsten", category = ROUTE_MINER, subcategory = "Targets")
    public static boolean routeMineTungsten = false;

    //</editor-fold>
    //<editor-fold desc="Route Builder">

    @KeyBind(
            name = "Enable RouteBuilder",
            description = "They key to click to enable RouteBuilder",
            category = ROUTE_MINER,
            subcategory = "Route Builder"
    )
    public static OneKeyBind routeBuilder = new OneKeyBind(Keyboard.KEY_LBRACKET);

    @KeyBind(
            name = "Add Block To Route (Walk)",
            description = "The Key to click to add the block player is standing on block to the route",
            category = ROUTE_MINER,
            subcategory = "Route Builder"
    )
    public static OneKeyBind routeBuilderWalkAddKeybind = new OneKeyBind(Keyboard.KEY_P);

    @KeyBind(
            name = "Add Block To Route (Etherwarp)",
            description = "The Key to click to add the block player is standing on block to the route",
            category = ROUTE_MINER,
            subcategory = "Route Builder"
    )
    public static OneKeyBind routeBuilderEtherwarpAddKeybind = new OneKeyBind(Keyboard.KEY_I);

    @KeyBind(
            name = "Remove Block From Route",
            description = "The Key To Remove the block player is standing on from the route",
            category = ROUTE_MINER,
            subcategory = "Route Builder"
    )
    public static OneKeyBind routeBuilderRemoveKeybind = new OneKeyBind(Keyboard.KEY_O);

    @Color(
            name = "Route Node Color",
            description = "The Color of The Blocks On a Route",
            category = ROUTE_MINER,
            subcategory = "Route Builder"
    )
    public static OneColor routeBuilderNodeColor = new OneColor(0, 255, 255, 100);

    @Color(
            name = "Route Tracer Color",
            description = "The Color of The Line Between Blocks On a Route",
            category = ROUTE_MINER,
            subcategory = "Route Builder"
    )
    public static OneColor routeBuilderTracerColor = new OneColor(0, 255, 255, 100);

    @KeyBind(
            name = "RouteBuilder Add UNIDI",
            category = ROUTE_MINER, subcategory = "Graph"
    )
    public static OneKeyBind routeBuilderUnidi = new OneKeyBind(Keyboard.KEY_NUMPAD7);

    @KeyBind(
            name = "RouteBuilder Add BIDI",
            category = ROUTE_MINER, subcategory = "Graph"
    )
    public static OneKeyBind routeBuilderBidi = new OneKeyBind(Keyboard.KEY_NUMPAD8);

    @KeyBind(
            name = "RouteBuilder Select",
            category = ROUTE_MINER, subcategory = "Graph"
    )
    public static OneKeyBind routeBuilderSelect = new OneKeyBind(Keyboard.KEY_NUMPAD4);

    @KeyBind(
            name = "RouteBuilder Move",
            category = ROUTE_MINER, subcategory = "Graph"
    )
    public static OneKeyBind routeBuilderMove = new OneKeyBind(Keyboard.KEY_NUMPAD5);

    @KeyBind(
            name = "RouteBuilder Delete",
            category = ROUTE_MINER, subcategory = "Graph"
    )
    public static OneKeyBind routeBuilderDelete = new OneKeyBind(Keyboard.KEY_NUMPAD6);

    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Powder Macro">
    @Info(
            text = "This feature is currently in development and currently does not function.",
            type = InfoType.ERROR,
            category = POWDER,
            size = 2
    )
    public static boolean ignored3 = true;

    @Switch(
            name = "Great Explorer Maxed", category = POWDER, subcategory = "General",
            description = "Is it maxed?"
    )
    public static boolean greatExplorerMaxed = false;

    @Slider(
            name = "Center Pitch", category = POWDER, subcategory = "Ranges",
            description = "The pitch you look at the center of the screen.",
            min = 0, max = 20
    )
    public static int gemstoneCenterPitch = 10;

    @Slider(
            name = "Yaw Range", category = POWDER, subcategory = "Ranges",
            description = "The range of yaw you look at while mining.",
            min = 0, max = 90
    )
    public static int gemstoneYawRange = 45;

    @Slider(
            name = "Pitch Range", category = POWDER, subcategory = "Ranges",
            description = "The range of pitch you look at while mining.",
            min = 0, max = 30
    )
    public static int gemstonePitchRange = 15;

    @Slider(
            name = "Oval Speed", category = POWDER, subcategory = "Ranges",
            description = "The speed you rotate your mouse in an oval shape while mining.",
            min = 1f, max = 5f
    )
    public static float gemstoneOvalSpeed = 2;

    //</editor-fold>

    //<editor-fold desc="Delays">

    @Slider(
            name = "Rotation Time",
            category = DELAY, subcategory = "General",
            description = "Time it takes to rotate to the next block while mining mithril",
            min = 50, max = 1000
    )
    public static int rotationTime = 300;

    @Slider(
            name = "Rotation Time Randomization",
            category = DELAY, subcategory = "General",
            min = 50, max = 1000
    )
    public static int rotationTimeRandomizer = 300;

    @Slider(
            name = "Tick Glide Offset",
            category = DELAY, subcategory = "General",
            min = 0, max = 10
    )
    public static int tickGlideOffset = 4;

    @Slider(
            name = "Sneak Time",
            category = DELAY, subcategory = "General",
            min = 0, max = 2000
    )
    public static int sneakTime = 500;

    @Slider(
            name = "Sneak Time Randomizer",
            category = DELAY, subcategory = "General",
            min = 0, max = 2000
    )
    public static int sneakTimeRandomizer = 300;

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

    @Slider(
            name = "Aotv Look Delay (Right Click)",
            description = "Rotation time to look at next block while aotving",
            category = DELAY,
            subcategory = "Auto Aotv",
            min = 50, max = 1000
    )
    public static int delayAutoAotvLookDelay = 250;

    @Slider(
            name = "Aotv Look Delay (Etherwarp)",
            description = "Rotation time to look at next block while Etherwarping",
            category = DELAY,
            subcategory = "Auto Aotv",
            min = 50, max = 2000
    )
    public static int delayAutoAotvEtherwarpLookDelay = 500;

    @Slider(
            name = "Server Side Rotation Time",
            description = "Rotation time to look at next block with client side rotation",
            category = DELAY,
            subcategory = "Auto Aotv",
            min = 0, max = 2000
    )
    public static int delayAutoAotvServerRotation = 500;

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
    @Exclude
    public static DebugHUD debugHUD = DebugHUD.getInstance();

    @Slider(
            name = "Mining Coefficient",
            category = DEBUG,
            subcategory = "Block Miner",
            min = 0, max = 1000
    )
    public static int miningCoefficient = 200;

    @Slider(
            name = "Angle Coefficient",
            category = DEBUG,
            subcategory = "Block Miner",
            min = 0, max = 10
    )
    public static int angleCoefficient = 1;

    @Slider(
            name = "Distance Coefficient",
            category = DEBUG,
            subcategory = "Block Miner",
            min = 0, max = 20
    )
    public static int distanceCoefficient = 10;

    @Slider(
            name = "MobKiller Dist Cost",
            category = DEBUG,
            subcategory = "MobKiller",
            min = 0, max = 1000
    )
    public static int devMKillDist = 100;

    @Slider(
            name = "MobKiller Rot Cost",
            category = DEBUG,
            subcategory = "MobKiller",
            min = 0, max = 1000
    )
    public static int devMKillRot = 5;

    @Text(
            name = "MobKiller Mob Name",
            category = DEBUG,
            subcategory = "MobKiller",
            placeholder = "Enter Mobname Here"
    )
    public static String devMKillerMob = "Goblin";

    @Text(
            name = "MobKiller Weapon",
            category = DEBUG,
            subcategory = "MobKiller",
            placeholder = "Enter Weapon Name Here"
    )
    public static String devMKillerWeapon = "Aspect of the Dragon";

    @Slider(
            name = "MobKiller Check Timer",
            category = DEBUG,
            subcategory = "MobKiller",
            min = 0, max = 5000
    )
    public static int devMKillTimer = 1000;

    @Slider(
            name = "Rotation Curve",
            category = DEBUG,
            subcategory = "Cost",
            min = 0, max = 5
    )
    public static int cost4 = 1;

    @Switch(
            name = "Use Fixed Rotation Time",
            category = DEBUG,
            subcategory = "Path"
    )
    public static boolean fixrot = false;

    @Slider(
            name = "Rotation time",
            category = DEBUG,
            subcategory = "Path",
            min = 0, max = 2000
    )
    public static int rottime = 500;

    @Slider(
            name = "Rota mult",
            category = DEBUG,
            subcategory = "Path",
            min = 0f, max = 10f
    )
    public static float rotmult = 2f;

    //</editor-fold>
    //<editor-fold desc="Failsafe">

    @Switch(
            name = "Enable Failsafe Trigger Sound", category = FAILSAFE, size = OptionSize.DUAL,
            subcategory = "General",
            description = "Makes a sound when a failsafe has been triggered"
    )
    public static boolean enableFailsafeSound = true;

    @Slider(
            name = "Time to wait before toggling failsafe(in ms)",
            category = FAILSAFE,
            subcategory = "General",
            min = 0, max = 15000
    )
    public static int failsafeToggleDelay = 3000;

    @Slider(
            name = "Vertical Knockback Threshold",
            category = FAILSAFE,
            subcategory = "General",
            min = 3000, max = 10000
    )
    public static int verticalKnockbackThreshold = 4000;

    @DualOption(
            name = "Name Mention Failsafe Behaviour", category = FAILSAFE, subcategory = "Failsafe Behaviour",
            description = "The action Name Mention Failsafe will take when your name is mentioned in chat",
            left = "Pause Macro",
            right = "Change Lobby",
            size = 1
    )
    public static boolean nameMentionFailsafeBehaviour = false;

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
    public static int failsafeSoundTimes = 10;

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
            name = "Play Sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "Plays the selected sound",
            text = "Play"
    )
    Runnable _playFailsafeSoundButton = () -> AudioManager.getInstance().playSound();

    @Button(
            name = "Stop Sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "Stops playing the selected sound",
            text = "Stop"
    )
    Runnable _stopFailsafeSoundButton = () -> AudioManager.getInstance().resetSound();
    //</editor-fold>

    public static int getRandomRotationTime() {
        return rotationTime + (int) (Math.random() * rotationTimeRandomizer);
    }

    public static int getRandomSneakTime() {
        return sneakTime + (int) (Math.random() * sneakTimeRandomizer);
    }

    public static int getRandomGuiWaitDelay() {
        return delaysGuiDelay + (int) (Math.random() * delaysGuiDelayRandomizer);
    }

}
