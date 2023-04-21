package com.jelly.MightyMiner.baritone.automine.config;

import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PathFindSetting {
    @Getter
    boolean mineWithPreference;

    @Getter
    PathMode pathMode;

    @Getter
    boolean findWithBlockPos;


}
