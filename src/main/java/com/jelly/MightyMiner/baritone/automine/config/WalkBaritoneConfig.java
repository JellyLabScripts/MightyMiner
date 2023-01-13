package com.jelly.MightyMiner.baritone.automine.config;

import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;

public class WalkBaritoneConfig extends BaritoneConfig{

    public WalkBaritoneConfig(int minY, int maxY, int restartTimeThreshold){
        super(MiningType.NONE,
                false,
                false,
                false,
                200,
                restartTimeThreshold,
                null,
                BlockUtils.walkables,
                maxY,
                minY);
    }
}
