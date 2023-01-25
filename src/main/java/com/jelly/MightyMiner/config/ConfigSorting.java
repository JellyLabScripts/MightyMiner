package com.jelly.MightyMiner.config;

import gg.essential.vigilance.data.Category;
import gg.essential.vigilance.data.SortingBehavior;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ConfigSorting extends SortingBehavior {

    @Override
    public Comparator<Category> getCategoryComparator() {
        return (b1, b2) -> {
            if(b1.getName().equals("Addons")){
                return 1;
            } else if(b2.getName().equals("Addons")){
                return -1;
            } else {
                if (b1.getName().equals("Core")) {
                    return -1;
                } else if (b2.getName().equals("Core")) {
                    return 1;
                } else {
                    return b1.getName().compareTo(b2.getName());
                }
            }
        };
    }
}
