package com.jelly.MightyMiner.config;

import com.jelly.MightyMiner.features.MobKiller;
import com.jelly.MightyMiner.gui.ChangeLocationGUI;
import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.*;

import java.awt.*;
import java.io.File;

public class Config extends Vigilant {

    @Property(
            type = PropertyType.SELECTOR,
            name = "Macro", category = "Core",
            subcategory = "Macro",
            options = { "Gemstone macro", "Powder macro", "Mithril macro", "AOTV Gemstone macro"}
    )
    public int macroType = 0;

    @Property(type = PropertyType.SWITCH, name = "Use mining speed boost", category = "Core", subcategory = "Mining")
    public boolean useMiningSpeedBoost = true;


    @Property(type = PropertyType.SWITCH, name = "Make glass panes as a full blocks", category = "Core", subcategory = "Mining")
    public boolean glassPanesFullBlock = false;

    @Property(type = PropertyType.SWITCH, name = "Auto renew crystal hollows pass before expire", category = "Core", subcategory = "Crystal hollows", description = "Will automatically renew the crystal hollows pass before it expires")
    public boolean autoRenewCrystalHollowsPass = true;


    @Property(type = PropertyType.SWITCH, name = "Refuel with abiphone", category = "Core", subcategory = "Refuel")
    public boolean refuelWithAbiphone = false;

    @Property(type = PropertyType.NUMBER, name = "Refuel if less than", category = "Core", subcategory = "Refuel", max = 3000, increment = 50)
    public int refuelThreshold = 200;

    @Property(type = PropertyType.SELECTOR, name = "Type of fuel to use", category = "Core", subcategory = "Refuel", options = {"Goblin Egg", "Biofuel", "Volta", "Oil Barrel"})
    public int typeOfFuelIndex = 0;


    @Property(type = PropertyType.SWITCH, name = "Blue cheese omelette pickaxe switch", description = "Automatically switches to the pickaxe with blue cheese omelette when using mining speed boost", category = "Core", subcategory = "Mining")
    public boolean blueCheeseOmeletteToggle = false;


    @Property(type = PropertyType.SWITCH, name = "Debug mode", category = "Core", subcategory = "Macro", description = "Shows logs")
    public boolean debugLogMode = false;

    @Property(type = PropertyType.SWITCH, name = "Toggle mouse ungrab", description = "May not work on some computers", category = "Core", subcategory = "Macro")
    public boolean mouseUngrab = false;

    @Property(type = PropertyType.SLIDER, name = "Stuck time threshold (seconds)", description = "restarts macro when stuck time > threshold", category = "Gemstone macro", subcategory = "Miscellaneous", max = 20, min = 3)
    public int gemRestartTimeThreshold = 8;


    @Property(type = PropertyType.SLIDER, name = "Max y level", category = "Gemstone macro", subcategory = "Pathfinding", max = 256)
    public int gemMaxY = 256;

    @Property(type = PropertyType.SLIDER, name = "Min y level", category = "Gemstone macro", subcategory = "Pathfinding", max = 25)
    public int gemMinY = 0;

    @Property(type = PropertyType.SWITCH, name = "Auto open chest", category = "Gemstone macro", subcategory = "Miscellaneous")
    public boolean gemOpenChest = false;

    @Property(type = PropertyType.SLIDER, name = "Rotation time (milliseconds)", description = "Time the macro takes for each rotation", category = "Gemstone macro", subcategory = "Pathfinding", max = 500, min = 50)
    public int gemRotationTime = 300;

    @Property(type = PropertyType.SELECTOR, name = "Type of gemstone to mine", category = "Gemstone macro", subcategory = "Mining", options = {"Any", "Ruby", "Amethyst", "Jade", "Sapphire", "Amber", "Topaz"})
    public int gemGemstoneType = 0;

    @Property(type = PropertyType.SWITCH, name = "Enable Player detection failsafe", description = "Teleports you to your island if there is a player nearby", category = "Failsafes")
    public boolean playerFailsafe = true;

    @Property(type = PropertyType.SLIDER, name = "Player detection radius", description = "Warp back to island if there is player inside the given radius of player", category = "Failsafes", min = 1, max = 30)
    public int playerRad = 10;


    @Property(type = PropertyType.CHECKBOX, name = "Disable macro on world change", description = "Disables the macro when you get teleported to another world", category = "Failsafes")
    public boolean disableOnWorldChange = false;


    @Property(type = PropertyType.SWITCH, name = "RGA hardstone aura", description = "Mines hard stone around you. USE WITH center to block and optionally make gemstones full block (Core)", category = "Powder macro", subcategory = "RGA Nuker")
    public boolean powNuker = false;

    @Property(type = PropertyType.SLIDER, name = "Hardstone aura height", category = "Powder macro", subcategory = "RGA Nuker", max = 4, min = 2)
    public int powNukerHeight = 3;

    @Property(type = PropertyType.SELECTOR, name = "Hardstone aura ",
            category = "Powder macro",
            subcategory = "RGA Nuker",
            options = { "Blocks around", "Facing axis"}
    )
    public int powNukerType = 0;

    @Property(type = PropertyType.SWITCH, name = "Center to block", description = "Center to the middle of block using AOTE or AOTV when necessary. Please turn this on if you're not using nuker.", category = "Powder macro", subcategory = "Miscellaneous")
    public boolean powCenter = false;

    @Property(type = PropertyType.SWITCH, name = "Mine gemstones", description = "Make sure you have a drill that is able to mine gemstones", category = "Powder macro", subcategory = "Mining")
    public boolean powMineGemstone = true;

    @Property(type = PropertyType.SLIDER, name = "Rotation rate", description = "The higher the rotation rate, the faster you'll rotate (depends on computer)", category = "Powder macro", subcategory = "Mining", max = 15, min = 1)
    public int powRotateRate = 7;

    @Property(type = PropertyType.SLIDER, name = "Rotation radius (Multiplied by 10)", description = "The radius of the circle being dug out (e.g. 20 = 2 block radius)", category = "Powder macro", subcategory = "Mining", max = 35, min = 12)
    public int powRotateRadius = 20;

    @Property(type = PropertyType.SWITCH, name = "Use pickaxe to mine hardstone", category = "Powder macro", subcategory = "Miscellaneous")
    public boolean powPickaxeSwitch = true;

    @Property(type = PropertyType.SWITCH, name = "Switch blue cheese drill when solving chest", description = "Gives more powder, but make sure you have a blue cheese drill in your hotbar", category = "Powder macro", subcategory = "Miscellaneous")
    public boolean powBlueCheeseSwitch = true;

    @Property(type = PropertyType.SLIDER, name = "Width between each lane", category = "Powder macro", subcategory = "Mining", max = 15, min = 3)
    public int powLaneWidth = 6;

    @Property(type = PropertyType.SELECTOR, name = "Mithril macro priority 1", category = "Mithril macro", subcategory = "Mining", options = { "Clay / Gray Wool", "Prismarine", "Blue Wool"})
    public int mithPriority1 = 1;

    @Property(type = PropertyType.SELECTOR, name = "Mithril macro priority 2", category = "Mithril macro", subcategory = "Mining", options = { "Clay / Gray Wool", "Prismarine", "Blue Wool"})
    public int mithPriority2 = 2;

    @Property(type = PropertyType.SELECTOR, name = "Mithril macro priority 3", category = "Mithril macro", subcategory = "Mining", options = { "Clay / Gray Wool", "Prismarine", "Blue Wool"})
    public int mithPriority3 = 0;

    @Property(type = PropertyType.SWITCH, name = "Shift when mining", category = "Mithril macro", subcategory = "Miscellaneous")
    public boolean mithShiftWhenMine = true;

    @Property(type = PropertyType.SLIDER, name = "Rotation time (milliseconds)", description = "Time the macro takes for each rotation", category = "Mithril macro", subcategory = "Pathfinding", max = 500, min = 50)
    public int mithRotationTime = 300;

    @Property(type = PropertyType.SLIDER, name = "Stuck time threshold (seconds)", description = "restarts macro when stuck time > threshold, depends on your mining speed", category = "Mithril macro", subcategory = "Pathfinding", max = 20, min = 2)
    public int mithRestartTimeThreshold = 5;


    /*@Property( type = PropertyType.SLIDER, name = "Rotation time (milliseconds)", description = "Time the pathfinding AI takes for each rotation", category = "Commission macro", subcategory = "Miscellaneous", max = 5, min = 1)
    public int comBarRotationTime = 1;

    @Property(type = PropertyType.SLIDER, name = "Safewalk index", description = "Stops walking when there is a large rotation. TURN THIS UP IF you are using high speed", category = "Commission macro", subcategory = "Miscellaneous", max = 10)
    public int comBarSafeIndex = 5;*/

    @Property(type = PropertyType.SWITCH, name = "Use Hyperion under player", category = "Addons", subcategory = "MobKiller")
    public boolean useHyperionUnderPlayer = true;

    @Property(type = PropertyType.SLIDER, name = "MobKiller camera speed in ms", category = "Addons", subcategory = "MobKiller", max = 1000, min = 1)
    public int mobKillerCameraSpeed = 100;

    @Property(type = PropertyType.SLIDER, name = "MobKiller delay between attacks in ms", category = "Addons", subcategory = "MobKiller", max = 1000, min = 1)
    public int mobKillerAttackDelay = 100;

    @Property(type = PropertyType.TEXT, name = "Custom item to use for MobKiller", description = "Leave empty to use default weapons", category = "Addons", subcategory = "MobKiller")
    public String customItemToKill = "";

    @Property(type = PropertyType.SELECTOR, name = "Mouse button to use in MobKiller", category = "Addons", subcategory = "MobKiller", options = {"Left", "Right"})
    public int attackButton = 0;

    @Property(type = PropertyType.SLIDER, name = "MobKiller scan distance", category = "Addons", subcategory = "MobKiller", max = 30, min = 1)
    public int mobKillerScanRange = 10;

    @Property(type = PropertyType.NUMBER, name = "MobKiller info text X", category = "Addons", subcategory = "MobKiller", hidden = true)
    public int targetInfoLocationX = 0;

    @Property(type = PropertyType.NUMBER, name = "MobKiller info text Y", category = "Addons", subcategory = "MobKiller", hidden = true)
    public int targetInfoLocationY = 0;

    @Property(type = PropertyType.BUTTON, name = "Set target info location", category = "Addons", subcategory = "MobKiller")
    public void setTargetInfoLocation() {
        ChangeLocationGUI.open(MobKiller::drawInfo, this::saveTargetInfoLocation);
    }

    @Property(type = PropertyType.SWITCH, name = "Sell wishing compass to npc", description = "You need a booster cookie", category = "Addons", subcategory = "Autosell")
    public boolean sellWishingCompass = true;

    @Property(type = PropertyType.SWITCH, name = "Sell ascension rope to npc", description = "You need a booster cookie", category = "Addons", subcategory = "Autosell")
    public boolean sellAscensionRope = true;


    @Property(type = PropertyType.SLIDER, name = "Camera speed to ore in ms", category = "AOTV gemstone macro", subcategory = "Mechanics", max = 1500, min = 1)
    public int aotvCameraSpeed = 100;

    @Property(type = PropertyType.SLIDER, name = "Camera speed to waypoint in ms", category = "AOTV gemstone macro", subcategory = "Mechanics", max = 1500, min = 1)
    public int aotvCameraWaypointSpeed = 100;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "Teleport time threshold (s)",
            description = "Stops macro if it teleports between routes too fast. (If there is no veins on the spot, macro will teleport to the next and to the next etc)", category = "AOTV gemstone macro", subcategory = "Mechanics", minF = 0f, maxF = 3f, decimalPlaces = 1)
    public float aotvTeleportThreshold = 1.5f;

    @Property(type = PropertyType.SLIDER, name = "Stuck time threshold (seconds)", description = "Restarts macro when stuck time > threshold, depends on your mining speed", category = "AOTV gemstone macro", subcategory = "Mechanics", max = 20, min = 2)
    public int aotvRestartTimeThreshold = 5;


    @Property(type = PropertyType.DECIMAL_SLIDER, name = "Space from edge block to the center for accuracy checks", subcategory = "Targeting", description = "Lower value means that macro will check closes to the block's edge if the block is visible", category = "AOTV gemstone macro", minF = 0f, maxF = 0.5f, decimalPlaces = 2)
    public float aotvMiningAccuracy = 0.1f;

    @Property(type = PropertyType.SLIDER, name = "Accuracy checks per dimension", subcategory = "Targeting", description = "Higher value means that macro will check more times if the block is visible", category = "AOTV gemstone macro", min = 1, max = 16)
    public int aotvMiningAccuracyChecks = 8;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "Space from cobblestone to the center", subcategory = "Targeting", description = "Increase if macro destroys cobblestone too often", category = "AOTV gemstone macro", minF = 0f, maxF = 0.35f, decimalPlaces = 3)
    public float aotvMiningCobblestoneAccuracy = 0.15f;


    @Property(type = PropertyType.SWITCH, name = "Auto yog killer", description = "Warning: Early alpha. For more configuration options go to MobKiller", category = "AOTV gemstone macro", subcategory = "Yogs")
    public boolean aotvKillYogs = true;

    @Property(type = PropertyType.SELECTOR, name = "Type of gemstone to mine", category = "AOTV gemstone macro", subcategory = "Mining", options = {"Any", "Ruby", "Amethyst", "Jade", "Sapphire", "Amber", "Topaz", "Jasper", "Mithril"})
    public int aotvGemstoneType = 0;

    @Property(type = PropertyType.SWITCH, name = "Stop if any cobblestone on the route has been destroyed", category = "AOTV gemstone macro", subcategory = "Mining")
    public boolean stopIfCobblestoneDestroyed = true;

    @Property(type = PropertyType.SWITCH, name = "Mine gemstone panes", category = "AOTV gemstone macro", subcategory = "Mining")
    public boolean aotvMineGemstonePanes = true;

    @Property(type = PropertyType.SWITCH, name = "Show route lines", category = "AOTV gemstone macro", subcategory = "Drawings")
    public boolean aotvShowRouteLines = true;

    @Property(type = PropertyType.COLOR, name = "Color of route line", category = "AOTV gemstone macro", subcategory = "Drawings")
    public Color aotvRouteLineColor = new Color(217f / 255f, 55f / 255f, 55f / 255f, 200f / 255f);

    @Property(type = PropertyType.SWITCH, name = "Highlight route blocks", category = "AOTV gemstone macro", subcategory = "Drawings")
    public boolean aotvHighlightRouteBlocks = true;

    @Property(type = PropertyType.COLOR, name = "Color of highlighted route blocks", category = "AOTV gemstone macro", subcategory = "Drawings")
    public Color aotvRouteBlocksColor = new Color(217f / 255f, 55f / 255f, 55f / 255f, 200f / 255f);

    @Property(type = PropertyType.SWITCH, name = "Show distance to blocks", category = "AOTV gemstone macro", subcategory = "Drawings")
    public boolean aotvShowDistanceToBlocks = true;

    @Property(type = PropertyType.SWITCH, name = "Draw blocks blocking AOTV vision", category = "AOTV gemstone macro", subcategory = "Drawings")
    public boolean drawBlocksBlockingAOTV = true;

    @Property(type = PropertyType.COLOR, name = "Color of blocks blocking AOTV vision", category = "AOTV gemstone macro", subcategory = "Drawings")
    public Color aotvVisionBlocksColor = new Color(255, 0, 0, 120);


    @Property(type = PropertyType.SWITCH, name = "Rotation check", category = "Failsafes")
    public boolean stopMacrosOnRotationCheck = true;


    public Config() {
        super(new File("./config/mightyminer.toml"), "Mighty Miner", new JVMAnnotationPropertyCollector(), new ConfigSorting());
        init();
    }



    public Void saveTargetInfoLocation(int x, int y) {
        targetInfoLocationX = x;
        targetInfoLocationY = y;
        return null;
    }

    private void init() {
        this.initialize();
        this.markDirty();
        this.preload();


        this.addDependency("playerRad", "playerFailsafe");
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
    }
}
