package com.jelly.MightyMiner.config;

import eu.okaeri.configs.OkaeriConfig;
import net.minecraft.util.BlockPos;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.util.*;

public class CoordsConfig extends OkaeriConfig {

    private ListOrderedMap<String, ListOrderedMap<Integer, BlockPos>> routes = new ListOrderedMap() {{
        ListOrderedMap<Integer, BlockPos> def = new ListOrderedMap<>();
        put("default", def);
    }};

    private String selectedRoute = "default";
    public void setSelectedRoute(String selectedRoute) {
        this.selectedRoute = selectedRoute;
    }

    public ListOrderedMap<Integer, BlockPos> getSelectedRoute() {
        return routes.get(selectedRoute);
    }

    public String getSelectedRouteName() {
        return selectedRoute;
    }

    public ListOrderedMap<String, ListOrderedMap<Integer, BlockPos>> getRoutes() {
        return routes;
    }

    public ListOrderedMap<Integer, BlockPos> getRoute(String name) {
        return routes.get(name);
    }

    public void addRoute(String name) {
        routes.put(name, new ListOrderedMap<>());
    }

    public void addCoord(Integer index, BlockPos pos) {
        ListOrderedMap<Integer, BlockPos> inner = routes.get(selectedRoute);
        inner.put(index, pos);
    }


    public void removeCoord(int index) {
        ListOrderedMap<Integer, BlockPos> inner = routes.get(selectedRoute);
        inner.remove(inner.keyList().indexOf(index));
    }

    public void removeRoute(String name) {
        routes.remove(name);
    }
}
