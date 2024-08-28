package com.jelly.mightyminerv2.util.helper.location;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public enum SubLocation {
    //<editor-fold desc="Hub Island">
    ARCHERY_RANGE("Archery Range"),
    AUCTION_HOUSE("Auction House"),
    BANK("Bank"),
    BAZAAR_ALLEY("Bazaar Alley"),
    BLACKSMITHS_HOUSE("Blacksmith's House"),
    BUILDERS_HOUSE("Builder's House"),
    CANVAS_ROOM("Canvas Room"),
    COAL_MINE("Coal Mine"),
    COLOSSEUM_ARENA("Colosseum Arena"),
    COLOSSEUM("Colosseum"),
    COMMUNITY_CENTER("Community Center"),
    ELECTION_ROOM("Election Room"),
    FARM("Farm"),
    FARMHOUSE("Farmhouse"),
    FASHION_SHOP("Fashion Shop"),
    FISHERMANS_HUT("Fisherman's Hut"),
    FLOWER_HOUSE("Flower House"),
    FOREST("Forest"),
    GRAVEYARD("Graveyard"),
    HEXATORUM("Hexatorum"),
    LIBRARY("Library"),
    MOUNTAIN("Mountain"),
    MUSEUM("Museum"),
    REGALIA_ROOM("Regalia Room"),
    RUINS("Ruins"),
    SHENS_AUCTION("Shen's Auction"),
    TAVERN("Tavern"),
    THAUMATURGIST("Thaumaturgist"),
    UNINCORPORATED("Unincorporated"),
    VILLAGE("Village"),
    WILDERNESS("Wilderness"),
    WIZARD_TOWER("Wizard Tower"),
    //</editor-fold>

    //<editor-fold desc="The Park">
    BIRCH_PARK("Birch Park"),
    DARK_THICKET("Dark Thicket"),
    HOWLING_CAVE("Howling Cave"),
    JUNGLE_ISLAND("Jungle Island"),
    LONELY_ISLAND("Lonely Island"),
    MELODYS_PLATEAU("Melody's Plateau"),
    SAVANNA_WOODLAND("Savanna Woodland"),
    SPRUCE_WOODS("Spruce Woods"),
    VIKING_LONGHOUSE("Viking Longhouse"),
    //</editor-fold>

    //<editor-fold desc="The Farming Islands">
    THE_BARN("The Barn"),
    WINDMILL("Windmill"),
    //</editor-fold>

    //<editor-fold desc="Mushroom Desert">
    DESERT_SETTLEMENT("Desert Settlement"),
    GLOWING_MUSHROOM_CAVE("Glowing Mushroom Cave"),
    JAKES_HOUSE("Jake's House"),
    MUSHROOM_DESERT("Mushroom Desert"),
    MUSHROOM_GORGE("Mushroom Gorge"),
    OASIS("Oasis"),
    OVERGROWN_MUSHROOM_CAVE("Overgrown Mushroom Cave"),
    SHEPHERDS_KEEP("Shepherd's Keep"),
    TRAPPERS_DEN("Trapper's Den"),
    TREASURE_HUNTER_CAMP("Treasure Hunter Camp"),
    //</editor-fold>

    //<editor-fold desc="Gold Mine">
    GOLD_MINE("Gold Mine"),
    //</editor-fold>

    //<editor-fold desc="Deep Caverns">
    DIAMOND_RESERVE("Diamond Reserve"),
    GUNPOWDER_MINES("Gunpowder Mines"),
    LAPIS_QUARRY("Lapis Quarry"),
    OBSIDIAN_SANCTUARY("Obsidian Sanctuary"),
    PIGMENS_DEN("Pigmen's Den"),
    SLIMEHILL("Slimehill"),
    //</editor-fold>

    //<editor-fold desc="Dwarven Mines">
    ARISTOCRAT_PASSAGE("Aristocrat Passage"),
    BARRACKS_OF_HEROES("Barracks of Heroes"),
    C_AND_C_MINECARTS_CO("C&C Minecarts Co."),
    CLIFFSIDE_VEINS("Cliffside Veins"),
    DIVANS_GATEWAY("Divan's Gateway"),
    DWARVEN_MINES("Dwarven Mines"),
    DWARVEN_TAVERN("Dwarven Tavern"),
    DWARVEN_VILLAGE("Dwarven Village"),
    FAR_RESERVE("Far Reserve"),
    FORGE_BASIN("Forge Basin"),
    GATES_TO_THE_MINES("Gates to the Mines"),
    GOBLIN_BURROWS("Goblin Burrows"),
    GRAND_LIBRARY("Grand Library"),
    GREAT_ICE_WALL("Great Ice Wall"),
    HANGING_COURT("Hanging Court"),
    LAVA_SPRINGS("Lava Springs"),
    MINERS_GUILD("Miner's Guild"),
    PALACE_BRIDGE("Palace Bridge"),
    RAMPARTS_QUARRY("Rampart's Quarry"),
    ROYAL_MINES("Royal Mines"),
    ROYAL_PALACE("Royal Palace"),
    ROYAL_QUARTERS("Royal Quarters"),
    THE_FORGE("The Forge"),
    THE_LIFT("The Lift"),
    THE_MIST("The Mist"),
    UPPER_MINES("Upper Mines"),
    //</editor-fold>

    //<editor-fold desc="Crystal Hollows">
    CRYSTAL_HOLLOWS("Crystal Hollows"),
    CRYSTAL_NUCLEUS("Crystal Nucleus"),
    DRAGONS_LAIR("Dragon's Lair"),
    FAIRY_GROTTO("Fairy Grotto"),
    GOBLIN_HOLDOUT("Goblin Holdout"),
    GOBLIN_QUEENS_DEN("Goblin Queen's Den"),
    JUNGLE_TEMPLE("Jungle Temple"),
    JUNGLE("Jungle"),
    KHAZADDUM("Khazad-dûm"),
    LOST_PRECURSOR_CITY("Lost Precursor City"),
    MAGMA_FIELDS("Magma Fields"),
    MINES_OF_DIVAN("Mines of Divan"),
    MITHRIL_DEPOSITS("Mithril Deposits"),
    PRECURSOR_REMNANTS("Precursor Remnants"),
    //</editor-fold>

    //<editor-fold desc="Spider's Den">
    ARACHNES_BURROW("Arachne's Burrow"),
    ARACHNES_SANCTUARY("Arachne's Sanctuary"),
    ARCHAEOLOGISTS_CAMP("Archaeologist's Camp"),
    GRANDMAS_HOUSE("Grandma's House"),
    GRAVEL_MINES("Gravel Mines"),
    SPIDER_MOUND("Spider Mound"),
    //</editor-fold>

    //<editor-fold desc="The End">
    DRAGONS_NEST("Dragon's Nest"),
    THE_END("The End"),
    VOID_SEPULTURE("Void Sepulture"),
    VOID_SLATE("Void Slate"),
    ZEALOT_BRUISER_HIDEOUT("Zealot Bruiser Hideout"),
    //</editor-fold>

    //<editor-fold desc="Crimson Isle">
    AURAS_LAB("Aura's Lab"),
    BARBARIAN_OUTPOST("Barbarian Outpost"),
    BELLY_OF_THE_BEAST("Belly of the Beast"),
    BLAZING_VOLCANO("Blazing Volcano"),
    BURNING_DESERT("Burning Desert"),
    CATHEDRAL("Cathedral"),
    CHIEFS_HUT("Chief's Hut"),
    COMMUNITY_CENTER_CRIMSON("Community Center"),
    COURTYARD("Courtyard"),
    CRIMSON_FIELDS("Crimson Fields"),
    DOJO("Dojo"),
    DRAGONTAIL_AUCTION_HOUSE("Dragontail Auction House"),
    DRAGONTAIL_BANK("Dragontail Bank"),
    DRAGONTAIL_BAZAAR("Dragontail Bazaar"),
    DRAGONTAIL_BLACKSMITH("Dragontail Blacksmith"),
    DRAGONTAIL_MINION_SHOP("Dragontail Minion Shop"),
    DRAGONTAIL_TOWNSQUARE("Dragontail Townsquare"),
    DRAGONTAIL("Dragontail"),
    FORGOTTEN_SKULL("Forgotten Skull"),
    IGRUPANS_CHICKEN_COOP("Igrupan's Chicken Coop"),
    IGRUPANS_HOUSE("Igrupan's House"),
    MAGE_COUNCIL("Mage Council"),
    MAGE_OUTPOST("Mage Outpost"),
    MAGMA_CHAMBER("Magma Chamber"),
    MATRIARCHS_LAIR("Matriarch's Lair"),
    MYSTIC_MARSH("Mystic Marsh"),
    ODGERS_HUT("Odger's Hut"),
    PLHLEGBLAST_POOL("Plhlegblast Pool"),
    RUINS_OF_ASHFANG("Ruins of Ashfang"),
    SCARLETON_AUCTION_HOUSE("Scarleton Auction House"),
    SCARLETON_BANK("Scarleton Bank"),
    SCARLETON_BAZAAR("Scarleton Bazaar"),
    SCARLETON_BLACKSMITH("Scarleton Blacksmith"),
    SCARLETON_MINION_SHOP("Scarleton Minion Shop"),
    SCARLETON_PLAZA("Scarleton Plaza"),
    SCARLETON("Scarleton"),
    SMOLDERING_TOMB("Smoldering Tomb"),
    STRONGHOLD("Stronghold"),
    THE_BASTION("The Bastion"),
    THE_DUKEDOM("The Dukedom"),
    THE_WASTELAND("The Wasteland"),
    THRONE_ROOM("Throne Room"),
    //</editor-fold>

    //<editor-fold desc="Winter Island">
    EINARYS_EMPORIUM("Einary's Emporium"),
    GARYS_SHACK("Gary's Shack"),
    GLACIAL_CAVE("Glacial Cave"),
    HOT_SPRINGS("Hot Springs"),
    JERRY_POND("Jerry Pond"),
    JERRYS_WORKSHOP("Jerry's Workshop"),
    MOUNT_JERRY("Mount Jerry"),
    REFLECTIVE_POND("Reflective Pond"),
    SHERRYS_SHOWROOM("Sherry's Showroom"),
    SUNKEN_JERRY_POND("Sunken Jerry Pond"),
    TERRYS_SHACK("Terry's Shack"),
    //</editor-fold>

    //<editor-fold desc="Catacombs">
    THE_CATACOMBS_ENTRANCE("The Catacombs (Entrance)"),
    THE_CATACOMBS_F1("The Catacombs (F1)"),
    THE_CATACOMBS_F2("The Catacombs (F2)"),
    THE_CATACOMBS_F3("The Catacombs (F3)"),
    THE_CATACOMBS_F4("The Catacombs (F4)"),
    THE_CATACOMBS_F5("The Catacombs (F5)"),
    THE_CATACOMBS_F6("The Catacombs (F6)"),
    THE_CATACOMBS_F7("The Catacombs (F7)"),
    //</editor-fold>

    //<editor-fold desc="Rift Dimension">
    AROUND_COLOSSEUM("Around Colosseum"),
    BARRIER_STREET("Barrier Street"),
    BARRY_CENTER("Barry Center"),
    BARRY_HQ("Barry HQ"),
    BLACK_LAGOON("Black Lagoon"),
    BOOK_IN_A_BOOK("Book in a Book"),
    BROKEN_CAGE("Broken Cage"),
    CAKE_HOUSE("Cake House"),
    COLISSEUM("Colosseum"),
    DOLPHIN_TRAINER("Dolphin Trainer"),
    DREADFARM("Dreadfarm"),
    DEJA_VU_ALLEY("Déjà Vu Alley"),
    EMPTY_BANK("Empty Bank"),
    ENIGMAS_CRIB("Enigma's Crib"),
    FAIRYLOSOPHER_TOWER("Fairylosopher Tower"),
    GREAT_BEANSTALK("Great Beanstalk"),
    HALF_EATEN_CAVE("Half-Eaten Cave"),
    INFESTED_HOUSE("Infested House"),
    LAGOON_CAVE("Lagoon Cave"),
    LAGOON_HUT("Lagoon Hut"),
    LEECHES_LAIR("Leeches Lair"),
    LIVING_CAVE("Living Cave"),
    LIVING_STILLNESS("Living Stillness"),
    LONELY_TERRACE("Lonely Terrace"),
    MIRRORVERSE("Mirrorverse"),
    MURDER_HOUSE("Murder House"),
    OTHERSIDE("Otherside"),
    OUBLIETTE("Oubliette"),
    PHOTON_PATHWAY("Photon Pathway"),
    PUMPGROTTO("Pumpgrotto"),
    RIFT_GALLERY_ENTRANCE("Rift Gallery Entrance"),
    RIFT_GALLERY("Rift Gallery"),
    SHIFTED_TAVERN("Shifted Tavern"),
    STILLGORE_CHATEAU("Stillgore Château"),
    TAYLORS("Taylor's"),
    THE_BASTION_RIFT("The Bastion"),
    VILLAGE_PLAZA_RIFT("Village Plaza"),
    WEST_VILLAGE("West Village"),
    WIZARD_TOWER_RIFT("Wizard Tower"),
    YOUR_ISLAND("Your Island"),
    WYLD_WOODS("Wyld Woods"),
    //</editor-fold>

    KNOWHERE("Knowhere");

    private final String name;
    private static final Map<String, SubLocation> nameToLocationMap = new HashMap<>();

    static {
        for (SubLocation location : SubLocation.values()) {
            nameToLocationMap.put(location.getName(), location);
        }
    }

    SubLocation(String name) {
        this.name = name;
    }

    public static SubLocation fromName(String name) {
        final SubLocation loc = nameToLocationMap.get(name);
        if(loc == null) return SubLocation.KNOWHERE;
        return loc;
    }
}
