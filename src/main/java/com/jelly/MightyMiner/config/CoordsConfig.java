package com.jelly.MightyMiner.config;

import eu.okaeri.configs.OkaeriConfig;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.util.BlockPos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CoordsConfig extends OkaeriConfig {

    public List<BlockPos> blockPos = Collections.singletonList(new BlockPos(0, 0, 0));

}
