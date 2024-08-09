package com.jelly.mightyminerv2.Macro.commissionmacro.helper;

import java.util.HashMap;

public enum Commission {
  MITHRIL_MINER("Mithril Miner"),
  TITANIUM_MINER("Titanium Miner"),
  UPPER_MITHRIL("Upper Mines Mithril"),
  ROYAL_MITHRIL("Royal Mines Mithril"),
  LAVA_MITHRIL("Lava Springs Mithril"),
  CLIFFSIDE_MITHRIL("Cliffside Veins Mithril"),
  RAMPARTS_MITHRIL("Rampart's Quarry Mithril"),
  UPPER_TITANIUM("Upper Mines Titanium"),
  ROYAL_TITANIUM("Royal Mines Titanium"),
  LAVA_TITANIUM("Lava Springs Titanium"),
  CLIFFSIDE_TITANIUM("Cliffside Veins Titanium"),
  RAMPARTS_TITANIUM("Rampart's Quarry Titanium"),
  GOBLIN_SLAYER("Goblin Slayer"),
  GLACITE_WALKER_SLAYER("Glacite Walker Slayer"),
  TREASURE_HOARDER_SLAYER("Treasure Hoarder Puncher	"),
  // Do not want this
//  STAR_CENTRY_SLAYER("Star Sentry Puncher	"),
  COMMISSION_CLAIM("Claim Commission");

  private static final HashMap<String, Commission> commissions = new HashMap<>();

  static {
    for (final Commission comm : Commission.values()) {
      commissions.put(comm.name, comm);
    }
  }

  public static Commission getCommission(final String name) {
    return commissions.get(name);
  }

  public String name;

  Commission(final String name) {
    this.name = name;
  }
}
