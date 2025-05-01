package com.jelly.mightyminerv2.util.helper;

import java.util.ArrayList;
import java.util.List;

public enum MineableBlock {

    QUARTZ(155, 153),
    DIAMOND(57),
    EMERALD(133),
    REDSTONE( 152),
    LAPIS(22),
    GOLD(41),
    IRON(42),
    COAL(173),
    SULPHUR(19),
    HARDSTONE(1),
    TITANIUM(16385),
    GRAY_MITHRIL(28707, 37023),
    GREEN_MITHRIL(168, 4264, 8360),
    BLUE_MITHRIL(12323),
    OPAL(95, 160),
    JASPER(8287, 8352),
    TOPAZ(16544, 16479),
    AMBER(4191, 4256),
    SAPPHIRE(12383, 12448),
    JADE(20575, 20640),
    AMETHYST(41055, 41120),
    RUBY(57504, 57436),
    AQUAMARINE(45151, 45216),
    PERIDOT(53343, 53408),
    ONYX(61535, 61600),
    CITRINE(49247, 49312),
    GLACITE(174),
    UMBER(172, 32949, 49311),
    TUNGSTEN(4193, 82),;

    public final List<Integer> stateIds;

    MineableBlock(int... values) {
        stateIds = new ArrayList<>();
        for (int value : values) {
            stateIds.add(value);
        }
    }

}