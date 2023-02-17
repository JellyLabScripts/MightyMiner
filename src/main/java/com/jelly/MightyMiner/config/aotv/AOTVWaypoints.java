package com.jelly.MightyMiner.config.aotv;

import com.google.gson.annotations.Expose;
import com.jelly.MightyMiner.utils.LogUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
public class AOTVWaypoints {

    @Expose
    private final ArrayList<AOTVWaypointsStructs.WaypointList> WaypointLists = new ArrayList<AOTVWaypointsStructs.WaypointList>() {{
        add(new AOTVWaypointsStructs.WaypointList("Default", true, true, new ArrayList<>()));
    }};

    public AOTVWaypointsStructs.WaypointList getSelectedRoute() {
        return WaypointLists.stream().filter(wl -> wl.enabled).findFirst().orElse(null);
    }

    public ArrayList<AOTVWaypointsStructs.WaypointList> getRoutes() {
        return WaypointLists;
    }

    public AOTVWaypointsStructs.WaypointList addRoute(String name) {
        AOTVWaypointsStructs.WaypointList route = new AOTVWaypointsStructs.WaypointList(name);
        WaypointLists.add(route);
        return route;
    }

    public boolean addCoord(AOTVWaypointsStructs.WaypointList wp, AOTVWaypointsStructs.Waypoint pos) {
        int index = WaypointLists.indexOf(wp);
        AOTVWaypointsStructs.WaypointList inner = WaypointLists.get(index);
        if (wp.waypoints.stream().anyMatch(p -> p.x == pos.x && p.y == pos.y && p.z == pos.z)) {
            return false;
        }
        inner.waypoints.add(pos);
        WaypointLists.set(index, inner);
        return true;
    }

    public void removeCoord(AOTVWaypointsStructs.WaypointList wp, AOTVWaypointsStructs.Waypoint waypoint) {
        int index = WaypointLists.indexOf(wp);
        AOTVWaypointsStructs.WaypointList inner = WaypointLists.get(index);
        inner.waypoints.remove(waypoint);
        WaypointLists.set(index, inner);
    }

    public void removeRoute(AOTVWaypointsStructs.WaypointList index) {
        WaypointLists.remove(index);
    }

    public void changeVisibility(AOTVWaypointsStructs.WaypointList index, boolean visible) {
        int i = WaypointLists.indexOf(index);
        AOTVWaypointsStructs.WaypointList inner = WaypointLists.get(i);
        inner.showCoords = visible;
        WaypointLists.set(i, inner);
    }

    public void enableRoute(AOTVWaypointsStructs.WaypointList wp) {
        for (AOTVWaypointsStructs.WaypointList waypointList : WaypointLists) {
            waypointList.enabled = false;
        }
        int index = WaypointLists.indexOf(wp);
        AOTVWaypointsStructs.WaypointList inner = WaypointLists.get(index);
        inner.enabled = true;
        WaypointLists.set(index, inner);
    }
}
