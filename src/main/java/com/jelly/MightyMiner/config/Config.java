package com.jelly.MightyMiner.config;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.*;

import java.io.File;

public class Config extends Vigilant {

    @Property(
            type = PropertyType.SELECTOR,
            name = "Macro", category = "Core",
            subcategory = "Macro",
            options = { "Gemstone macro", "Powder macro", "Mithril macro"}
    )
    public int macroType = 0;

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
            name = "Mine gemstones",
            category = "Powder macro",
            subcategory = "Miscellaneous"
    )
    public boolean powMineGemstone = true;

    @Property(type = PropertyType.SWITCH,
            name = "RGA hardstone aura",
            category = "Powder macro",
            subcategory = "Miscellaneous"
    )
    public boolean powStoneAura = false;

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









    public Config() {
        super(new File("./config/mightyminer.toml"), "Mighty Miner", new JVMAnnotationPropertyCollector());
        init();
    }

    private void init() {
        this.initialize();
        this.markDirty();
        this.preload();
    }



}
