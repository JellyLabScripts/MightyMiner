package com.jelly.MightyMiner.config;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.*;

import java.awt.*;
import java.io.File;
import java.util.Comparator;

public class Config extends Vigilant {

    @Property(
            type = PropertyType.SELECTOR,
            name = "Macro", category = "Core",
            subcategory = "Macro",
            options = { "Gemstone macro", "Powder macro", "Mithril macro", "AOTV Gemstone macro"}
    )
    public int macroType = 0;

    @Property(
                type = PropertyType.SWITCH,
                name = "Use mining speed boost", category = "Core",
                subcategory = "Mining"
    )
    public boolean useMiningSpeedBoost = true;


    @Property(
            type = PropertyType.SWITCH,
            name = "Make glass panes as a full blocks",
            category = "Core",
            subcategory = "Mining"
    )
    public boolean glassPanesFullBlock = false;


    @Property(
            type = PropertyType.NUMBER,
            name = "Blue cheese omelette pickaxe slot",
            description = "Note that the first slot is 0",
            category = "Core",
            subcategory = "Mining",
            max = 7
    )
    public int blueCheeseOmeletteSlot = 7;

    @Property(
            type = PropertyType.SWITCH,
            name = "Blue cheese omelette pickaxe switch",
            description = "automatically switches to the pickaxe with blue cheese omelette when using mining speed boost",
            category = "Core",
            subcategory = "Mining"
    )
    public boolean blueCheeseOmeletteToggle = false;


    @Property(
            type = PropertyType.SWITCH,
            name = "Debug mode",
            category = "Core",
            subcategory = "Macro",
            description = "Shows logs"
    )
    public boolean debugLogMode = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Toggle mouse ungrab",
            description = "May not work on some computers",
            category = "Core",
            subcategory = "Macro"
    )
    public boolean mouseUngrab = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Stuck time threshold (seconds)",
            description = "restarts macro when stuck time > threshold",
            category = "Gemstone macro",
            subcategory = "Miscellaneous",
            max = 20,
            min = 3
    )
    public int gemRestartTimeThreshold = 8;


    @Property(type = PropertyType.SLIDER,
            name = "Max y level",
            category = "Gemstone macro",
            subcategory = "Miscellaneous",
            max = 256
    )
    public int gemMaxY = 256;

    @Property(type = PropertyType.SLIDER,
            name = "Min y level",
            category = "Gemstone macro",
            subcategory = "Miscellaneous",
            max = 256
    )
    public int gemMinY = 0;

    @Property(type = PropertyType.SWITCH,
            name = "Auto open chest",
            category = "Gemstone macro",
            subcategory = "Miscellaneous"
    )
    public boolean gemOpenChest = false;

    @Property(type = PropertyType.SLIDER,
            name = "Rotation time (milliseconds)",
            description = "Time the macro takes for each rotation",
            category = "Gemstone macro",
            subcategory = "Miscellaneous",
            max = 500,
            min = 50
    )
    public int gemRotationTime = 300;



    @Property(type = PropertyType.SWITCH,
            name = "Enable Player detection failsafe",
            description = "Teleports you to your island if there is a player nearby",
            category = "Gemstone macro",
            subcategory = "Failsafes"
    )
    public boolean gemPlayerFailsafe = true;

    @Property(type = PropertyType.SLIDER,
            name = "Player detection radius",
            description = "Warp back to island if there is player inside the given radius of player",
            category = "Gemstone macro",
            subcategory = "Failsafes",
            min = 1,
            max = 30
    )
    public int gemPlayerRad = 10;




    @Property(type = PropertyType.SWITCH,
            name = "RGA hardstone aura",
            description = "Mines hard stone around you. USE WITH Center to block",
            category = "Powder macro",
            subcategory = "RGA Nuker"
    )
    public boolean powStoneAura = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Hardstone aura height",
            category = "Powder macro",
            subcategory = "RGA Nuker",
            max = 4,
            min = 2
    )
    public int powAuraHeight = 3;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Hardstone aura ",
            category = "Powder macro",
            subcategory = "RGA Nuker",
            options = { "Blocks around", "Facing axis"}
    )
    public int powAuraType = 1;

    @Property(type = PropertyType.SWITCH,
            name = "Center to block",
            description = "Center to the middle of block using AOTE or AOTV when necessary",
            category = "Powder macro",
            subcategory = "Miscellaneous"
    )
    public boolean powCenter = false;

    @Property(type = PropertyType.SWITCH,
            name = "Enable Player detection failsafe",
            description = "Teleports you to your island if there is a player nearby",
            category = "Powder macro",
            subcategory = "Failsafes"
    )
    public boolean powPlayerFailsafe = true;


    @Property(type = PropertyType.SWITCH,
            name = "Mine gemstones",
            category = "Powder macro",
            subcategory = "Miscellaneous"
    )
    public boolean powMineGemstone = true;

    @Property(type = PropertyType.SLIDER,
            name = "Player detection radius",
            description = "Warp back to island if there is player inside the given radius of player",
            category = "Powder macro",
            subcategory = "Failsafes",
            min = 1,
            max = 30
    )
    public int powPlayerRad = 10;

    @Property(type = PropertyType.SLIDER,
            name = "Width between each lane",
            category = "Powder macro",
            subcategory = "Miscellaneous",
            max = 15,
            min = 3
    )
    public int powLaneWidth = 6;



    @Property(
            type = PropertyType.SELECTOR,
            name = "Mithril macro priority 1",
            category = "Mithril macro",
            subcategory = "Miscellaneous",
            options = { "Clay", "Prismarine", "Wool"}
    )
    public int mithPriority1 = 1;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Mithril macro priority 2",
            category = "Mithril macro",
            subcategory = "Miscellaneous",
            options = { "Clay", "Prismarine", "Wool"}
    )
    public int mithPriority2 = 2;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Mithril macro priority 3",
            category = "Mithril macro",
            subcategory = "Miscellaneous",
            options = { "Clay", "Prismarine", "Wool"}
    )
    public int mithPriority3 = 0;

    @Property(type = PropertyType.SWITCH,
            name = "Shift when mining",
            category = "Mithril macro",
            subcategory = "Miscellaneous"
    )
    public boolean mithShiftWhenMine = true;

    @Property(type = PropertyType.SLIDER,
            name = "Rotation time (milliseconds)",
            description = "Time the macro takes for each rotation",
            category = "Mithril macro",
            subcategory = "Miscellaneous",
            max = 500,
            min = 50
    )
    public int mithRotationTime = 300;

    @Property(
            type = PropertyType.SLIDER,
            name = "Stuck time threshold (seconds)",
            description = "restarts macro when stuck time > threshold, depends on your mining speed",
            category = "Mithril macro",
            subcategory = "Miscellaneous",
            max = 20,
            min = 2
    )
    public int mithRestartTimeThreshold = 5;

    @Property(type = PropertyType.SWITCH,
            name = "Enable Player detection failsafe",
            description = "Teleports you to your island if there is a player nearby",
            category = "Mithril macro",
            subcategory = "Failsafes"
    )
    public boolean mithPlayerFailsafe = true;

    @Property(type = PropertyType.SLIDER,
            name = "Player detection radius",
            description = "Warp back to island if there is player inside the given radius of player",
            category = "Mithril macro",
            subcategory = "Failsafes",
            min = 1,
            max = 30
    )
    public int mithPlayerRad = 10;


   /* @Property( type = PropertyType.SLIDER,
            name = "Rotation time (milliseconds)",
            description = "Time the pathfinding AI takes for each rotation",
            category = "Commission macro",
            subcategory = "Miscellaneous",
            max = 5,
            min = 1
    )
    public int comBarRotationTime = 1;

    @Property(
            type = PropertyType.SLIDER,
            name = "Safewalk index",
            description = "Stops walking when there is a large rotation. TURN THIS UP IF you are using high speed",
            category = "Commission macro",
            subcategory = "Miscellaneous",
            max = 10
    )
    public int comBarSafeIndex = 5;*/

    @Property(type = PropertyType.SLIDER,
            name = "Rotation time (milliseconds)",
            description = "Time the macro takes for each rotation",
            category = "AOTV gemstone macro",
            subcategory = "Miscellaneous",
            max = 500,
            min = 20
    )
    public int aotvRotationTime = 100;

    @Property(
            type = PropertyType.SLIDER,
            name = "Stuck time threshold (seconds)",
            description = "restarts macro when stuck time > threshold, depends on your mining speed",
            category = "AOTV gemstone macro",
            subcategory = "Miscellaneous",
            max = 20,
            min = 2
    )
    public int aotvRestartTimeThreshold = 5;


    @Property(
            type = PropertyType.SWITCH,
            name = "Auto yog killer",
            category = "AOTV gemstone macro",
            subcategory = "Yogs"
    )
    public boolean killYogs = true;

    @Property(
            type = PropertyType.SLIDER,
            name = "Radius",
            category = "AOTV gemstone macro",
            subcategory = "Yogs",
            max = 50,
            min = 1
    )
    public int yogsRadius = 15;

    @Property(
            type = PropertyType.SWITCH,
            name = "Mine gemstone panes",
            category = "AOTV gemstone macro",
            subcategory = "Miscellaneous"
    )
    public boolean aotvMineGemstonePanes = true;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show route lines",
            category = "AOTV gemstone macro",
            subcategory = "Drawings"
    )
    public boolean showRouteLines = true;

    @Property(
            type = PropertyType.SWITCH,
            name = "Highlight route blocks",
            category = "AOTV gemstone macro",
            subcategory = "Drawings"
    )
    public boolean highlightRouteBlocks = true;


    @Property(
            type = PropertyType.COLOR,
            name = "Color of route line",
            category = "AOTV gemstone macro",
            subcategory = "Drawings"
    )
    public Color routeLineColor = new Color(217f / 255f, 55f / 255f, 55f / 255f, 200f / 255f);

    @Property(
            type = PropertyType.COLOR,
            name = "Color of highlighted route blocks",
            category = "AOTV gemstone macro",
            subcategory = "Drawings"
    )
    public Color routeBlocksColor = new Color(217f / 255f, 55f / 255f, 55f / 255f, 200f / 255f);


    @Property(
            type = PropertyType.SWITCH,
            name = "Refuel with abiphone",
            category = "Core",
            subcategory = "Additions"
    )
    public boolean refuelWithAbiphone = false;

    @Property(
            type = PropertyType.NUMBER,
            name = "Refuel if less than",
            category = "Core",
            subcategory = "Additions",
            min = 0,
            max = 3000,
            increment = 50
    )
    public int refuelThreshold = 200;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Type of fuel to use",
            category = "Core",
            subcategory = "Additions",
            options = {"Goblin Egg", "Biofuel", "Volta", "Oil Barrel"}
    )
    public int typeOfFuelIndex = 0;

   /* @Property(
            type = PropertyType.SWITCH,
            name = "Always show drawings",
            category = "Core",
            subcategory = "Drawings"
    )
    public boolean showAlwaysDrawings = true;*/

    public Config() {
        super(new File("./config/mightyminer.toml"), "Mighty Miner", new JVMAnnotationPropertyCollector(), new ConfigSorting());
        init();
    }




    private void init() {
        this.initialize();
        this.markDirty();
        this.preload();


        this.addDependency("gemPlayerRad", "gemPlayerFailsafe");
        this.addDependency("powPlayerRad", "powPlayerFailsafe");
        this.addDependency("mithPlayerRad", "mithPlayerFailsafe");
        this.addDependency("powAuraHeight", "powStoneAura");
        this.addDependency("powAuraType", "powStoneAura");
        this.addDependency("routeLineColor", "showRouteLines");
        this.addDependency("routeBlocksColor", "highlightRouteBlocks");
        this.addDependency("refuelThreshold", "refuelWithAbiphone");
        this.addDependency("typeOfFuelIndex", "refuelWithAbiphone");
    }
}
