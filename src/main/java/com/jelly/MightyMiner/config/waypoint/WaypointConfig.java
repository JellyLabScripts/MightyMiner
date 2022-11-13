package com.jelly.MightyMiner.config.waypoint;

import com.jelly.MightyMiner.waypoints.Waypoint;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class WaypointConfig extends OkaeriConfig {
    @Comment("   PrivateIsland(\"dynamic\"),\n" +
            "        SpiderDen(\"combat_1\"),\n" +
            "        CrimsonIsle(\"crimson_isle\"),\n" +
            "        TheEnd(\"combat_3\"),\n" +
            "        GoldMine(\"mining_1\"),\n" +
            "        DeepCaverns(\"mining_2\"),\n" +
            "        DwarvenMines(\"mining_3\"),\n" +
            "        CrystalHollows(\"crystal_hollows\"),\n" +
            "        FarmingIsland(\"farming_1\"),\n" +
            "        ThePark(\"foraging_1\"),\n" +
            "        Dungeon(\"dungeon\"),\n" +
            "        DungeonHub(\"dungeon_hub\"),\n" +
            "        Hub(\"hub\"),\n" +
            "        DarkAuction(\"dark_auction\"),\n" +
            "        JerryWorkshop(\"winter\");")
    public final List<Waypoint> waypoints = Collections.singletonList(new Waypoint(
            Color.BLUE,
            "Shit happens ;)",
            0,
            100,
            0,
            "hub"
            )
    );
}
