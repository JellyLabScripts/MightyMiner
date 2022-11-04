package com.jelly.MightyMiner.baritone.automine.config;

import com.jelly.MightyMiner.baritone.automine.calculations.config.PathMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PathSetting{
    @Getter
    boolean mineWithPreference;

    @Getter
    PathMode pathMode;




}
