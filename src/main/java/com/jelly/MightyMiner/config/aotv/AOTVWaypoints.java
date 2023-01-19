package com.jelly.MightyMiner.config.aotv;

import com.google.gson.annotations.Expose;
import com.jelly.MightyMiner.gui.AOTVWaypointsGUI;
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
    private final ArrayList<AOTVWaypointsGUI.WaypointList> WaypointLists = new ArrayList<AOTVWaypointsGUI.WaypointList>() {{
        add(new AOTVWaypointsGUI.WaypointList("Default", true, true, new ArrayList<>()));
    }};

    public AOTVWaypointsGUI.WaypointList getSelectedRoute() {
        return WaypointLists.stream().filter(wl -> wl.enabled).findFirst().orElse(null);
    }

    public ArrayList<AOTVWaypointsGUI.WaypointList> getRoutes() {
        return WaypointLists;
    }

    public AOTVWaypointsGUI.WaypointList addRoute(String name) {
        AOTVWaypointsGUI.WaypointList route = new AOTVWaypointsGUI.WaypointList(name);
        WaypointLists.add(route);
        return route;
    }

    public boolean addCoord(AOTVWaypointsGUI.WaypointList wp, AOTVWaypointsGUI.Waypoint pos) {
        int index = WaypointLists.indexOf(wp);
        AOTVWaypointsGUI.WaypointList inner = WaypointLists.get(index);
        if (wp.waypoints.stream().anyMatch(p -> p.x == pos.x && p.y == pos.y && p.z == pos.z)) {
            LogUtils.addMessage("AOTV Waypoints - This waypoint already exists!");
            return false;
        }
        inner.waypoints.add(pos);
        WaypointLists.set(index, inner);
        return true;
    }

    public void removeCoord(AOTVWaypointsGUI.WaypointList wp, AOTVWaypointsGUI.Waypoint waypoint) {
        int index = WaypointLists.indexOf(wp);
        AOTVWaypointsGUI.WaypointList inner = WaypointLists.get(index);
        inner.waypoints.remove(waypoint);
        WaypointLists.set(index, inner);
    }

    public void removeRoute(AOTVWaypointsGUI.WaypointList index) {
        WaypointLists.remove(index);
    }

    public void changeVisibility(AOTVWaypointsGUI.WaypointList index, boolean visible) {
        int i = WaypointLists.indexOf(index);
        AOTVWaypointsGUI.WaypointList inner = WaypointLists.get(i);
        inner.showCoords = visible;
        WaypointLists.set(i, inner);
    }

    public void enableRoute(AOTVWaypointsGUI.WaypointList wp) {
        for (AOTVWaypointsGUI.WaypointList waypointList : WaypointLists) {
            waypointList.enabled = false;
        }
        int index = WaypointLists.indexOf(wp);
        AOTVWaypointsGUI.WaypointList inner = WaypointLists.get(index);
        inner.enabled = true;
        WaypointLists.set(index, inner);
    }
}
