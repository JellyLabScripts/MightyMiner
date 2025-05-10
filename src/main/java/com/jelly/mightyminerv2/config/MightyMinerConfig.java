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
    private transient static final String COMMISSION = "Commission";
    private transient static final String MINING_MACRO = "Mining Macro";
    private transient static final String ROUTE_MINER = "Route Miner";
    private transient static final String POWDER = "Gemstone Powder";
    private transient static final String ROUTE_BUILDER = "Route Builder";
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
                    "Commission",
                    "Glacial Commissions",
                    "Mining Macro",
                    "Route Miner",
                    "Gemstone Powder"
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

    @Switch(
            name = "Drill Swap", description = "Drill Swap",
            category = GENERAL
    )
    public static boolean drillSwap = false;

    @Switch(
            name = "Rod Swap",
            category = GENERAL,
            description = "Rod Swap"
    )
    public static boolean rodSwap = false;

    @Switch(
            name = "Full Blocks",
            category = GENERAL,
            description = "Gives a full block hitbox to blocks without a full block hitbox"
    )
    public static boolean miscFullBlock = false;

    @Switch(
            name = "Sneak While Mining",
            category = GENERAL
    )
    public static boolean sneakWhileMining = false;

    @Switch(
            name = "Precision Miner (to be implemented)", description = "Looks at particles spawned by precision miner perk (Might/Will Mess Up TickGLide)",
            category = GENERAL
    )
    public static boolean precisionMiner = false;

    @Switch(
            name = "Disable mining speed boost", description = "Only disable this if you are < HOTM 3.",
            category = GENERAL
    )
    public static boolean disableMiningSpeedBoost = false;

    @Text(
            name = "Mining Tool", description = "Mining tool that you use to mine blocks",
            category = GENERAL,
            placeholder = "Enter here..."
    )
    public static String miningTool = "";

    @Text(
            name = "Alt Mining Tool", description = "Mining tool that you use to activate abilities and such",
            category = GENERAL,
            placeholder = "Enter here..."
    )
    public static String altMiningTool = "";

    @Button(
            name = "Mining Tool Set",
            text = "Set Mining tool",
            description = "Set the Mining Tool to the currently held item",
            category = GENERAL
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

    @Button(
            name = "Mining Tool Set",
            text = "Set Mining tool",
            description = "Set the Mining Tool to the currently held item",
            category = GENERAL
    )
    Runnable _setAltMiningTool = () -> {
        ItemStack currentItem = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];

        if (currentItem == null) {
            Logger.sendMessage("Don't hold an empty hand.");
            return;
        }

        altMiningTool = StringUtils.stripControlCodes(currentItem.getDisplayName());
        Logger.sendMessage("Mining Tool set to: " + currentItem.getDisplayName());
    };

    @Switch(
            name = "Refuel Drill (Requires abiphone!)",
            category = GENERAL,
            subcategory = "Refuel"
    )
    public static boolean commDrillRefuel = false;

    @Dropdown(
            name = "Machine Fuel",
            category = GENERAL,
            subcategory = "Refuel",
            options = {"Volta", "Oil Barrel"}
    )
    public static int commMachineFuel = 1;
    //</editor-fold>


    //<editor-fold desc="Commission">

    @Info(
            text = "Using Royal Pigion for Glacial Commissions is FORCED",
            type = InfoType.ERROR,
            category = COMMISSION,
            subcategory = "General"
    )
    public static boolean ignored0 = true;

    @Switch(
            name = "Always Mine Titanium", description = "Mines titanium even if it isnt a titanium commission",
            category = COMMISSION,
            subcategory = "General"
    )
    public static boolean commMineTitanium = false;

    @Dropdown(
            name = "Claim Method", category = COMMISSION,
            options = {"NPC", "Royal Pigeon"},
            subcategory = "General"
    )
    public static int commClaimMethod = 0;

    @Switch(
            name = "Sprint During MobKiller", description = "Allow Sprinting while mobkiller is active (looks sussy with sprint)",
            category = GENERAL,
            subcategory = "MobKiller"
    )
    public static boolean mobKillerSprint = true;

    @Switch(
            name = "Interpolate During MobKiller", description = "Helps reduce sliding",
            category = GENERAL,
            subcategory = "MobKiller"
    )
    public static boolean mobKillerInterpolate = true;

    @Text(
            name = "Slayer Weapon", description = "Weapon used when killing goblins",
            category = GENERAL,
            subcategory = "MobKiller",
            placeholder = "Enter here..."
    )
    public static String slayerWeapon = "";

    @Button(
            name = "Slayer Weapon Set",
            text = "Set Slayer Weapon",
            description = "Set the Slayer Weapon to the currently held item",
            category = GENERAL,
            subcategory = "MobKiller"
    )
    Runnable _setSlayerWeapon = () -> {
        ItemStack currentItem = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];

        if (currentItem == null) {
            Logger.sendMessage("Don't hold an empty hand.");
            return;
        }

        slayerWeapon = StringUtils.stripControlCodes(currentItem.getDisplayName());
        Logger.sendMessage("Slayer Weapon set to: " + currentItem.getDisplayName());
    };

    @Exclude
    @HUD(
            name = "CommissionHUD",
            category = COMMISSION,
            subcategory = "HUD"
    )
    public static CommissionHUD commissionHUD = CommissionHUD.getInstance();

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

    @Dropdown(
            name = "Route Target", category = ROUTE_MINER, subcategory = "Route",
            description = "What you want to mine.",
            options = {
                    // Ores
                    "Iron",
                    "Redstone",
                    "Coal",
                    "Gold",
                    "Lapis",
                    "Diamond",
                    "Emerald",
                    "Quartz",
                    // Gemstones
                    "Ruby",
                    "Sapphire",
                    "Jade",
                    "Amethyst",
                    "Topaz",
                    "Onyx",
                    "Aquamarine",
                    "Citrine",
                    "Peridot",
                    "Jasper",
                    // Gemstones
                    "Glacite",
                    "Umber",
                    "Tungsten"
            }
    )
    public static int routeTarget = 0;

    @DualOption(
            name = "Route Type", category = ROUTE_MINER, subcategory = "General",
            description = "The type of route this is.",
            left = "Regular", right = "Snail"
    )
    public static boolean routeType = false;

    //</editor-fold>

    //<editor-fold desc="Powder Macro">

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

    //<editor-fold desc="Route Builder">

    @KeyBind(
            name = "Enable RouteBuilder",
            description = "They key to click to enable RouteBuilder",
            category = ROUTE_BUILDER,
            size = 2
    )
    public static OneKeyBind routeBuilder = new OneKeyBind(Keyboard.KEY_LBRACKET);

    @KeyBind(
            name = "Add Block To Route (Aotv)",
            description = "The Key to click to add the block player is standing on block to the route",
            category = ROUTE_BUILDER
    )
    public static OneKeyBind routeBuilderAotvAddKeybind = new OneKeyBind(Keyboard.KEY_P);

    @KeyBind(
            name = "Add Block To Route (Etherwarp)",
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
            description = "Makes a sound when a failsafe has been triggered"
    )
    public static boolean enableFailsafeSound = true;

    @Slider(
            name = "Time to wait before toggling failsafe(in ms)",
            category = FAILSAFE,
            min = 0, max = 15000
    )
    public static int failsafeToggleDelay = 3000;

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