package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.*;
import com.jelly.MightyMiner.utils.HypixelUtils.NpcUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Tuple;
import net.minecraft.util.Vec3;
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
        public Entity entity;
        public EntityArmorStand stand;
        public boolean worm;
        public double distance() {
            if (entity != null)
                return Minecraft.getMinecraft().thePlayer.getDistanceToEntity(entity);
            else
                return Minecraft.getMinecraft().thePlayer.getDistanceToEntity(stand);
        }

        public Target(Entity entity, EntityArmorStand stand) {
            this.entity = entity;
            this.stand = stand;
        }

        public Target(Entity entity, EntityArmorStand stand, boolean worm) {
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
        return target != null;
    }

    public void toggle() {
        if (!isToggled) {
            blockedVisionDelay.reset();
            attackDelay.reset();
            currentState = States.SEARCHING;
            rotation.reset();
        }
        isToggled = !isToggled;
        target = null;
        potentialTargets.clear();
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
                    String vCustomNameTag = StringUtils.stripControlCodes(v.getCustomNameTag());
                    if (caseSensitive) {
                        return vCustomNameTag.contains(mobsName1);
                    } else {
                        return vCustomNameTag.toLowerCase().contains(mobsName1.toLowerCase());
                    }
                }))).collect(Collectors.toList());

                if (filtered.isEmpty())
                    break;

                double distance = 9999;
                Target closestTarget = null;

                for (Entity entity : filtered) {
                    double currentDistance;
                    EntityArmorStand stand = (EntityArmorStand) entity;

                    Target target1;
                    Entity target;

                    if (stand.getCustomNameTag().contains("Scatha") || stand.getCustomNameTag().contains("Worm")) {
                        target1 = new Target(null, stand, true);
                        target = stand;
                    } else {
                        target = NpcUtil.getEntityCuttingOtherEntity(stand, null);

                        if (target == null) continue;
                        if (NpcUtil.getEntityHp(stand) <= 0) continue;
                        if (!PlayerUtils.entityIsVisible(target)) continue;

                        target1 = new Target(target, stand, false);
                    }

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

                if (closestTarget != null && closestTarget.distance() < scanRange) {
                    target = closestTarget;
                    currentState = States.ATTACKING;
                }
                break;
            case ATTACKING:

                if (NpcUtil.getEntityHp(target.entity) <= 0 || target.distance() > scanRange || (target.entity != null && (target.entity.isDead)) || target.stand.getCustomNameTag().isEmpty()) {
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

                    if (!rotation.completed) return;

                    rotation.initAngleLock(mc.thePlayer.rotationYaw, 89, MightyMiner.config.mobKillerCameraSpeed);

                    if (AngleUtils.isDiffLowerThan(mc.thePlayer.rotationYaw, 89, 0.5f)) {
                        rotation.reset();
                        rotation.completed = true;
                    }

                    if (attackDelay.hasReached(MightyMiner.config.mobKillerAttackDelay) && target.distance() < 6) {
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

                    boolean visible;
                    boolean targeted;

                    if (target.worm) {
                        visible = PlayerUtils.entityIsVisible(target.stand);
                    } else {
                        visible = PlayerUtils.entityIsVisible(target.entity);
                    }

                    if (!visible) {
                        LogUtils.addMessage("MobKiller - Something is blocking target, waiting for free shot...");
                        blockedVisionDelay.reset();
                        currentState = States.BLOCKED_VISION;
                    } else {
                        targeted = PlayerUtils.entityIsTargeted(target.entity);
                        if (targeted) {
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

                    if (!rotation.completed) return;

                    int yawRotation;
                    int pitchRotation;

                    Tuple<Float, Float> angles;
                    if (target.worm) {
                        angles = AngleUtils.getRotation(target.stand.getPositionVector().add(new Vec3(0, 0.2f, 0)));
                    } else {
                        angles = AngleUtils.getRotation(target.entity.getPositionVector().add(new Vec3(0, target.entity.height / 2, 0)));
                    }
                    yawRotation = (int) angles.getFirst().floatValue();
                    pitchRotation = (int) angles.getSecond().floatValue();
                    rotation.initAngleLock(yawRotation, pitchRotation, MightyMiner.config.mobKillerCameraSpeed);
                }

                break;
            case BLOCKED_VISION:
                if (NpcUtil.getEntityHp(target.entity) <= 0 || target.distance() > MightyMiner.config.mobKillerScanRange) {
                    currentState = States.KILLED;
                    afterKillDelay.reset();
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
            DrawUtils.drawEntity(target.worm ? target.stand : target.entity, new Color(200, 100, 100, 200), 2, event.partialTicks);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLastRotation(RenderWorldLastEvent event) {
        if (rotation.rotating)
            rotation.update();
    }

    public static String[] drawInfo() {
        return new String[]{
                "§r§lTarget:",
                "§rName: §f" + (target != null ? NpcUtil.stripString(target.stand.getCustomNameTag()) : "None"),
                "§rDistance: §f" + (target != null ? (String.format("%.2f", target.distance()) + "m") : "No target"),
                "§rHealth: §f" + (target != null ? (NpcUtil.getEntityHp(target.entity != null ? target.entity : target.stand)) : "No target"),
                "§rState: §f" + currentState.name(),
                "§rVisible: §f" + (target != null ? (PlayerUtils.entityIsVisible(target.entity != null ? target.entity : target.stand) ? "Yes" : "No") : "No target"),
        };
    }
}
