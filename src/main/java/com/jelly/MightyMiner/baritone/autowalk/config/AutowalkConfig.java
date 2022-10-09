package com.jelly.MightyMiner.baritone.autowalk.config;

import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class AutowalkConfig {
    @Getter
    int safeIndex;
    @Getter
    int rotationTime;
}
