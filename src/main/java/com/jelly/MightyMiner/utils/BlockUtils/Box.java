package com.jelly.MightyMiner.utils.BlockUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Box {
    // (0, 0, 0) = player coords
    @Getter
    int dx_bound1;
    @Getter
    int dx_bound2;
    @Getter
    int dy_bound1;
    @Getter
    int dy_bound2;
    @Getter
    int dz_bound1;
    @Getter
    int dz_bound2;

}
