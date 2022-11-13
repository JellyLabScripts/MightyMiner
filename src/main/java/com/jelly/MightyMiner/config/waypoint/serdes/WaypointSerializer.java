package com.jelly.MightyMiner.config.waypoint.serdes;

import com.jelly.MightyMiner.waypoints.Waypoint;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;

import java.awt.*;
import java.nio.file.Watchable;
import java.util.Locale;

public class WaypointSerializer implements ObjectSerializer<Waypoint> {
    @Override
    public boolean supports(@NonNull Class<? super Waypoint> type) {
        return Waypoint.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull Waypoint object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("color", object.getWaypointColor());
        data.add("name", object.getName());
        data.add("x", object.getX());
        data.add("y", object.getY());
        data.add("z", object.getZ());
        data.add("dimension", object.getDimension());
    }

    @Override
    public Waypoint deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        return new Waypoint(
                data.get("color", Color.class),
                data.get("name", String.class),
                data.get("x", Integer.class),
                data.get("y", Integer.class),
                data.get("z", Integer.class),
                data.get("dimension", String.class)
        );
    }
}
