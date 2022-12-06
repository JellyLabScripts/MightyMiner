package com.jelly.MightyMiner.path;

import net.minecraft.util.BlockPos;
import org.apache.commons.collections4.iterators.ArrayListIterator;

import java.util.ArrayList;
import java.util.List;


public enum AOTVPaths {
    DIVAN_GATEWAY_1(
            List.of(new BlockPos(0.5, 166.5, -10.5),
                    new BlockPos(0.5, 170.5, -3.5),
                    new BlockPos(4.5, 182.7, 21.5),
                    new BlockPos(27.5, 179.5, 67.5),
                    new BlockPos(-7.5, 161.5, 106.5),
                    new BlockPos(0.5, 205.0, 137.5),
                    new BlockPos(0.5, 232.5, 89.5),
                    new BlockPos(10.5, 225.5, 130.5)));


    private final List<BlockPos> blocks;

    AOTVPaths(List<BlockPos> blocks) {
        this.blocks = blocks;
    }
}
