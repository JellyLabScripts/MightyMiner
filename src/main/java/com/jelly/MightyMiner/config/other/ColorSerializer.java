package com.jelly.MightyMiner.config.other;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import java.awt.*;

public class ColorSerializer implements ObjectSerializer<Color> {
    @Override
    public boolean supports(@NonNull Class<? super Color> type) {
        return Color.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull Color object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("color", object.getRGB());
    }

    @Override
    public Color deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        return new Color(data.get("color", Integer.class));
    }
}
