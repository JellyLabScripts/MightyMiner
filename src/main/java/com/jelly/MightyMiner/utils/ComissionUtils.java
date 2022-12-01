package com.jelly.MightyMiner.utils;

import com.jelly.MightyMiner.macros.macros.CommissionMacro;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ComissionUtils {
    public static Pair<CommissionMacro.ComissionType, Boolean> determineComm() {
        CommissionMacro.ComissionType quest = null;
        boolean isDone = false;
        List<String> tablist = TablistUtils.getTabList();
        for (String s : tablist) {
            for (CommissionMacro.ComissionType value : CommissionMacro.ComissionType.values()) {
                if (s.contains(value.questName)) {
                    quest = value;
                    isDone = s.contains("DONE");
                }
            }
        }
        return Pair.of(quest, isDone);
    }
}
