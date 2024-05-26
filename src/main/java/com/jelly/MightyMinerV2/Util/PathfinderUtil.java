package com.jelly.MightyMinerV2.Util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathfinderUtil {
    public static List<BlockPos> findPath(World world, BlockPos start, BlockPos end) {
        List<BlockPos> openList = new ArrayList<>();
        List<BlockPos> closedList = new ArrayList<>();

        Map<BlockPos, Integer> gScoreMap = new HashMap<>();
        Map<BlockPos, Integer> fScoreMap = new HashMap<>();
        Map<BlockPos, BlockPos> parentMap = new HashMap<>();

        BlockPos current = start;
        int gScore = 0;
        int hScore = (int) MathHelper.abs(end.getX() - start.getX()) + (int) MathHelper.abs(end.getY() - start.getY()) + (int) MathHelper.abs(end.getZ() - start.getZ());
        int fScore = gScore + hScore;

        gScoreMap.put(current, gScore);
        fScoreMap.put(current, fScore);
        openList.add(current);

        while (!openList.isEmpty()) {
            Collections.sort(openList, new Comparator<BlockPos>() {
                @Override
                public int compare(BlockPos o1, BlockPos o2) {
                    return Integer.compare(fScoreMap.get(o2), fScoreMap.get(o1));
                }
            });

            current = openList.remove(0);
            closedList.add(current);

            if (current.getX() == end.getX() && current.getY() == end.getY() && current.getZ() == end.getZ()) {
                List<BlockPos> path = new ArrayList<>();
                while (current != start) {
                    path.add(current);
                    current = parentMap.get(current);
                }
                Collections.reverse(path);
                LogUtil.send("Found path: " + path, LogUtil.ELogType.SUCCESS);
                return path;
            }

            for (EnumFacing direction : EnumFacing.values()) {
                BlockPos neighbor = current.offset(direction);
                if (closedList.contains(neighbor)) continue;

                Block block = world.getBlockState(neighbor).getBlock();
                if (block instanceof BlockAir) continue;

                int tentativeGScore = gScoreMap.get(current) + 1;
                if (openList.contains(neighbor)) {
                    int currentGScore = gScoreMap.get(neighbor);
                    if (tentativeGScore >= currentGScore) continue;
                }

                gScoreMap.put(neighbor, tentativeGScore);
                fScoreMap.put(neighbor, tentativeGScore + hScore);
                parentMap.put(neighbor, current);

                if (!openList.contains(neighbor)) {
                    openList.add(neighbor);
                }
            }
        }

        LogUtil.send("Could not find path", LogUtil.ELogType.ERROR);
        return Collections.emptyList();
    }
}