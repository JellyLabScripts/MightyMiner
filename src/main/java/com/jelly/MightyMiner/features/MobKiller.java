package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.*;
import com.jelly.MightyMiner.utils.HypixelUtils.NpcUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.jelly.MightyMiner.handlers.KeybindHandler.leftClick;
import static com.jelly.MightyMiner.handlers.KeybindHandler.rightClick;


public class MobKiller {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static Target target;
    public static int scanRange = 20;

    private static boolean caseSensitive = false;
    private static String[] mobsNames = null;

    private final Timer attackDelay = new Timer();
    private final Timer blockedVisionDelay = new Timer();
    private final Timer afterKillDelay = new Timer();
    public static States currentState = States.SEARCHING;

    public static boolean isToggled = false;

    private final CopyOnWriteArrayList<Target> potentialTargets = new CopyOnWriteArrayList<>();

    private final Rotation rotation = new Rotation();

    private static class Target {
        public EntityLivingBase entity;
        public EntityArmorStand stand;
        public boolean worm;
        public double distance() {
            if (entity != null)
                return Minecraft.getMinecraft().thePlayer.getDistanceToEntity(entity);
            else
                return Minecraft.getMinecraft().thePlayer.getDistanceToEntity(stand);
        }

        public Target(EntityLivingBase entity, EntityArmorStand stand) {
            this.entity = entity;
            this.stand = stand;
        }

        public Target(EntityLivingBase entity, EntityArmorStand stand, boolean worm) {
            this.entity = entity;
            this.stand = stand;
            this.worm = worm;
        }
    }

    private enum States {
        SEARCHING,
        ATTACKING,
        BLOCKED_VISION,
        KILLED
    }

    public static boolean hasTarget() {
        return currentState == States.ATTACKING && target != null;
    }

    public void Toggle() {
        if (!isToggled) {
            isToggled = true;
            blockedVisionDelay.reset();
            attackDelay.reset();
            currentState = States.SEARCHING;
            target = null;
            potentialTargets.clear();
        } else {
            isToggled = false;
            target = null;
            potentialTargets.clear();
            rotation.reset();
            isToggled = false;
        }
    }

    public static void setMobsNames(boolean caseSensitive, String... mobsNames) {
        MobKiller.caseSensitive = caseSensitive;
        MobKiller.mobsNames = mobsNames;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (!isToggled) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (PlayerUtils.hasOpenContainer()) return;

        if (mobsNames == null || mobsNames.length == 0) return;


        switch (currentState) {
            case SEARCHING:
                potentialTargets.clear();
                List<Entity> entities = mc.theWorld.loadedEntityList.stream().filter(entity -> entity instanceof EntityArmorStand).filter(entity -> mc.thePlayer.getPositionEyes(1).distanceTo(entity.getPositionVector()) <= scanRange).collect(Collectors.toList());
                List<Entity> filtered = entities.stream().filter(v -> (!v.getName().contains(mc.thePlayer.getName()) && Arrays.stream(mobsNames).anyMatch(mobsName -> {
                    String mobsName1 = StringUtils.stripControlCodes(mobsName);
                    String vName = StringUtils.stripControlCodes(v.getName());
                    String vCustomNameTag = StringUtils.stripControlCodes(v.getCustomNameTag());
                    if (caseSensitive) {
                        return vName.contains(mobsName1) || vCustomNameTag.contains(mobsName1);
                    } else {
                        return vName.toLowerCase().contains(mobsName1.toLowerCase()) || vCustomNameTag.toLowerCase().contains(mobsName1.toLowerCase());
                    }
                }))).collect(Collectors.toList());

                if (filtered.isEmpty())
                    break;

                double distance = 9999;
                Target closestTarget = null;

                for (Entity entity : filtered) {
                    double currentDistance;
                    EntityArmorStand stand = (EntityArmorStand) entity;

                    if (stand.getCustomNameTag().contains("Scatha") || stand.getCustomNameTag().contains("Worm")) {
                        Target target1 = new Target(null, stand, true);

                        if (closestTarget != null) {
                            currentDistance = stand.getDistanceToEntity(mc.thePlayer);
                            if (currentDistance < distance) {
                                distance = currentDistance;
                                closestTarget = target1;
                            }
                        } else {
                            distance = stand.getDistanceToEntity(mc.thePlayer);
                            closestTarget = target1;
                        }

                        potentialTargets.add(target1);

                        continue;
                    }

                    Entity target = NpcUtil.getEntityCuttingOtherEntity(stand, null);

                    if (target == null) continue;
                    if (NpcUtil.getEntityHp(stand) <= 0) continue;

                    boolean visible = PlayerUtils.entityIsVisible(target);

                    if (!visible) continue;

                    if (target instanceof EntityLivingBase) {

                        Target target1 = new Target((EntityLivingBase) target, stand);

                        if (closestTarget != null) {
                            currentDistance = target.getDistanceToEntity(mc.thePlayer);
                            if (currentDistance < distance) {
                                distance = currentDistance;
                                closestTarget = target1;
                            }
                        } else {
                            distance = target.getDistanceToEntity(mc.thePlayer);
                            closestTarget = target1;
                        }

                        potentialTargets.add(target1);
                    }
                }

                if (closestTarget != null && closestTarget.distance() < scanRange) {
                    target = closestTarget;
                    currentState = States.ATTACKING;
                }
                break;
            case ATTACKING:

                if (NpcUtil.getEntityHp(target.stand) <= 0 || target.distance() > scanRange || target.stand == null || (target.entity != null && (target.entity.isDead || target.entity.getHealth() < 0.0))) {
                    currentState = States.KILLED;
                    afterKillDelay.reset();
                    break;
                }

                if (MightyMiner.config.useHyperionUnderPlayer) {
                    int weapon = PlayerUtils.getItemInHotbar("Hyperion");

                    if (weapon == -1) {
                        LogUtils.addMessage("MobKiller - No Hyperion found");
                        return;
                    }

                    mc.thePlayer.inventory.currentItem = weapon;

                    if (target.distance() > 5.5) return;


                    if (rotation.completed)
                        rotation.initAngleLock(mc.thePlayer.rotationYaw, 89, MightyMiner.config.mobKillerCameraSpeed);

                    if (AngleUtils.isDiffLowerThan(mc.thePlayer.rotationYaw, 89, 0.5f)) {
                        rotation.reset();
                        rotation.completed = true;
                    }

                    if (!rotation.completed) return;

                    if (attackDelay.hasReached(MightyMiner.config.mobKillerAttackDelay) && target.distance() <= 6) {
                        rightClick();
                        attackDelay.reset();
                    }

                } else {

                    int weapon;

                    if (!MightyMiner.config.customItemToKill.isEmpty()) {
                        weapon = PlayerUtils.getItemInHotbar(MightyMiner.config.customItemToKill);
                    } else {
                        weapon = PlayerUtils.getItemInHotbar("Juju", "Terminator", "Bow", "Frozen Scythe", "Glacial Scythe");
                    }

                    if (weapon == -1) {
                        LogUtils.addMessage("MobKiller - No weapon found");
                        return;
                    }

                    mc.thePlayer.inventory.currentItem = weapon;

                    if (target.worm) {
                        rotation.initAngleLock(AngleUtils.getRequiredYawSide(target.stand.getPosition()), AngleUtils.getRequiredPitchSide(target.stand.getPosition()), MightyMiner.config.mobKillerCameraSpeed);
                    } else {
                        rotation.initAngleLock(AngleUtils.getRequiredYawSide(target.entity.getPosition()), AngleUtils.getRequiredPitchSide(target.entity.getPosition()), MightyMiner.config.mobKillerCameraSpeed);

                    }

                    if (AngleUtils.isDiffLowerThan(AngleUtils.getRequiredYawSide(target.entity.getPosition()), AngleUtils.getRequiredPitchSide(target.entity.getPosition()), 1f)) {
                        rotation.reset();
                        rotation.completed = true;
                    }

                    if (!rotation.completed) return;

                    boolean visible = PlayerUtils.entityIsVisible(target.entity);

                    if (!target.worm && !visible) {
                        LogUtils.addMessage("MobKiller - Something is blocking target, waiting for free shot...");
                        blockedVisionDelay.reset();
                        currentState = States.BLOCKED_VISION;
                    } else {
                        if (attackDelay.hasReached(MightyMiner.config.mobKillerAttackDelay)) {
                            if (MightyMiner.config.attackButton == 0) {
                                if (target.distance() <= 4.5) {
                                    leftClick();
                                }
                            } else {
                                rightClick();
                            }
                            attackDelay.reset();
                        }
                    }
                }


                break;
            case BLOCKED_VISION:

                if (NpcUtil.getEntityHp(target.stand) <= 0 || target.distance() > MightyMiner.config.mobKillerScanRange) {
                    currentState = States.KILLED;
                    break;
                }

                if (blockedVisionDelay.hasReached(5000)) {
                    currentState = States.ATTACKING;
                    break;
                }

                break;
            case KILLED:

                if (!afterKillDelay.hasReached(150))
                    return;

                target = null;
                currentState = States.SEARCHING;
                break;
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;



        if (potentialTargets.size() > 0) {
            potentialTargets.forEach(v -> {
                if (v != target)
                    DrawUtils.drawEntity(v.worm ? v.stand : v.entity, new Color(100, 200, 100, 200), 2, event.partialTicks);
            });
        }

        if (target != null) {
            if (rotation.rotating)
                rotation.update();

            DrawUtils.drawEntity(target.worm ? target.stand : target.entity, new Color(200, 100, 100, 200), 2, event.partialTicks);
        }
    }

    public static String[] drawInfo() {
        return new String[]{
                "§r§lTarget:",
                "§rName: §f" + (target != null ? NpcUtil.stripString(target.stand.getCustomNameTag()) : "None"),
                "§rDistance: §f" + (target != null ? (String.format("%.2f", target.distance()) + "m") : "No target"),
                "§rHealth: §f" + (target != null ? (NpcUtil.getEntityHp(target.stand)) : "No target"),
                "§rState: §f" + currentState.name()
        };
    }
}
