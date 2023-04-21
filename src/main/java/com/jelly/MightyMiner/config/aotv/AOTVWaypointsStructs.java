package com.jelly.MightyMiner.config.aotv;

import com.google.gson.annotations.Expose;
import com.jelly.MightyMiner.MightyMiner;
import net.minecraft.util.BlockPos;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class AOTVWaypointsStructs {

    public static class Waypoint {
        @Expose
        public String name;
        @Expose
        public int x;
        @Expose
        public int y;
        @Expose
        public int z;

        public Waypoint(String name, int x, int y, int z) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Waypoint(String name, BlockPos pos) {
            this.name = name;
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
        }
    }

    public static class WaypointList {
        @Expose
        public boolean enabled;
        @Expose
        public String name;
        @Expose
        public boolean showCoords;
        @Expose
        public ArrayList<Waypoint> waypoints;


        public WaypointList(String name) {
            this.enabled = false;
            this.showCoords = false;
            this.waypoints = new ArrayList<>();
            this.name = name;
        }

        public WaypointList(String name, boolean enabled, boolean showCoords, ArrayList<Waypoint> waypoints) {
            this.enabled = enabled;
            this.showCoords = showCoords;
            this.waypoints = waypoints;
            this.name = name;
        }
    }

    public AOTVWaypointsStructs() {
        LoadWaypoints();
    }

    public static void SaveWaypoints() {
        // Save waypoints to config file
        String json = MightyMiner.gson.toJson(MightyMiner.aotvWaypoints);
        try {
            Files.write(Paths.get("./config/aotv_coords_mm.json"), json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void LoadWaypoints() {
        // Load waypoints from config file
        try {
            String json = new String(Files.readAllBytes(Paths.get("./config/aotv_coords_mm.json")), StandardCharsets.UTF_8);
            MightyMiner.aotvWaypoints = MightyMiner.gson.fromJson(json, AOTVWaypoints.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


