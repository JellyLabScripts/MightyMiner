package com.jelly.MightyMiner.config.serdes;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import net.minecraft.util.BlockPos;

public class CoordsSerializer implements ObjectSerializer<BlockPos> {
    @Override
    public boolean supports(@NonNull Class<? super BlockPos> type) {
        return BlockPos.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull BlockPos object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("x", object.getX());
        data.add("y", object.getY());
        data.add("z", object.getZ());
    }

    @Override
    public BlockPos deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        return new BlockPos(
                data.get("x", Integer.class),
                data.get("y", Integer.class),
                data.get("z", Integer.class));
    }
}
