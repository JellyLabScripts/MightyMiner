package com.jelly.mightyminerv2.util;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext;
import com.jelly.mightyminerv2.util.helper.heap.MinHeap;
import com.jelly.mightyminerv2.pathfinder.helper.BlockStateAccessor;
import com.jelly.mightyminerv2.pathfinder.movement.MovementHelper;
import kotlin.Pair;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.util.*;
import java.util.stream.Collectors;


public class BlockUtil {

  private static final Minecraft mc = Minecraft.getMinecraft();

  // Credit: GTC
  public static final Map<EnumFacing, float[]> BLOCK_SIDES = new HashMap<EnumFacing, float[]>() {{
    put(EnumFacing.DOWN, new float[]{0.5f, 0.01f, 0.5f});
    put(EnumFacing.UP, new float[]{0.5f, 0.99f, 0.5f});
    put(EnumFacing.WEST, new float[]{0.01f, 0.5f, 0.5f});
    put(EnumFacing.EAST, new float[]{0.99f, 0.5f, 0.5f});
    put(EnumFacing.NORTH, new float[]{0.5f, 0.5f, 0.01f});
    put(EnumFacing.SOUTH, new float[]{0.5f, 0.5f, 0.99f});
    put(null, new float[]{0.5f, 0.5f, 0.5f}); // Handles the null case
  }};

  public static BlockPos getBlockLookingAt() {
    return mc.objectMouseOver.getBlockPos();
  }

  public static List<BlockPos> getWalkableBlocksAround(BlockPos playerPos) {
    List<BlockPos> walkableBlocks = new ArrayList<>();
    BlockStateAccessor bsa = new BlockStateAccessor(mc.theWorld);
    int yOffset = MovementHelper.INSTANCE.isBottomSlab(bsa.get(playerPos.getX(), playerPos.getY(), playerPos.getZ())) ? -1 : 0;

    for (int i = -1; i <= 1; i++) {
      for (int j = yOffset; j <= 0; j++) {
        for (int k = -1; k <= 1; k++) {
          int x = playerPos.getX() + i;
          int y = playerPos.getY() + j;
          int z = playerPos.getZ() + k;

          if (MovementHelper.INSTANCE.canStandOn(bsa, x, y, z, bsa.get(x, y, z)) &&
              MovementHelper.INSTANCE.canWalkThrough(bsa, x, y + 1, z, bsa.get(x, y + 1, z)) &&
              MovementHelper.INSTANCE.canWalkThrough(bsa, x, y + 2, z, bsa.get(x, y + 2, z))) {
            walkableBlocks.add(new BlockPos(x, y, z));
          }
        }
      }
    }
    return walkableBlocks;
  }

  // unfortunately this function is extremely expensive
  public static List<BlockPos> getBestMithrilBlocksAround(final int[] priority, BlockPos blockToIgnore) {
    final MinHeap<BlockPos> blocks = new MinHeap<>(500);
    final Set<Long> visitedPositions = new HashSet<>();
    final List<BlockPos> walkableBlocks = getWalkableBlocksAround(PlayerUtil.getBlockStandingOn());

    if (blockToIgnore != null) {
      visitedPositions.add(longHash(blockToIgnore.getX(), blockToIgnore.getY(), blockToIgnore.getZ()));
    }

    for (final BlockPos blockPos : walkableBlocks) {
      final Vec3 blockCenter = new Vec3(blockPos).addVector(0.5, mc.thePlayer.eyeHeight, 0.5);
      for (int x = -3; x < 4; x++) {
        for (int z = -3; z < 4; z++) {
          for (int y = -3; y < 4; y++) {
            final BlockPos pos = blockPos.add(x, y + 2, z);
            final long hash = longHash(pos.getX(), pos.getY(), pos.getZ());
            if (visitedPositions.contains(hash)) {
              continue;
            }

            final double hardness = getBlockStrength(pos);
            if (hardness < 500 || hardness > 2000 || hardness == 600) {
              continue;
            }

            final double distance = blockCenter.distanceTo(new Vec3(pos));
            if (distance > 4) {
              continue;
            }

            if (!hasVisibleSide(blockCenter, pos)) {
              continue;
            }

            final float angleChange = AngleUtil.getNeededChange(AngleUtil.getPlayerAngle(), AngleUtil.getRotation(blockCenter, pos)).getValue();
            final int priorityValue = Math.max(1, priority[getPriorityIndex((int) hardness)]); // In case its set to zero

            visitedPositions.add(hash);
            // Todo: Cost requires more testing.
            blocks.add(pos, ((hardness / 100) * MightyMinerConfig.devMithHard + angleChange * MightyMinerConfig.devMithRot
                + distance * MightyMinerConfig.devMithDist) / priorityValue);
          }
        }
      }
    }
    return blocks.getBlocks();
  }

  // Stole from baritoe
  public static long longHash(int x, int y, int z) {
    long hash = 3241;
    hash = 3457689L * hash + x;
    hash = 8734625L * hash + y;
    hash = 2873465L * hash + z;
    return hash;
  }

  // Priority - [GrayWool, Prismarine, BlueWool, Titanium]
  // The higher the priority, the higher the chance of it getting picked
  public static List<BlockPos> getBestMithrilBlocks(int[] priority, BlockPos blockToIgnore) {
    final MinHeap<BlockPos> blocks = new MinHeap<>(500);
    final BlockPos playerHeadPos = PlayerUtil.getBlockStandingOn().add(0, 2, 0);
    final Vec3 posEyes = mc.thePlayer.getPositionEyes(1);
    for (int x = -4; x < 5; x++) {
      for (int z = -4; z < 5; z++) {
        for (int y = -3; y < 5; y++) {
          final BlockPos pos = playerHeadPos.add(x, y, z);
          if (pos.equals(blockToIgnore)) {
            continue;
          }

          final double hardness = getBlockStrength(pos);
          if (hardness < 500 || hardness > 2000 || hardness == 600) {
            continue;
          }

          final double distance = posEyes.distanceTo(new Vec3(pos));
          if (distance > 4) {
            continue;
          }

          if (!hasVisibleSide(pos)) {
            continue;
          }

          final float angleChange = AngleUtil.getNeededChange(AngleUtil.getPlayerAngle(), AngleUtil.getRotation(pos)).getValue();
          final int priorityValue = Math.max(1, priority[getPriorityIndex((int) hardness)]); // In case its set to zero
          blocks.add(pos, ((hardness / 100) * MightyMinerConfig.devMithHard + angleChange * MightyMinerConfig.devMithRot
              + distance * MightyMinerConfig.devMithDist) / priorityValue);
        }
      }
    }
    return blocks.getBlocks();
  }

  public static List<Pair<BlockPos, List<Float>>> getBestMithrilBlocksDebug(int[] priority) {
//    final MinHeap<BlockPos> blocks = new MinHeap<>(500);
    final BlockPos playerHeadPos = PlayerUtil.getBlockStandingOn().add(0, 2, 0);
    final Vec3 posEyes = mc.thePlayer.getPositionEyes(1);
    List<Pair<BlockPos, List<Float>>> debugs = new ArrayList<>();
    for (int x = -4; x < 5; x++) {
      for (int z = -4; z < 5; z++) {
        for (int y = -3; y < 5; y++) {
          final BlockPos pos = playerHeadPos.add(x, y, z);

          final double hardness = getBlockStrength(pos);
          if (hardness < 500 || hardness > 2000 || hardness == 600) {
            continue;
          }

          final double distance = posEyes.distanceTo(new Vec3(pos));
          if (distance > 4) {
            continue;
          }

          if (!hasVisibleSide(pos)) {
            continue;
          }

          final float angleChange = AngleUtil.getNeededChange(AngleUtil.getPlayerAngle(), AngleUtil.getRotation(pos)).getValue();
          final int priorityValue = Math.max(1, priority[getPriorityIndex((int) hardness)]); // In case its set to zero
//          blocks.add(pos, (hardness / 100 + angleChange * 0.15 + distance * 0.35) / priorityValue);
          debugs.add(new Pair(pos, Arrays.asList(
              (float) (hardness / (100 * MightyMinerConfig.devMithHard * priorityValue)),
              angleChange / (MightyMinerConfig.devMithRot * priorityValue),
              (float) distance / (MightyMinerConfig.devMithDist * priorityValue)))
          );
        }
      }
    }
    debugs.sort(Comparator.comparing(it -> it.getSecond().stream().reduce(0f, Float::sum)));
    return debugs;
  }

  private static int getPriorityIndex(final int hardness) {
    switch (hardness) {
      case 500:
        return 0;
      case 800:
        return 1;
      case 1500:
        return 2;
      default:
        return 3;
    }
  }

  public static int getBlockStrength(final BlockPos pos) {
    final IBlockState blockState = mc.theWorld.getBlockState(pos);

    if (blockState == null) {
      return 30;
    }

    Block block = blockState.getBlock();

    if (block == Blocks.diamond_block) {
      return 50;
    } else if (block == Blocks.gold_block) {
      return 600;
    } else if (block == Blocks.sponge) {
      return 500;
    } else if (block == Blocks.stone) {
      switch (blockState.getValue(BlockStone.VARIANT)) {
        case STONE:
          return 50;
        case DIORITE_SMOOTH:
          return 2000;
        default:
          break;
      }
    } else if (block == Blocks.wool) {
      switch (blockState.getValue(BlockColored.COLOR)) {
        case GRAY:
          return 500;
        case LIGHT_BLUE:
          return 1500;
        default:
          break;
      }
    } else if (block == Blocks.stained_hardened_clay) {
      if (blockState.getValue(BlockColored.COLOR) == EnumDyeColor.CYAN) {
        return 500;
      }
    } else if (block == Blocks.prismarine) {
      switch (blockState.getValue(BlockPrismarine.VARIANT)) {
        case ROUGH:
        case DARK:
        case BRICKS:
          return 800;
        default:
          break;
      }
    } else if (block instanceof BlockGlass) {
      switch (blockState.getValue(BlockColored.COLOR)) {
        case RED:
          return 2500;
        case PURPLE:
        case BLUE:
        case ORANGE:
        case LIME:
          return 3200;
        case WHITE:
        case YELLOW:
          return 4000;
        case MAGENTA:
          return 5000;
//                case BLUE: - Aquamarine - Do Something about this
        case BLACK:
        case BROWN:
        case GREEN:
          return 5200;
        default:
          break;
      }
    } else {
      return 30;
    }

    return 5000;
  }

  public static int getMiningTime(final BlockPos pos, final int miningSpeed) {
    return (int) Math.ceil((getBlockStrength(pos) * 30) / (float) miningSpeed) + MightyMinerConfig.mithrilMinerTickGlideOffset;
  }

  public static Vec3 getSidePos(BlockPos block, EnumFacing face) {
    final float[] offset = BLOCK_SIDES.get(face);
    return new Vec3(block.getX() + offset[0], block.getY() + offset[1], block.getZ() + offset[2]);
  }

  public static boolean canSeeSide(BlockPos block, EnumFacing side) {
    return RaytracingUtil.canSeePoint(getSidePos(block, side));
  }

  public static boolean canSeeSide(Vec3 from, BlockPos block, EnumFacing side) {
    return RaytracingUtil.canSeePoint(from, getSidePos(block, side));
  }

  public static List<EnumFacing> getAllVisibleSides(BlockPos block) {
    final List<EnumFacing> sides = new ArrayList<>();
    for (EnumFacing face : BLOCK_SIDES.keySet()) {
      if (face != null && !mc.theWorld.getBlockState(block).getBlock()
          .shouldSideBeRendered(mc.theWorld, block.offset(face), face)) {
        continue;
      }
      if (canSeeSide(block, face)) {
        sides.add(face);
      }
    }
    return sides;
  }

  public static List<EnumFacing> getAllVisibleSides(Vec3 from, BlockPos block) {
    final List<EnumFacing> sides = new ArrayList<>();
    for (EnumFacing face : BLOCK_SIDES.keySet()) {
      if (face != null && !mc.theWorld.getBlockState(block).getBlock().shouldSideBeRendered(mc.theWorld, block.offset(face), face)) {
        continue;
      }
      if (canSeeSide(from, block, face)) {
        sides.add(face);
      }
    }
    return sides;
  }

  public static Vec3 getClosestVisibleSidePos(BlockPos block) {
    if (!mc.theWorld.isBlockFullCube(block)) {
      return null;
    }
    final Vec3 eyePos = mc.thePlayer.getPositionEyes(1);
    double dist = Double.MAX_VALUE;
    EnumFacing face = null;
    for (EnumFacing side : BLOCK_SIDES.keySet()) {
      if (side != null && !mc.theWorld.getBlockState(block).getBlock()
          .shouldSideBeRendered(mc.theWorld, block.offset(side), side)) {
        continue;
      }
      final double distanceToThisSide = eyePos.distanceTo(getSidePos(block, side));
      if (canSeeSide(block, side) && distanceToThisSide < dist) {
        if (side == null && face != null) {
          continue;
        }
        dist = distanceToThisSide;
        face = side;
      }
    }
    final float[] offset = BLOCK_SIDES.get(face);
    return new Vec3(block.getX() + offset[0], block.getY() + offset[1], block.getZ() + offset[2]);
  }


  public static Vec3 getClosestVisibleSidePos(Vec3 from, BlockPos block) {
    if (!mc.theWorld.isBlockFullCube(block)) {
      return null;
    }
    double dist = Double.MAX_VALUE;
    EnumFacing face = null;
    for (EnumFacing side : BLOCK_SIDES.keySet()) {
      if (side != null && !mc.theWorld.getBlockState(block).getBlock().shouldSideBeRendered(mc.theWorld, block.offset(side), side)) {
        continue;
      }
      final double distanceToThisSide = from.distanceTo(getSidePos(block, side));
      if (canSeeSide(from, block, side) && distanceToThisSide < dist) {
        if (side == null && face != null) {
          continue;
        }
        dist = distanceToThisSide;
        face = side;
      }
    }
    final float[] offset = BLOCK_SIDES.get(face);
    return new Vec3(block.getX() + offset[0], block.getY() + offset[1], block.getZ() + offset[2]);
  }

  public static EnumFacing getClosestVisibleSide(BlockPos block) {
    if (!mc.theWorld.isBlockFullCube(block)) {
      return null;
    }
    final Vec3 eyePos = mc.thePlayer.getPositionEyes(1);
    double dist = Double.MAX_VALUE;
    EnumFacing face = null;
    for (EnumFacing side : BLOCK_SIDES.keySet()) {
      if (side != null && !mc.theWorld.getBlockState(block).getBlock()
          .shouldSideBeRendered(mc.theWorld, block.offset(side), side)) {
        continue;
      }
      final double distanceToThisSide = eyePos.distanceTo(getSidePos(block, side));
      if (canSeeSide(block, side) && distanceToThisSide < dist) {
        if (side == null && face != null) {
          continue;
        }
        dist = distanceToThisSide;
        face = side;
      }
    }
    return face;
  }

  public static EnumFacing getClosestVisibleSide(Vec3 from, BlockPos block) {
    if (!mc.theWorld.isBlockFullCube(block)) {
      return null;
    }
    double dist = Double.MAX_VALUE;
    EnumFacing face = null;
    for (EnumFacing side : BLOCK_SIDES.keySet()) {
      if (side != null && !mc.theWorld.getBlockState(block).getBlock().shouldSideBeRendered(mc.theWorld, block.offset(side), side)) {
        continue;
      }
      final double distanceToThisSide = from.distanceTo(getSidePos(block, side));
      if (canSeeSide(from, block, side) && distanceToThisSide < dist) {
        if (side == null && face != null) {
          continue;
        }
        dist = distanceToThisSide;
        face = side;
      }
    }
    return face;
  }

  public static boolean hasVisibleSide(BlockPos block) {
    if (!mc.theWorld.isBlockFullCube(block)) {
      return false;
    }
    for (EnumFacing side : EnumFacing.values()) {
      if (!mc.theWorld.getBlockState(block).getBlock().shouldSideBeRendered(mc.theWorld, block.offset(side), side)) {
        continue;
      }
      if (canSeeSide(block, side)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasVisibleSide(Vec3 from, BlockPos block) {
    if (!mc.theWorld.isBlockFullCube(block)) {
      return false;
    }
    for (EnumFacing side : EnumFacing.values()) {
      if (!mc.theWorld.getBlockState(block).getBlock().shouldSideBeRendered(mc.theWorld, block.offset(side), side)) {
        continue;
      }
      if (canSeeSide(from, block, side)) {
        return true;
      }
    }
    return false;
  }

  public static List<Vec3> bestPointsOnBestSide(final BlockPos block) {
    return pointsOnBlockSide(block, getClosestVisibleSide(block)).stream()
        .filter(RaytracingUtil::canSeePoint)
        .sorted(Comparator.comparingDouble(i -> AngleUtil.getNeededChange(AngleUtil.getPlayerAngle(), AngleUtil.getRotation(i)).getValue()))
        .collect(Collectors.toList());
  }

  public static List<Vec3> bestPointsOnBestSide(Vec3 from, final BlockPos block) {
    return pointsOnBlockSide(block, getClosestVisibleSide(from, block)).stream()
        .filter(it -> RaytracingUtil.canSeePoint(from, it))
        .sorted(Comparator.comparingDouble(i -> AngleUtil.getNeededChange(AngleUtil.getPlayerAngle(), AngleUtil.getRotation(from, i)).getValue()))
        .collect(Collectors.toList());
  }

  public static List<Vec3> bestPointsOnVisibleSides(final BlockPos block) {
    return pointsOnVisibleSides(block).stream()
        .filter(RaytracingUtil::canSeePoint)
        .sorted(Comparator.comparingDouble(mc.thePlayer.getPositionEyes(1)::distanceTo))
        .collect(Collectors.toList());
  }

  public static List<Vec3> bestPointsOnVisibleSides(Vec3 from, final BlockPos block) {
    return pointsOnVisibleSides(block).stream()
        .filter(it -> RaytracingUtil.canSeePoint(from, it))
        .sorted(Comparator.comparingDouble(from::distanceTo))
        .collect(Collectors.toList());
  }

  // Should not use this because it wont ensure the points can be looked at
  private static List<Vec3> pointsOnVisibleSides(final BlockPos block) {
    final List<Vec3> points = new ArrayList<>();
    for (EnumFacing side : getAllVisibleSides(block)) {
      points.addAll(pointsOnBlockSide(block, side));
    }
    return points;
  }

  private static List<Vec3> pointsOnVisibleSides(Vec3 from, final BlockPos block) {
    final List<Vec3> points = new ArrayList<>();
    for (EnumFacing side : getAllVisibleSides(from, block)) {
      points.addAll(pointsOnBlockSide(block, side));
    }
    return points;
  }

  // Credits to GTC <3
  private static List<Vec3> pointsOnBlockSide(final BlockPos block, final EnumFacing side) {
    final Set<Vec3> points = new HashSet<>();

    if (side != null) {
      float[] it = BLOCK_SIDES.get(side);
      for (int i = 0; i < 20; i++) {
        float x = it[0];
        float y = it[1];
        float z = it[2];
        if (x == 0.5f) {
          x = randomVal();
        }
        if (y == 0.5f) {
          y = randomVal();
        }
        if (z == 0.5f) {
          z = randomVal();
        }
        Vec3 point = new Vec3(block).addVector(x, y, z);
        if (!points.contains(point)) {
          points.add(point);
        }
      }
    } else {
      for (float[] bside : BLOCK_SIDES.values()) {
        for (int i = 0; i < 20; i++) {
          float x = bside[0];
          float y = bside[1];
          float z = bside[2];
          if (x == 0.5f) {
            x = randomVal();
          }
          if (y == 0.5f) {
            y = randomVal();
          }
          if (z == 0.5f) {
            z = randomVal();
          }
          Vec3 point = new Vec3(block).addVector(x, y, z);
          if (!points.contains(point)) {
            points.add(point);
          }
        }
      }
    }
    return new ArrayList<>(points);
  }

  private static float randomVal() {
    return (new Random().nextInt(6) + 2) / 10.0f;
  }

  public static boolean canWalkBetween(CalculationContext ctx, BlockPos start, BlockPos end) {
    int ey = end.getY();
    int ex = end.getX();
    int ez = end.getZ();
    IBlockState endState = ctx.get(ex, ey, ez);
    if (!MovementHelper.INSTANCE.canStandOn(ctx.getBsa(), ex, ey, ez, endState)) {
//      Logger.sendLog("Cannot stand on x: " + ex + ", y: " + ey + ", z: " + ez);
      return false;
    }
    if (!MovementHelper.INSTANCE.canWalkThrough(ctx.getBsa(), ex, ey + 1, ez, ctx.get(ex, ey + 1, ez))) {
//      Logger.sendLog("Cannot walk throug x: " + ex + ", y: " + (ey + 1) + ", z: " + ez);
      return false;
    }
    if (!MovementHelper.INSTANCE.canWalkThrough(ctx.getBsa(), ex, ey + 2, ez, ctx.get(ex, ey + 2, ez))) {
//      Logger.sendLog("Cannot walk throug x: " + ex + ", y: " + (ey + 2) + ", z: " + ez);
      return false;
    }
    int sy = start.getY();
    if(ey - sy > 1){
//      Logger.sendLog("ey - sy > 1");
      return true;
    }

    int sx = start.getX();
    int sz = start.getZ();
    IBlockState fromState = ctx.get(sx, sy, sz);

    boolean srcSmall = MovementHelper.INSTANCE.isBottomSlab(fromState);
    boolean destSmall = MovementHelper.INSTANCE.isBottomSlab(endState);
    boolean destSmallStair = MovementHelper.INSTANCE.isValidStair(endState, ex - sx, ez - sz);
//    Logger.sendLog("SrcSmall: " + srcSmall + ", DestSmall: " + destSmall + ", DestSmallStair: " + destSmallStair);
    if (!srcSmall == !(destSmall || destSmallStair) && !srcSmall && !destSmallStair) {
      return true;
    }
    return false;
  }
}
