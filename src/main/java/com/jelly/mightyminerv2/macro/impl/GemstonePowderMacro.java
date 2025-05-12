package com.jelly.mightyminerv2.macro.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class GemstonePowderMacro extends AbstractMacro {

    @Getter
    public static RouteMinerMacro instance = new RouteMinerMacro();

    @Override
    public String getName() {
        return "Gemstone Powder Macro";
    }

    @Override
    public List<String> getNecessaryItems() {
        List<String> items = new ArrayList<>();

        if (MightyMinerConfig.drillRefuel) {
            items.add("Abiphone");
        }

        return items;
    }

}
