package com.jelly.MightyMiner.utils.HypixelUtils;

import com.jelly.MightyMiner.macros.macros.CommissionMacro;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.Utils.MathUtils;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;

public class WarpCoordinateUtils {
    public static ArrayList<BlockPos> getRandomEmissaryWarpCoordinates() {
        int routeNumber = MathUtils.randomNum(0, 5);
        LogUtils.debugLog("Route number: " + routeNumber);
        ArrayList<BlockPos> route = new ArrayList<>();
        if (routeNumber == 0) {
            route.add(new BlockPos(3, 165, -12));
            route.add(new BlockPos(35, 145, 1));
            route.add(new BlockPos(39, 134, 22));
        } else if (routeNumber == 1) {
            route.add(new BlockPos(6, 155, -10));
            route.add(new BlockPos(38, 142, 6));
            route.add(new BlockPos(39, 134, 22));
        } else if (routeNumber == 2) {
            route.add(new BlockPos(4, 155, -12));
            route.add(new BlockPos(27, 145, -6));
            route.add(new BlockPos(39, 134, 22));
        } else if (routeNumber == 3) {
            route.add(new BlockPos(6, 155, -10));
            route.add(new BlockPos(27, 145, -6));
            route.add(new BlockPos(39, 134, 22));
        } else if (routeNumber == 4) {
            route.add(new BlockPos(6, 155, -10));
            route.add(new BlockPos(35, 145, 1));
            route.add(new BlockPos(39, 134, 22));
        } else if (routeNumber == 5) {
            route.add(new BlockPos(4, 155, -12));
            route.add(new BlockPos(35, 145, 1));
            route.add(new BlockPos(39, 134, 22));
        }
        return route;
    }

    public static ArrayList<BlockPos> getRandomCommissionWarpCoordinates(String questName) {
        int routeNumber = MathUtils.randomNum(0, 2);
        LogUtils.debugLog("Route number: " + routeNumber);
        ArrayList<BlockPos> route = new ArrayList<>();
        if (questName.equals(CommissionMacro.ComissionType.CLIFFSIDE_VEINS_MITHRIL.questName) || questName.equals(CommissionMacro.ComissionType.CLIFFSIDE_VEINS_TITANIUM.questName) || questName.equals(CommissionMacro.ComissionType.MITHRIL_MINER.questName) || questName.equals(CommissionMacro.ComissionType.TITANIUM_MINER.questName)) {
            if (routeNumber == 0) {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(25, 176, 7));
                route.add(new BlockPos(27, 175, 22));
                route.add(new BlockPos(51, 161, 59));
                route.add(new BlockPos(10, 169, 36));
            } else if (routeNumber == 1) {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(25, 176, 7));
                route.add(new BlockPos(27, 175, 22));
                route.add(new BlockPos(51, 161, 59));
                route.add(new BlockPos(50, 187, 19));
            } else {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(25, 176, 7));
                route.add(new BlockPos(27, 175, 22));
                route.add(new BlockPos(51, 161, 59));
                route.add(new BlockPos(13, 161, 59));
                route.add(new BlockPos(-8, 161, 59));
                route.add(new BlockPos(-23, 219, 50));
            }
        } else if (questName.equals(CommissionMacro.ComissionType.RAMPARTS_QUARRY_MITHRIL.questName) || questName.equals(CommissionMacro.ComissionType.RAMPARTS_QUARRY_TITANIUM.questName)) {
            if (routeNumber == 0) {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(-28, 186, 14));
                route.add(new BlockPos(-64, 210, -14));
                route.add(new BlockPos(-98, 232, -16));
            } else if (routeNumber == 1) {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(-28, 186, 14));
                route.add(new BlockPos(-64, 210, -14));
                route.add(new BlockPos(-42, 210,23));
            } else {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(-28, 186, 14));
                route.add(new BlockPos(-46, 176, -35));
                route.add(new BlockPos(-56, 181,-61));
            }
        } else if (questName.equals(CommissionMacro.ComissionType.LAVA_SPRINGS_MITHRIL.questName) || questName.equals(CommissionMacro.ComissionType.LAVA_SPRINGS_TITANIUM.questName)) {
            if (routeNumber == 0) {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(30, 206, -6));
                route.add(new BlockPos(67, 227, 0));
            } else if (routeNumber == 1) {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(30, 206, -6));
                route.add(new BlockPos(62, 212, 3));
                route.add(new BlockPos(47, 228, -27));
            } else {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(30, 206, -6));
                route.add(new BlockPos(62, 212, 3));
                route.add(new BlockPos(54, 214, -30));
            }
        } else if (questName.equals(CommissionMacro.ComissionType.ROYAL_MINES_MITHRIL.questName) || questName.equals(CommissionMacro.ComissionType.ROYAL_MINES_TITANIUM.questName)) {
            if (routeNumber == 0) {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(25, 176, 7));
                route.add(new BlockPos(27, 175, 22));
                route.add(new BlockPos(51, 161, 59));
                route.add(new BlockPos(106, 156, 37));
                route.add(new BlockPos(131, 157, 37));
                route.add(new BlockPos(172, 162, 22));
                route.add(new BlockPos(160, 162, 21));
            } else if (routeNumber == 1) {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(25, 176, 7));
                route.add(new BlockPos(27, 175, 22));
                route.add(new BlockPos(51, 161, 59));
                route.add(new BlockPos(106, 156, 37));
                route.add(new BlockPos(131, 157, 37));
                route.add(new BlockPos(172, 162, 22));
                route.add(new BlockPos(170, 153, 61));
            } else {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(25, 176, 7));
                route.add(new BlockPos(27, 175, 22));
                route.add(new BlockPos(51, 161, 59));
                route.add(new BlockPos(106, 156, 37));
                route.add(new BlockPos(131, 157, 37));
                route.add(new BlockPos(172, 162, 22));
                route.add(new BlockPos(164, 161, 18));
            }
        } else if (questName.equals(CommissionMacro.ComissionType.UPPER_MINES_MITHRIL.questName) || questName.equals(CommissionMacro.ComissionType.UPPER_MINES_TITANIUM.questName)) {
            if (routeNumber == 0) {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(-28, 186, 14));
                route.add(new BlockPos(-64, 210, 4));
                route.add(new BlockPos(-101, 180, -11));
                route.add(new BlockPos(-140, 176, -41));
            } else if (routeNumber == 1) {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(-28, 186, 14));
                route.add(new BlockPos(-64, 210, 4));
                route.add(new BlockPos(-101, 180, -11));
                route.add(new BlockPos(-140, 176, -41));
                route.add(new BlockPos(-128, 172, -30));
            } else {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(-28, 186, 14));
                route.add(new BlockPos(-64, 210, 4));
                route.add(new BlockPos(-101, 180, -11));
                route.add(new BlockPos(-140, 176, -41));
                route.add(new BlockPos(-129, 169, -17   ));
            }
        } else if (questName.equals(CommissionMacro.ComissionType.ICE_WALKER_SLAYER.questName)) {
            if (routeNumber == 0) {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(25, 176, 7));
                route.add(new BlockPos(27, 179, 67));
                route.add(new BlockPos(-8, 161, 106));
                route.add(new BlockPos(9, 130, 129));
                route.add(new BlockPos(-22, 137, 171));
                route.add(new BlockPos(22, 137, 171));
            } else if (routeNumber == 1) {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(25, 176, 7));
                route.add(new BlockPos(27, 179, 67));
                route.add(new BlockPos(-8, 161, 106));
                route.add(new BlockPos(9, 130, 129));
                route.add(new BlockPos(-22, 137, 171));
            } else {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(25, 176, 7));
                route.add(new BlockPos(27, 179, 67));
                route.add(new BlockPos(-8, 161, 106));
                route.add(new BlockPos(9, 130, 129));
                route.add(new BlockPos(-22, 137, 171));
                route.add(new BlockPos(-5, 134, 177));
            }
        } else if (questName.equals(CommissionMacro.ComissionType.GOBLIN_SLAYER.questName)) {
            if (routeNumber == 0) {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(25, 176, 7));
                route.add(new BlockPos(27, 179, 67));
                route.add(new BlockPos(-8, 161, 106));
                route.add(new BlockPos(9, 130, 129));
                route.add(new BlockPos(-22, 137, 171));
                route.add(new BlockPos(-44, 149, 145));
                route.add(new BlockPos(-76, 144, 141));
            } else if (routeNumber == 1) {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(25, 176, 7));
                route.add(new BlockPos(27, 179, 67));
                route.add(new BlockPos(-8, 161, 106));
                route.add(new BlockPos(9, 130, 129));
                route.add(new BlockPos(-22, 137, 171));
                route.add(new BlockPos(-44, 149, 145));
                route.add(new BlockPos(-76, 144, 141));
            } else {
                route.add(new BlockPos(0, 165, -12));
                route.add(new BlockPos(25, 176, 7));
                route.add(new BlockPos(27, 179, 67));
                route.add(new BlockPos(-8, 161, 106));
                route.add(new BlockPos(9, 130, 129));
                route.add(new BlockPos(-22, 137, 171));
                route.add(new BlockPos(-44, 149, 145));
                route.add(new BlockPos(-76, 144, 141));
            }
        }
        return route;
    }
}
