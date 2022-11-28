package com.jelly.MightyMiner.config.locations;

import eu.okaeri.configs.OkaeriConfig;
import net.minecraft.util.BlockPos;
import org.apache.commons.collections4.map.ListOrderedMap;

public class AOTVConfig extends OkaeriConfig {

    private ListOrderedMap<String, ListOrderedMap<Integer, BlockPos>> locations = new ListOrderedMap() {{
        ListOrderedMap<Integer, BlockPos> locations = new ListOrderedMap<>();
        put("locationNumeroUno", locations);
    }};
}
