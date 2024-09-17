package com.jelly.mightyminerv2.macro.commissionmacro.helper;

import com.jelly.mightyminerv2.util.helper.location.SubLocation;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.util.helper.route.TransportMethod;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.util.Vec3;

public enum Commission {
  // maybe set it to null and choose a random one?
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
  TREASURE_HOARDER_SLAYER("Treasure Hoarder Puncher", SubLocation.TREASURE_HUNTER_CAMP),
  COMMISSION_CLAIM("Claim Commission", SubLocation.FORGE_BASIN); // theres no set location for this so yea
  // Do not want this
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
          new RouteWaypoint(44, 134, 21, TransportMethod.WALK),
          new RouteWaypoint(58, 197, -11, TransportMethod.WALK),
          new RouteWaypoint(-170, 149, 32, TransportMethod.WALK),
          new RouteWaypoint(-75, 152, -11, TransportMethod.WALK),
          new RouteWaypoint(-131, 173, -52, TransportMethod.WALK)
      });
      put(SubLocation.CLIFFSIDE_VEINS, new RouteWaypoint[]{new RouteWaypoint(93, 144, 51, TransportMethod.WALK)});
      put(SubLocation.ROYAL_MINES, new RouteWaypoint[]{new RouteWaypoint(115, 153, 83, TransportMethod.WALK)});
      put(SubLocation.GREAT_ICE_WALL, new RouteWaypoint[]{new RouteWaypoint(0, 127, 143, TransportMethod.WALK)});
      put(SubLocation.GOBLIN_BURROWS, new RouteWaypoint[]{new RouteWaypoint(-56, 134, 153, TransportMethod.WALK)});
      put(SubLocation.RAMPARTS_QUARRY, new RouteWaypoint[]{
          new RouteWaypoint(-41, 138, -13, TransportMethod.WALK),
          new RouteWaypoint(-58, 146, -18, TransportMethod.WALK)
      });
      put(SubLocation.UPPER_MINES, new RouteWaypoint[]{
          new RouteWaypoint(-111, 166, -74, TransportMethod.WALK),
          new RouteWaypoint(-145, 206, -30, TransportMethod.WALK)
      });
      put(SubLocation.TREASURE_HUNTER_CAMP, new RouteWaypoint[]{new RouteWaypoint(-115, 204, -53, TransportMethod.WALK)});
      put(SubLocation.LAVA_SPRINGS, new RouteWaypoint[]{new RouteWaypoint(53, 197, -24, TransportMethod.WALK)});
    }};
    VEINS = Collections.unmodifiableMap(veinsMap);
  }

  private final String name;
  private final SubLocation location;

  Commission(String name, SubLocation location) {
    this.name = name;
    this.location = location;
  }

  public static Commission getCommission(final String name) {
    return COMMISSIONS.get(name);
  }

  public String getName() {
    return name;
  }

  public RouteWaypoint getWaypoint() {
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