package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.feature.AbstractFeature;
import lombok.Getter;

public class AutoSell extends AbstractFeature {

    @Getter
    public static AutoSell instance = new AutoSell();

    @Override
    public String getName() {
        return "AutoSell";
    }

}
