package com.jelly.mightyminerv2.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.macro.impl.helper.Commission;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;

import java.util.*;

public class CommissionUtil {

    public static final List<Pair<String, Vec3>> emissaries = Arrays.asList(
            new Pair<>("Ceanna", new Vec3(42.50, 134.50, 22.50)),
            new Pair<>("Carlton", new Vec3(-72.50, 153.00, -10.50)),
            new Pair<>("Wilson", new Vec3(171.50, 150.00, 31.50)),
            new Pair<>("Lilith", new Vec3(58.50, 198.00, -8.50)),
            new Pair<>("Fraiser", new Vec3(-132.50, 174.00, -50.50))
//      new Pair<>("Eliza", new Vec3(-37.50, 200.00, -131.50)) // she's at dwarven village, dont need her
    );
    private static final Minecraft mc = Minecraft.getMinecraft();
//      Commission.TREASURE_HOARDER_SLAYER, ImmutableSet.of("Treasure Hoarder"));
    private static final Map<Commission, Set<String>> slayerMob = ImmutableMap.of(
            Commission.GOBLIN_SLAYER, ImmutableSet.of("Goblin", "Knifethrower", "Fireslinger"),
            Commission.MINES_SLAYER, ImmutableSet.of("Goblin", "Knifethrower", "Fireslinger", "Glacite Walker"),
            Commission.GLACITE_WALKER_SLAYER, ImmutableSet.of("Glacite Walker"));

    public static Set<String> getMobForCommission(Commission commission) {
        return slayerMob.get(commission);
    }

    public static Optional<EntityPlayer> getEmissary(Vec3 pos) {
        return mc.theWorld.playerEntities.stream()
                .filter(entity -> entity.posX == pos.xCoord && entity.posY == pos.yCoord && entity.posZ == pos.zCoord
                        && !entity.getName().contains("Sentry") // Just Because; It should never happen
                        && EntityUtil.isNpc(entity))
                .findFirst();
    }

    public static Optional<EntityPlayer> getClosestEmissary() {
        Vec3 pos = emissaries
                .stream()
                .min(Comparator.comparing(it -> mc.thePlayer.getPositionVector().squareDistanceTo(it.getSecond())))
                .map(Pair::getSecond)
                .orElse(null);
        if (pos == null) {
            return Optional.empty();
        }

        return mc.theWorld.playerEntities.stream()
                .filter(entity -> entity.posX == pos.xCoord && entity.posY == pos.yCoord && entity.posZ == pos.zCoord
                        && !entity.getName().contains("Sentry") // Just Because; It should never happen
                        && EntityUtil.isNpc(entity))
                .findFirst();
    }

    public static Vec3 getClosestEmissaryPosition() {
        return emissaries
                .stream()
                .min(Comparator.comparing(it -> mc.thePlayer.getPositionVector().squareDistanceTo(it.getSecond())))
                .get().getSecond();
    }

    public static List<Commission> getCurrentCommissionsFromTablist() {
        List<Commission> comms = new ArrayList<>();
        boolean foundCommission = false;
        for (final String text : TablistUtil.getCachedTablist()) {
            if (!foundCommission) {
                if (text.equalsIgnoreCase("Commissions:")) {
                    foundCommission = true;
                }
                continue;
            }

            if (text.contains("DONE")) {
                return Collections.singletonList(Commission.COMMISSION_CLAIM);
            }

            Commission comm = Commission.getCommission(text.split(": ")[0].trim());
            if (comm != null) {
                comms.add(comm);
            }

            if (text.isEmpty()) {
                break;
            }
        }

        return Commission.getBestCommissionFrom(comms);
    }

    public static int getClaimableCommissionSlot() {
        if (!(mc.thePlayer.openContainer instanceof ContainerChest)) {
            return -1;
        }
        final ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
        for (int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); i++) {
            final ItemStack stack = chest.getLowerChestInventory().getStackInSlot(i);
            if (stack == null || stack.getItem() == null) {
                continue;
            }
            for (final String lore : InventoryUtil.getItemLore(stack)) {
                if (lore.equalsIgnoreCase("completed")) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static List<Commission> getCommissionFromContainer(ContainerChest container) {
        IInventory lowerChest = container.getLowerChestInventory();
        List<Commission> comms = new ArrayList<>();
        for (int i = 0; i < lowerChest.getSizeInventory(); i++) {
            final ItemStack stack = lowerChest.getStackInSlot(i);
            if (stack == null || stack.getItem() == null || !stack.hasDisplayName() || !StringUtils.stripControlCodes(stack.getDisplayName())
                    .startsWith("Commission")) {
                continue;
            }
            List<String> loreList = InventoryUtil.getItemLore(stack);
            for (int j = 0; j < loreList.size(); j++) {
                if (loreList.get(j).isEmpty()) {
                    Commission comm = Commission.getCommission(loreList.get(++j));
                    if (comm != null) {
                        if (comm == Commission.COMMISSION_CLAIM) {
                            return Collections.singletonList(comm);
                        }
                        comms.add(comm);
                    }
                    break;
                }
            }
        }
        return Commission.getBestCommissionFrom(comms);
    }

//  public static List<EntityPlayer> getCommissionMobs(Commission commission) {
//    String mobName;
//    if ((mobName = slayerMob.get(commission)) == null) {
//      return new ArrayList<>();
//    }
//    List<EntityPlayer> mobs = new ArrayList<>();
//    for (EntityPlayer mob : mc.theWorld.playerEntities) {
//      if (mob.getName().equals(mobName)) {
//        mobs.add(mob);
//      }
//    }
//
//    Angle playerAngle = new Angle(AngleUtil.get360RotationYaw(), 0);
//    Vec3 playerPos = mc.thePlayer.getPositionVector();
//    mobs.sort(Comparator.comparingDouble(
//        mob -> mob.getPositionVector().distanceTo(playerPos) + AngleUtil.getNeededChange(playerAngle, AngleUtil.getRotation(mob)).yaw));
//    return mobs;
//  }

    public static List<EntityLiving> getMobList(String mobName, Set<EntityLiving> mobsToIgnore) {
        List<EntityLiving> mobs = new ArrayList<>();
        for (Entity mob : mc.theWorld.loadedEntityList) {
            if (mob instanceof EntityLiving && mob.getName().trim().equals(mobName) && mob.isEntityAlive() && !mobsToIgnore.contains(mob)) {
                mobs.add((EntityLiving) mob);
            }
        }

        Vec3 playerPos = mc.thePlayer.getPositionVector();
        float normalizedYaw = AngleUtil.normalizeAngle(mc.thePlayer.rotationYaw);
        mobs.sort(Comparator.comparingDouble(mob -> {
                    Vec3 mobPos = mob.getPositionVector();
                    double distanceCost =
                            Math.hypot(playerPos.xCoord - mobPos.xCoord, playerPos.zCoord - mobPos.zCoord) + Math.abs(mobPos.yCoord - playerPos.yCoord) * 2;
                    double angleCost = Math.abs(AngleUtil.normalizeAngle((normalizedYaw - AngleUtil.getRotation(mob).yaw)));
                    return distanceCost * MightyMinerConfig.devMKillDist + angleCost * MightyMinerConfig.devMKillRot;
                }
        ));
        return mobs;
    }

    // right now i have no use for a list of mobs so this should do it
    public static List<Pair<EntityPlayer, Pair<Double, Double>>> getMobListDebug(String mobName, Set<EntityPlayer> mobsToIgnore) {
        List<Pair<EntityPlayer, Pair<Double, Double>>> mobs = new ArrayList<>();
        Vec3 playerPos = mc.thePlayer.getPositionVector();
        float normalizedYaw = AngleUtil.normalizeAngle(mc.thePlayer.rotationYaw);
        for (EntityPlayer mob : mc.theWorld.playerEntities) {
            if (mob.getName().trim().equals(mobName) && mob.isEntityAlive() && !mobsToIgnore.contains(mob)) {
                Vec3 mobPos = mob.getPositionVector();
                double distanceCost =
                        Math.hypot(playerPos.xCoord - mobPos.xCoord, playerPos.zCoord - mobPos.zCoord) + Math.abs(mobPos.yCoord - playerPos.yCoord) * 2;
                double angleCost = Math.abs(AngleUtil.normalizeAngle((normalizedYaw - AngleUtil.getRotation(mob).yaw)));
                mobs.add(new Pair(mob, new Pair(distanceCost * 0.6, angleCost * 0.1)));
            }
        }

        mobs.sort(Comparator.comparing(a -> {
            Pair<Double, Double> b = a.getSecond();
            return b.getFirst() + b.getSecond();
        }));
        return mobs;
    }
}
