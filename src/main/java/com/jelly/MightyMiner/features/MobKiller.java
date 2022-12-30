package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.jelly.MightyMiner.handlers.KeybindHandler.rightClick;


public class MobKiller {

    private final Minecraft mc = Minecraft.getMinecraft();

    private Target target;

    private static boolean caseSensitive = false;
    private static String[] mobsNames = null;

    private final Timer attackDelay = new Timer();
    private final Timer blockedVisionDelay = new Timer();
    private States currentState = States.SEARCHING;

    private final Rotation rotation = new Rotation();

    private final ArrayList<Target> potentialTargets = new ArrayList<>();

    public static int scanRange = 15;

    public static boolean enabled = false;

    private Macro lastMacro = null;

    private static class Target {
        public EntityLiving entity;
        public EntityArmorStand stand;

        public double distance() {
            return Minecraft.getMinecraft().thePlayer.getDistanceToEntity(entity);
        }

        public Target(EntityLiving entity, EntityArmorStand stand) {
            this.entity = entity;
            this.stand = stand;
        }
    }

    private enum States {
        SEARCHING,
        ATTACKING,
        BLOCKED_VISION,
        KILLED
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void Enable() {
        enabled = true;
        onEnable();
    }

    public void Disable() {
        enabled = false;
        onDisable();
    }

    public void onEnable() {
        blockedVisionDelay.reset();
        attackDelay.reset();
        currentState = States.SEARCHING;
        target = null;
        potentialTargets.clear();
    }

    public void onDisable() {
        target = null;
        potentialTargets.clear();
        rotation.reset();
    }

    public static void setMobsNames(boolean caseSensitive, String... mobsNames) {
        MobKiller.caseSensitive = caseSensitive;
        MobKiller.mobsNames = mobsNames;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (!enabled) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (mobsNames == null || mobsNames.length == 0) return;

        if (mc.currentScreen != null && !(mc.currentScreen instanceof net.minecraft.client.gui.GuiChat))
            return;

        switch (currentState) {
            case SEARCHING:
                potentialTargets.clear();
                List<Entity> entities = mc.theWorld.loadedEntityList.stream().filter(entity -> entity instanceof EntityArmorStand).collect(Collectors.toList());
                List<Entity> filtered = entities.stream().filter(v -> (!v.getName().contains(mc.thePlayer.getName()) && Arrays.stream(mobsNames).anyMatch(mobsName -> {
                    String mobsName1 = StringUtils.stripControlCodes(mobsName);
                    String vName = StringUtils.stripControlCodes(v.getName());
                    String vCustomNameTag = StringUtils.stripControlCodes(v.getCustomNameTag());
                    if (caseSensitive) {
                        return vName.contains(mobsName1) || vCustomNameTag.contains(mobsName1);
                    } else {
                        return vName.toLowerCase().contains(mobsName1.toLowerCase()) || vCustomNameTag.toLowerCase().contains(mobsName1.toLowerCase());
                    }
                }))).filter(PlayerUtils::entityIsVisible).collect(Collectors.toList());

                if (filtered.size() > 0) {

                    double distance = 9999;
                    Target closestTarget = null;

                    for (Entity entity : filtered) {
                        double currentDistance;
                        EntityArmorStand stand = (EntityArmorStand) entity;
                        Entity target = NpcUtil.getEntityCuttingOtherEntity(stand, null);

                        if (target == null) continue;

                        if (NpcUtil.getEntityHp(stand) <= 0) continue;
                        boolean entity1 = PlayerUtils.entityIsVisible(target);
                        if (!entity1) continue;

                        if (target instanceof EntityLiving) {

                            Target target1 = new Target((EntityLiving) target, stand);

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

                    if (closestTarget != null && closestTarget.distance() <= scanRange) {
                        target = closestTarget;
                        currentState = States.ATTACKING;
                        MacroHandler.macros.forEach(macro -> {
                            if (macro.isEnabled()) {
                                lastMacro = macro;
                                macro.Pause();
                            }
                        });
                    }
                } else {
                    if (lastMacro != null) {
                        lastMacro.Unpause();
                        lastMacro = null;
                    }
                    onDisable();
                }
                break;
            case ATTACKING:

                if (NpcUtil.getEntityHp(target.stand) <= 0 || target.distance() > scanRange) {
                    currentState = States.KILLED;
                    break;
                }

                int weapon = PlayerUtils.getItemInHotbar(true, "Juju", "Terminator", "Bow", "Frozen Scythe", "Glacial Scythe");

                if (weapon == -1) {
                    LogUtils.addMessage("No weapon found");
                    currentState = States.SEARCHING;
                    toggle();
                    return;
                }

                mc.thePlayer.inventory.currentItem = weapon;

                Tuple<Float, Float> angles = AngleUtils.getRequiredRotationToEntity(target.entity);

                rotation.initAngleLock(angles.getFirst(), angles.getSecond(), 300);

                if (rotation.rotating) return;

                boolean pointedEntity = PlayerUtils.entityIsVisible(target.entity);
                if (pointedEntity) {
                    if (attackDelay.hasReached(120)) {
                        rightClick();
                        attackDelay.reset();
                    }
                } else {
                    LogUtils.addMessage("Something is blocking target, waiting for free shot...");
                    blockedVisionDelay.reset();
                    currentState = States.BLOCKED_VISION;
                }

                break;
            case BLOCKED_VISION:

                if (NpcUtil.getEntityHp(target.stand) <= 0 || target.distance() > scanRange) {
                    currentState = States.KILLED;
                    break;
                }

                if (blockedVisionDelay.hasReached(5000)) {
                    currentState = States.ATTACKING;
                    break;
                }

                break;
            case KILLED:
                target = null;
                currentState = States.SEARCHING;
                break;
        }
    }

    @SubscribeEvent
    public void onLastRender(RenderWorldLastEvent event) {
        if(rotation.rotating)
            rotation.update();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!enabled) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (potentialTargets.size() > 0) {
            potentialTargets.forEach(v -> DrawUtils.drawEntity(v.entity, new Color(100, 200, 100, 200), 2, event.partialTicks));
        }

        if (target != null) {
            DrawUtils.drawEntity(target.entity, new Color(200, 100, 100, 200), 2, event.partialTicks);
        }
    }
}
