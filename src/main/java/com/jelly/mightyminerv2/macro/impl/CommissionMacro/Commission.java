package com.jelly.mightyminerv2.macro.impl.CommissionMacro;

import com.jelly.mightyminerv2.util.CommissionUtil;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.location.SubLocation;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.util.helper.route.WaypointType;
import lombok.Getter;
import net.minecraft.util.Vec3;

import java.util.*;

public enum Commission {
    // maybe set it to null and choose a random one? - DO NOT REARRANGE THIS
    MITHRIL_MINER("Mithril Miner", SubLocation.RAMPARTS_QUARRY),
    TITANIUM_MINER("Titanium Miner", SubLocation.RAMPARTS_QUARRY),
    UPPER_MITHRIL("Upper Mines Mithril", SubLocation.UPPER_MINES),
    UPPER_TITANIUM("Upper Mines Titanium", SubLocation.UPPER_MINES),
    ROYAL_MITHRIL("Royal Mines Mithril", SubLocation.ROYAL_MINES),
    ROYAL_TITANIUM("Royal Mines Titanium", SubLocation.ROYAL_MINES),
    LAVA_MITHRIL("Lava Springs Mithril", SubLocation.LAVA_SPRINGS),
    LAVA_TITANIUM("Lava Springs Titanium", SubLocation.LAVA_SPRINGS),
    CLIFFSIDE_MITHRIL("Cliffside Veins Mithril", SubLocation.CLIFFSIDE_VEINS),
    CLIFFSIDE_TITANIUM("Cliffside Veins Titanium", SubLocation.CLIFFSIDE_VEINS),
    RAMPARTS_MITHRIL("Rampart's Quarry Mithril", SubLocation.RAMPARTS_QUARRY),
    RAMPARTS_TITANIUM("Rampart's Quarry Titanium", SubLocation.RAMPARTS_QUARRY),
    GOBLIN_SLAYER("Goblin Slayer", SubLocation.GOBLIN_BURROWS),
    GLACITE_WALKER_SLAYER("Glacite Walker Slayer", SubLocation.GREAT_ICE_WALL),
    MINES_SLAYER("Mines Slayer", SubLocation.GOBLIN_BURROWS),
    COMMISSION_CLAIM("Claim Commission", SubLocation.THE_FORGE), // theres no set location for this so yea
    REFUEL("Refuel Drill", SubLocation.FORGE_BASIN); // theres no set location for this so yea
    // Do not want this
//  TREASURE_HOARDER_SLAYER("Treasure Hoarder Puncher", SubLocation.TREASURE_HUNTER_CAMP),
//  STAR_CENTRY_SLAYER("Star Sentry Puncher	"),

    private static final Map<String, Commission> COMMISSIONS;
    private static final Map<SubLocation, RouteWaypoint[]> VEINS;

    static {
        Map<String, Commission> commissionsMap = new HashMap<>();
        for (Commission comm : Commission.values()) {
            commissionsMap.put(comm.name, comm);
        }
        COMMISSIONS = Collections.unmodifiableMap(commissionsMap);

        Map<SubLocation, RouteWaypoint[]> veinsMap = new EnumMap<SubLocation, RouteWaypoint[]>(SubLocation.class) {{
            put(SubLocation.FORGE_BASIN, new RouteWaypoint[]{
                    new RouteWaypoint(-9, 144, -20, WaypointType.WALK)
            });
            put(SubLocation.THE_FORGE, new RouteWaypoint[]{
                    new RouteWaypoint(44, 134, 21, WaypointType.WALK),
                    new RouteWaypoint(58, 197, -11, WaypointType.WALK),
                    new RouteWaypoint(171, 149, 33, WaypointType.WALK),
                    new RouteWaypoint(-75, 152, -11, WaypointType.WALK),
                    new RouteWaypoint(-131, 173, -52, WaypointType.WALK)
            });
            put(SubLocation.CLIFFSIDE_VEINS, new RouteWaypoint[]{new RouteWaypoint(93, 144, 51, WaypointType.WALK)});
            put(SubLocation.ROYAL_MINES, new RouteWaypoint[]{new RouteWaypoint(115, 153, 83, WaypointType.WALK)});
            put(SubLocation.GREAT_ICE_WALL, new RouteWaypoint[]{new RouteWaypoint(0, 127, 143, WaypointType.WALK)});
            put(SubLocation.GOBLIN_BURROWS, new RouteWaypoint[]{new RouteWaypoint(-56, 134, 153, WaypointType.WALK)});
            put(SubLocation.RAMPARTS_QUARRY, new RouteWaypoint[]{
                    new RouteWaypoint(-41, 138, -13, WaypointType.WALK),
                    new RouteWaypoint(-58, 146, -18, WaypointType.WALK)
            });
            put(SubLocation.UPPER_MINES, new RouteWaypoint[]{
                    new RouteWaypoint(-111, 166, -74, WaypointType.WALK),
                    new RouteWaypoint(-145, 206, -30, WaypointType.WALK)
            });
            put(SubLocation.TREASURE_HUNTER_CAMP, new RouteWaypoint[]{new RouteWaypoint(-115, 204, -53, WaypointType.WALK)});
            put(SubLocation.LAVA_SPRINGS, new RouteWaypoint[]{new RouteWaypoint(53, 197, -24, WaypointType.WALK)});
        }};
        VEINS = Collections.unmodifiableMap(veinsMap);
    }

    @Getter
    private final String name;
    private final SubLocation location;
    private final int priority;

    Commission(String name, SubLocation location) {
        this.name = name;
        this.location = location;
        if (name.endsWith("Miner")) {
            this.priority = 1;
        } else {
            this.priority = 0;
        }
    }

    public static Commission getCommission(final String name) {
        return COMMISSIONS.get(name);
    }

    // this is incredibly bad
    public static List<Commission> getBestCommissionFrom(List<Commission> commissions) {
        // max size of list is <=2 at any given time
        int size = commissions.size();
        if (size == 0) {
            return Collections.emptyList();
        }
        if (size == 1) {
            return commissions;
        }

        commissions.sort(Comparator.comparing(it -> it.priority));
        SubLocation lastLoc = commissions.get(0).location;
        boolean hasMiner = false;
        boolean sameLocation = true;
        for (Commission comm : commissions) {
            if (comm.getName().contains("Slayer")) {
                // how else am i supposed to create a mutable list of size 1
                return new ArrayList<>(Collections.singletonList(commissions.get(0)));
            }
            if (comm.getName().contains("Miner")) {
                hasMiner = true;
            }
            if (!lastLoc.equals(comm.location)) {
                sameLocation = false;
            }
        }

        if (!(sameLocation || hasMiner)) {
            return new ArrayList<>(Collections.singletonList(commissions.get(0)));
        }

        Logger.sendNote("overlap: " + commissions);
        return commissions;
    }

    public RouteWaypoint getWaypoint() {
        if (this.name.equals("Claim Commission")) {
            return closestWaypointTo(CommissionUtil.getClosestEmissaryPosition());
        }
        RouteWaypoint[] locs = VEINS.get(this.location);
        if (locs != null && locs.length > 0) {
            return locs[new Random().nextInt(locs.length)];
        }
        throw new IllegalStateException("No route waypoints available for location: " + this.location);
    }

    public RouteWaypoint closestWaypointTo(Vec3 pos) {
        RouteWaypoint[] locs = VEINS.get(this.location);
        if (locs != null && locs.length > 0) {
            return Arrays.stream(locs).min(Comparator.comparing(it -> it.toVec3().distanceTo(pos))).get();
        }
        throw new IllegalStateException("No route waypoints available for location: " + this.location);
    }
}