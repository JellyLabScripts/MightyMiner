package com.jelly.MightyMiner.config;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.data.PageLocation;
import cc.polyfrost.oneconfig.config.migration.VigilanceMigrator;
import cc.polyfrost.oneconfig.config.migration.VigilanceName;
import com.jelly.MightyMiner.gui.AOTVWaypointsPage;
import com.jelly.MightyMiner.hud.MobKillerHUD;
import com.jelly.MightyMiner.macros.macros.CommissionMacro;

public class Config extends cc.polyfrost.oneconfig.config.Config {

    //region PAGES

    private transient static final String CORE = "Core";
    private transient static final String GEMSTONE_MACRO = "Gemstone macro";
    private transient static final String POWDER_MACRO = "Powder macro";
    private transient static final String AOTV_MACRO = "AOTV macro";
    private transient static final String MITHRIL_MACRO = "Mithril macro";
    private transient static final String COMMISSION_MACRO = "Commission macro";
    private transient static final String FAILSAFES = "Failsafes";
    private transient static final String ADDONS = "Addons";


    //endregion

    @VigilanceName(name = "Macro", category = "Core", subcategory = "Macro")
    @Dropdown(
            name = "Macro", category = CORE,
            subcategory = "Macro",
            options = { "Gemstone macro", "Powder macro", "Mithril macro", "AOTV Gemstone macro", "Commission macro"}
    )
    public int macroType = 0;

    @VigilanceName(name = "Use mining speed boost", category = "Core", subcategory = "Mining")
    @Switch(name = "Use mining speed boost", category = CORE, subcategory = "Mining")
    public boolean useMiningSpeedBoost = true;

    @VigilanceName(name = "Blue cheese omelette pickaxe switch", category = "Core", subcategory = "Mining")
    @Switch(name = "Blue cheese omelette pickaxe switch", description = "Automatically switches to the pickaxe with blue cheese omelette when using mining speed boost", category = CORE, subcategory = "Mining")
    public boolean blueCheeseOmeletteToggle = false;

    @VigilanceName(name = "Macro", category = "Core", subcategory = "Macro")
    @Switch(name = "Make glass panes as a full blocks", category = CORE, subcategory = "Mining")
    public boolean glassPanesFullBlock = false;

    @VigilanceName(name = "Auto renew crystal hollows pass before expire", category = "Core", subcategory = "Crystal hollows")
    @Switch(name = "Auto renew crystal hollows pass before expire", category = CORE, subcategory = "Crystal hollows", description = "Will automatically renew the crystal hollows pass before it expires")
    public boolean autoRenewCrystalHollowsPass = true;

    @VigilanceName(name = "Refuel with abiphone", category = "Core", subcategory = "Refuel")
    @Switch(name = "Refuel with abiphone", category = CORE, subcategory = "Refuel")
    public boolean refuelWithAbiphone = false;

    @VigilanceName(name = "Refuel if less than", category = "Core", subcategory = "Refuel")
    @Slider(name = "Refuel if less than", category = CORE, subcategory = "Refuel", max = 3000, step = 50, min = 0.0F)
    public int refuelThreshold = 200;

    @VigilanceName(name = "Type of fuel to use", category = "Core", subcategory = "Refuel")
    @Dropdown(name = "Type of fuel to use", category = CORE, subcategory = "Refuel", options = {"Goblin Egg", "Biofuel", "Volta", "Oil Barrel"})
    public int typeOfFuelIndex = 0;

    @VigilanceName(name = "Debug mode", category = "Core", subcategory = "Macro")
    @Switch(name = "Debug mode", category = CORE, subcategory = "Macro", description = "Shows logs")
    public boolean debugLogMode = false;

    @VigilanceName(name = "Toggle mouse ungrab", category = "Core", subcategory = "Macro")
    @Switch(name = "Toggle mouse ungrab", description = "May not work on some computers", category = CORE, subcategory = "Macro")
    public boolean mouseUngrab = false;

    @VigilanceName(name = "Stuck time threshold (seconds)", category = GEMSTONE_MACRO, subcategory = "Miscellaneous")
    @Slider(name = "Stuck time threshold (seconds)", description = "restarts macro when stuck time > threshold", category = GEMSTONE_MACRO, subcategory = "Miscellaneous", max = 20, min = 3)
    public int gemRestartTimeThreshold = 8;

    @VigilanceName(name = "Max y level", category = GEMSTONE_MACRO, subcategory = "Pathfinding")
    @Slider(name = "Max y level", category = GEMSTONE_MACRO, subcategory = "Pathfinding", max = 256, min = 0)
    public int gemMaxY = 256;

    @VigilanceName(name = "Min y level", category = GEMSTONE_MACRO, subcategory = "Pathfinding")
    @Slider(name = "Min y level", category = GEMSTONE_MACRO, subcategory = "Pathfinding", max = 25, min = 0)
    public int gemMinY = 0;

    @VigilanceName(name = "Auto open chest", category = GEMSTONE_MACRO, subcategory = "Miscellaneous")
    @Switch(name = "Auto open chest", category = GEMSTONE_MACRO, subcategory = "Miscellaneous")
    public boolean gemOpenChest = false;

    @VigilanceName(name = "Rotation time (milliseconds)", category = GEMSTONE_MACRO, subcategory = "Pathfinding")
    @Slider(name = "Rotation time (milliseconds)", description = "Time the macro takes for each rotation", category = GEMSTONE_MACRO, subcategory = "Pathfinding", max = 500, min = 50, step = 10)
    public int gemRotationTime = 300;

    @VigilanceName(name = "Type of gemstone to mine", category = GEMSTONE_MACRO, subcategory = "Mining")
    @Dropdown(name = "Type of gemstone to mine", category = GEMSTONE_MACRO, subcategory = "Mining", options = {"Any", "Ruby", "Amethyst", "Jade", "Sapphire", "Amber", "Topaz", "Jasper"})
    public int gemGemstoneType = 0;

    @VigilanceName(name = "RGA hardstone aura", category = POWDER_MACRO, subcategory = "RGA Nuker")
    @Switch(name = "RGA hardstone aura", description = "Mines hard stone around you. USE WITH center to block and optionally make gemstones full block (Core)", category = POWDER_MACRO, subcategory = "RGA Nuker")
    public boolean powNuker = false;

    @VigilanceName(name = "Hardstone aura height", category = POWDER_MACRO, subcategory = "RGA Nuker")
    @Slider(name = "Hardstone aura height", category = POWDER_MACRO, subcategory = "RGA Nuker", max = 4, min = 2)
    public int powNukerHeight = 3;

    @VigilanceName(name = "Hardstone aura ", category = POWDER_MACRO, subcategory = "RGA Nuker")
    @Dropdown(name = "Hardstone aura",
            category = POWDER_MACRO,
            subcategory = "RGA Nuker",
            options = { "Blocks around", "Facing axis"}
    )
    public int powNukerType = 0;

    @VigilanceName(name = "Mine gemstones", category = POWDER_MACRO, subcategory = "Mining")
    @Switch(name = "Mine gemstones", description = "Make sure you have a drill that is able to mine gemstones", category = POWDER_MACRO, subcategory = "Mining")
    public boolean powMineGemstone = true;

    @Slider(name = "Rotation rate", description = "The higher the rotation rate, the faster you'll rotate (depends on computer)", category = POWDER_MACRO, subcategory = "Mining", max = 15, min = 1)
    public int powRotateRate = 7;

    @Slider(name = "Rotation radius", description = "The radius of the circle being dug out (e.g. 20 = 2 block radius)", category = POWDER_MACRO, subcategory = "Mining", max = 3.5f, min = 1.2f)
    public float powRotateRadius = 2f;

    @VigilanceName(name = "Use pickaxe to mine hardstone", category = POWDER_MACRO, subcategory = "Miscellaneous")
    @Switch(name = "Use pickaxe to mine hardstone", category = POWDER_MACRO, subcategory = "Miscellaneous")
    public boolean powPickaxeSwitch = true;

    @VigilanceName(name = "Switch to blue cheese drill when solving chest", category = POWDER_MACRO, subcategory = "Miscellaneous")
    @Switch(name = "Switch to blue cheese drill when solving chest", description = "Gives more powder, but make sure you have a blue cheese drill in your hotbar", category = POWDER_MACRO, subcategory = "Miscellaneous")
    public boolean powBlueCheeseSwitch = true;

    @VigilanceName(name = "Center to block", category = POWDER_MACRO, subcategory = "Miscellaneous")
    @Switch(name = "Center to block", description = "Center to the middle of block using AOTE or AOTV when necessary. Please turn this on if you're not using nuker.", category = POWDER_MACRO, subcategory = "Miscellaneous")
    public boolean powCenter = false;

    @VigilanceName(name = "Width between each lane", category = POWDER_MACRO, subcategory = "Mining")
    @Slider(name = "Width between each lane", category = POWDER_MACRO, subcategory = "Mining", max = 15, min = 3)
    public int powLaneWidth = 6;

    @VigilanceName(name = "Width between each lane", category = POWDER_MACRO, subcategory = ADDONS)
    @Switch(name = "Autosell junk items", description = "More configurations in Addons. Make sure you have cookie on", category = POWDER_MACRO, subcategory = ADDONS)
    public boolean powAutosell = false;

    @VigilanceName(name = "Mithril macro priority 1", category = MITHRIL_MACRO, subcategory = "Mining")
    @Dropdown(name = "Mithril macro priority 1", category = MITHRIL_MACRO, subcategory = "Mining", options = { "Clay / Gray Wool", "Prismarine", "Blue Wool", "Titanium"}, size = 2)
    public int mithPriority1 = 1;

    @VigilanceName(name = "Mithril macro priority 2", category = MITHRIL_MACRO, subcategory = "Mining")
    @Dropdown(name = "Mithril macro priority 2", category = MITHRIL_MACRO, subcategory = "Mining", options = { "Clay / Gray Wool", "Prismarine", "Blue Wool", "Titanium"}, size = 2)
    public int mithPriority2 = 2;

    @VigilanceName(name = "Mithril macro priority 3", category = MITHRIL_MACRO, subcategory = "Mining")
    @Dropdown(name = "Mithril macro priority 3", category = MITHRIL_MACRO, subcategory = "Mining", options = { "Clay / Gray Wool", "Prismarine", "Blue Wool", "Titanium"}, size = 2)
    public int mithPriority3 = 0;

    @VigilanceName(name = "Mithril macro priority 4", category = MITHRIL_MACRO, subcategory = "Mining")
    @Dropdown(name = "Mithril macro priority 4", category = MITHRIL_MACRO, subcategory = "Mining", options = { "Clay / Gray Wool", "Prismarine", "Blue Wool", "Titanium"}, size = 2)
    public int mithPriority4 = 0;

    @VigilanceName(name = "Shift when mining", category = MITHRIL_MACRO, subcategory = "Miscellaneous")
    @Switch(name = "Shift when mining", category = MITHRIL_MACRO, subcategory = "Miscellaneous")
    public boolean mithShiftWhenMine = true;

    @VigilanceName(name = "Rotation time (milliseconds)", category = MITHRIL_MACRO, subcategory = "Pathfinding")
    @Slider(name = "Rotation time (milliseconds)", description = "Time the macro takes for each rotation", category = MITHRIL_MACRO, subcategory = "Pathfinding", max = 500, min = 50)
    public int mithRotationTime = 300;

    @VigilanceName(name = "Stuck time threshold (seconds)", category = MITHRIL_MACRO, subcategory = "Pathfinding")
    @Slider(name = "Stuck time threshold (seconds)", description = "restarts macro when stuck time > threshold, depends on your mining speed", category = MITHRIL_MACRO, subcategory = "Pathfinding", max = 20, min = 2)
    public int mithRestartTimeThreshold = 5;

    /*@Property( type = PropertyType.SLIDER, name = "Rotation time (milliseconds)", description = "Time the pathfinding AI takes for each rotation", category = "Commission macro", subcategory = "Miscellaneous", max = 5, min = 1)
    public int comBarRotationTime = 1;

    @Property(type = PropertyType.SLIDER, name = "Safewalk index", description = "Stops walking when there is a large rotation. TURN THIS UP IF you are using high speed", category = "Commission macro", subcategory = "Miscellaneous", max = 10)
    public int comBarSafeIndex = 5;*/
    @VigilanceName(name = "Rotation time in ms", category = COMMISSION_MACRO, subcategory = "Mining")
    @Slider(name = "Rotation time in ms", description = "Time the macro takes for each rotation", category = COMMISSION_MACRO, subcategory = "Mining", max = 1200, min = 50)
    public int commRotationTime = 800;

    @VigilanceName(name = "Stuck time threshold in s", category = COMMISSION_MACRO, subcategory = "Mining")
    @Slider(name = "Stuck time threshold in s", description = "restarts macro when stuck time > threshold, depends on your mining speed", category = COMMISSION_MACRO, subcategory = "Mining", max = 10, min = 2)
    public int commRestartTimeThreshold = 5;

    @VigilanceName(name = "Shift when mining", category = COMMISSION_MACRO, subcategory = "Mining")
    @Switch(name = "Shift when mining", category = COMMISSION_MACRO, subcategory = "Mining")
    public boolean commShiftWhenMine = true;

    @VigilanceName(name = "Camera speed to waypoint in ms", category = COMMISSION_MACRO, subcategory = "Warping")
    @Slider(name = "Camera speed to waypoint in ms", category = COMMISSION_MACRO, subcategory = "Warping", max = 1500, min = 1, step = 10)
    public int commCameraWaypointSpeed = 800;

    @VigilanceName(name = "MobKiller camera speed in ms", category = COMMISSION_MACRO, subcategory = "Ice Walker / Goblin Killer")
    @Slider(name = "MobKiller camera speed in ms", category = COMMISSION_MACRO, subcategory = "Ice Walker / Goblin Killer", max = 1000, min = 1)
    public int commKillerCameraSpeed = 100;

    @VigilanceName(name = "MobKiller delay between attacks in ms", category = COMMISSION_MACRO, subcategory = "Ice Walker / Goblin Killer")
    @Slider(name = "MobKiller delay between attacks in ms", category = COMMISSION_MACRO, subcategory = "Ice Walker / Goblin Killer", max = 1000, min = 1)
    public int commKillerAttackDelay = 150;

    @VigilanceName(name = "MobKiller scan distance", category = COMMISSION_MACRO, subcategory = "Ice Walker / Goblin Killer")
    @Slider( name = "MobKiller scan distance", category = COMMISSION_MACRO, subcategory = "Ice Walker / Goblin Killer", max = 30, min = 1)
    public int commKillerScanRange = 10;

    @VigilanceName(name = "Arrive check wait time in ms", category = COMMISSION_MACRO, subcategory = "Warping")
    @Slider( name = "Arrive check wait time in ms", category = COMMISSION_MACRO, subcategory = "Warping", max = 750, min = 200)
    public int commArriveWaitTime = 250;

    @VigilanceName(name = "Stop on limbo", category = COMMISSION_MACRO, subcategory = "Failsafe")
    @Switch(name = "Stop on limbo", description = "Stop macro when getting kicked to limbo", category = COMMISSION_MACRO, subcategory = "Failsafe")
    public boolean stopOnLimbo = true;

    @VigilanceName(name = "Mana regeneration time in s", category = COMMISSION_MACRO, subcategory = "Failsafe")
    @Slider(name = "Mana regeneration time in s", description = "Time to regenerate mana in s", category = COMMISSION_MACRO, subcategory = "Failsafe", max = 35, min = 1)
    public int manaRegenTime = 20;

    @VigilanceName(name = "Player FOV", category = COMMISSION_MACRO, subcategory = "Failsafe")
    @Slider(name = "Player FOV", description = "Player FOV", category = COMMISSION_MACRO, subcategory = "Failsafe", max = 110, min = 30)
    public int playerFov = 80;


    @Page(name = "List of waypoints", location = PageLocation.TOP, category = AOTV_MACRO, subcategory = "Waypoints")
    public AOTVWaypointsPage aotvWaypointsPage = new AOTVWaypointsPage();

    @VigilanceName(name = "Camera speed to ore in ms", category = AOTV_MACRO, subcategory = "Mechanics")
    @Slider(name = "Camera speed to ore in ms", category = AOTV_MACRO, subcategory = "Mechanics", max = 1500, min = 1, step = 10)
    public int aotvCameraSpeed = 100;

    @VigilanceName(name = "Camera speed to waypoint in ms", category = AOTV_MACRO, subcategory = "Mechanics")
    @Slider(name = "Camera speed to waypoint in ms", category = AOTV_MACRO, subcategory = "Mechanics", max = 1500, min = 1, step = 10)
    public int aotvCameraWaypointSpeed = 100;

    @VigilanceName(name = "Teleport time threshold (s)", category = AOTV_MACRO, subcategory = "Mechanics")
    @Slider(name = "Teleport time threshold (s)",
            description = "Stops macro if it teleports between routes too fast. (If there is no veins on the spot, macro will teleport to the next and to the next etc)", category = AOTV_MACRO, subcategory = "Mechanics", min = 0f, max = 3f)
    public float aotvTeleportThreshold = 1.5f;

    @VigilanceName(name = "Stuck time threshold (seconds)", category = AOTV_MACRO, subcategory = "Mechanics")
    @Slider(name = "Stuck time threshold (seconds)", description = "Restarts macro when stuck time > threshold, depends on your mining speed", category = AOTV_MACRO, subcategory = "Mechanics", max = 20, min = 2)
    public int aotvRestartTimeThreshold = 5;

    @VigilanceName(name = "Space from edge block to the center for accuracy checks", category = AOTV_MACRO, subcategory = "Targeting")
    @Slider(name = "Space from edge block to the center for accuracy checks", subcategory = "Targeting", description = "Lower value means that macro will check closes to the block's edge if the block is visible", category = AOTV_MACRO, min = 0f, max = 0.5f)
    public float aotvMiningAccuracy = 0.1f;

    @VigilanceName(name = "Accuracy checks per dimension", category = AOTV_MACRO, subcategory = "Targeting")
    @Slider(name = "Accuracy checks per dimension", subcategory = "Targeting", description = "Higher value means that macro will check more times if the block is visible", category = AOTV_MACRO, min = 1, max = 16)
    public int aotvMiningAccuracyChecks = 8;

    @VigilanceName(name = "Space from cobblestone to the center", category = AOTV_MACRO, subcategory = "Targeting")
    @Slider(name = "Space from cobblestone to the center", subcategory = "Targeting", description = "Increase if macro destroys cobblestone too often", category = AOTV_MACRO, min = 0f, max = 0.35f)
    public float aotvMiningCobblestoneAccuracy = 0.15f;

    @VigilanceName(name = "Auto yog killer", category = AOTV_MACRO, subcategory = "Yogs")
    @Switch(name = "Auto yog killer", description = "Warning: Early alpha. For more configuration options go to MobKiller", category = AOTV_MACRO, subcategory = "Yogs")
    public boolean aotvKillYogs = true;

    @VigilanceName(name = "Type of gemstone to mine", category = AOTV_MACRO, subcategory = "Mining")
    @Dropdown(name = "Type of gemstone to mine", category = AOTV_MACRO, subcategory = "Mining", options = {"Any", "Ruby", "Amethyst", "Jade", "Sapphire", "Amber", "Topaz", "Jasper"})
    public int aotvGemstoneType = 0;

    @VigilanceName(name = "Stop if any cobblestone on the route has been destroyed", category = AOTV_MACRO, subcategory = "Mining")
    @Switch(name = "Stop if any cobblestone on the route has been destroyed", category = AOTV_MACRO, subcategory = "Mining")
    public boolean stopIfCobblestoneDestroyed = true;

    @VigilanceName(name = "Mine gemstone panes", category = AOTV_MACRO, subcategory = "Mining")
    @Switch(name = "Mine gemstone panes", category = AOTV_MACRO, subcategory = "Mining")
    public boolean aotvMineGemstonePanes = true;

    @VigilanceName(name = "Draw blocks blocking AOTV vision", category = AOTV_MACRO, subcategory = "Drawings")
    @Switch(name = "Draw blocks blocking AOTV vision", category = AOTV_MACRO, subcategory = "Drawings")
    public boolean drawBlocksBlockingAOTV = true;

    @VigilanceName(name = "Color of blocks blocking AOTV vision", category = AOTV_MACRO, subcategory = "Drawings")
    @Color(name = "Color of blocks blocking AOTV vision", category = AOTV_MACRO, subcategory = "Drawings")
    public OneColor aotvVisionBlocksColor = new OneColor(255, 0, 0, 120);

    @VigilanceName(name = "Show route lines", category = AOTV_MACRO, subcategory = "Drawings")
    @Switch(name = "Show route lines", category = AOTV_MACRO, subcategory = "Drawings")
    public boolean aotvShowRouteLines = true;

    @VigilanceName(name = "Color of route line", category = AOTV_MACRO, subcategory = "Drawings")
    @Color(name = "Color of route line", category = AOTV_MACRO, subcategory = "Drawings")
    public OneColor aotvRouteLineColor = new OneColor(217, 55, 55, 200);

    @VigilanceName(name = "Highlight route blocks", category = AOTV_MACRO, subcategory = "Drawings")
    @Switch(name = "Highlight route blocks", category = AOTV_MACRO, subcategory = "Drawings")
    public boolean aotvHighlightRouteBlocks = true;

    @VigilanceName(name = "Color of highlighted route blocks", category = AOTV_MACRO, subcategory = "Drawings")
    @Color(name = "Color of highlighted route blocks", category = AOTV_MACRO, subcategory = "Drawings")
    public OneColor aotvRouteBlocksColor = new OneColor(217, 55, 55, 200);

    @VigilanceName(name = "Show distance to blocks", category = AOTV_MACRO, subcategory = "Drawings")
    @Switch(name = "Show distance to blocks", category = AOTV_MACRO, subcategory = "Drawings", size = 2)
    public boolean aotvShowDistanceToBlocks = true;

    @VigilanceName(name = "MobKiller scan distance", category = ADDONS, subcategory = "MobKiller")
    @Slider( name = "MobKiller scan distance", category = ADDONS, subcategory = "MobKiller", max = 30, min = 1)
    public int mobKillerScanRange = 10;

    @VigilanceName(name = "MobKiller camera speed in ms", category = ADDONS, subcategory = "MobKiller")
    @Slider(name = "MobKiller camera speed in ms", category = ADDONS, subcategory = "MobKiller", max = 1000, min = 1)
    public int mobKillerCameraSpeed = 100;

    @VigilanceName(name = "MobKiller delay between attacks in ms", category = ADDONS, subcategory = "MobKiller")
    @Slider(name = "MobKiller delay between attacks in ms", category = ADDONS, subcategory = "MobKiller", max = 1000, min = 1)
    public int mobKillerAttackDelay = 100;

    @VigilanceName(name = "Custom item to use for MobKiller", category = ADDONS, subcategory = "MobKiller")
    @Text(name = "Custom item to use for MobKiller", description = "Leave empty to use default weapons", category = ADDONS, subcategory = "MobKiller")
    public String customItemToKill = "";

    @VigilanceName(name = "Mouse button to use in MobKiller", category = ADDONS, subcategory = "MobKiller")
    @Dropdown(name = "Mouse button to use in MobKiller", category = ADDONS, subcategory = "MobKiller", options = {"Left", "Right"})
    public int attackButton = 0;

    @VigilanceName(name = "Use Hyperion under player", category = ADDONS, subcategory = "MobKiller")
    @Switch(name = "Use Hyperion under player", category = ADDONS, subcategory = "MobKiller", size = 2)
    public boolean useHyperionUnderPlayer = false;

    @HUD(name = "MobKiller info", category = ADDONS, subcategory = "MobKiller")
    public MobKillerHUD mobKillerHud = new MobKillerHUD();

    @VigilanceName(name = "Sell wishing compass to npc", category = ADDONS, subcategory = "Autosell")
    @Switch(name = "Sell wishing compass to npc", description = "You need a booster cookie", category = ADDONS, subcategory = "Autosell")
    public boolean sellWishingCompass = true;

    @VigilanceName(name = "Sell ascension rope to npc", category = ADDONS, subcategory = "Autosell")
    @Switch(name = "Sell ascension rope to npc", description = "You need a booster cookie", category = ADDONS, subcategory = "Autosell")
    public boolean sellAscensionRope = true;

    @VigilanceName(name = "Player ESP", category = ADDONS, subcategory = "PlayerESP")
    @Switch(name = "Player ESP", category = ADDONS, subcategory = "PlayerESP")
    public boolean playerESP = true;

    @VigilanceName(name = "Player ESP color", category = ADDONS, subcategory = "PlayerESP")
    @Color(name = "Player ESP color", category = ADDONS, subcategory = "PlayerESP")
    public OneColor playerESPColor = new OneColor(255, 0, 0, 120);

    @VigilanceName(name = "Enable Player detection failsafe", category = FAILSAFES, subcategory = "PlayerESP")
    @Switch(name = "Enable Player detection failsafe", description = "Stop macro if there is a player nearby", category = FAILSAFES, subcategory = "Player detection failsafe")
    public boolean playerFailsafe = true;

    @VigilanceName(name = "Player detection radius", category = FAILSAFES, subcategory = "PlayerESP")
    @Slider(name = "Player detection radius", description = "Trigger failsafe if there is player inside the given radius of player", category = FAILSAFES, min = 1, max = 30, subcategory = "Player detection failsafe")
    public int playerRad = 10;

    @Slider(name = "Player detection threshold until disable", category = FAILSAFES, min = 1, max = 10, subcategory = "Player detection failsafe")
    public int playerDetectionThreshold = 3;

    @VigilanceName(name = "Disable macro on world change", category = FAILSAFES, subcategory = "World change failsafe")
    @Switch(name = "Disable macro on world change", description = "Disables the macro when you get teleported to another world", category = FAILSAFES, subcategory = "World change failsafe")
    public boolean disableOnWorldChange = false;

    @VigilanceName(name = "Rotation check", category = FAILSAFES, subcategory = "Rotation failsafe")
    @Switch(name = "Rotation check", description = "May give false positives", category = FAILSAFES, subcategory = "Rotation failsafe")
    public boolean stopMacrosOnRotationCheck = true;

    @Switch(name = "Show notifications on staff check", category = FAILSAFES, subcategory = "Miscellaneous")
    public boolean notifications = true;

    @Switch(name = "Play ping sound when macro is interrupted", category = FAILSAFES, subcategory = "Miscellaneous")
    public boolean pingSound = true;

    @Switch(name = "Fake movements when being staff checked", description = "You could disable this if you're always by your computer", category = FAILSAFES, subcategory = "Miscellaneous")
    public boolean fakeMovements = true;


    public Config() {
        super(new Mod("Mighty Miner", ModType.HYPIXEL, new VigilanceMigrator("mightyminer.toml")), "/mightyminer/config.json");
        initialize();

        this.addDependency("playerRad", "playerFailsafe");
        this.addDependency("playerDetectionThreshold", "playerFailsafe");
        this.addDependency("powNukerHeight", "powNuker");
        this.addDependency("powNukerType", "powNuker");
        this.addDependency("aotvRouteLineColor", "aotvShowRouteLines");
        this.addDependency("aotvRouteBlocksColor", "aotvHighlightRouteBlocks");
        this.addDependency("refuelThreshold", "refuelWithAbiphone");
        this.addDependency("typeOfFuelIndex", "refuelWithAbiphone");
        this.addDependency("aotvRouteLineColor", "aotvShowRouteLines");
        this.addDependency("aotvShowDistanceToBlocks", "aotvHighlightRouteBlocks");
        this.addDependency("aotvRouteBlocksColor", "aotvHighlightRouteBlocks");
        this.addDependency("aotvVisionBlocksColor", "drawBlocksBlockingAOTV");
        this.addDependency("playerESPColor", "playerESP");
        this.addDependency("blueCheeseOmeletteToggle", "useMiningSpeedBoost");
    }
}
