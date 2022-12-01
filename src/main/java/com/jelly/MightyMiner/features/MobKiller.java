package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;


public class MobKiller {

    public static String MobClass;
    public static int MobRange = 10;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Rotation rotation = new Rotation();

    private static final ArrayList<Entity> mobsList = new ArrayList<>();

    private static Entity target;

    enum States {
        NONE,
        SEARCHING,
        KILLING,
        BLOCKED_VISION,
        KILLED,
    }

    private static States currentState = States.NONE;

    private static Macro lastMacro;

    public static boolean enabled = false;

    private int waitTicks = 0;

    public static void Reset() {
        if (lastMacro != null) {
            lastMacro.Unpause();
        }
        target = null;
        mobsList.clear();
        currentState = States.NONE;
        lastMacro = null;
    }

    public static void Disable() {
        Reset();
        enabled = false;
        MobClass = null;
        MobRange = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!enabled) return;
        if (MobClass == null || MobClass.isEmpty()) return;

        if (event.phase != TickEvent.Phase.START)
            return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Class<?> type;
        try {
            type = Class.forName(MobClass);
        } catch (ClassNotFoundException e) {
            LogUtils.addMessage("Mob class not found: " + MobClass);
            enabled = false;
            return;
        }

        if (!PlayerUtils.hasMobsInRadius(MobRange, MobClass)) {
            mobsList.clear();
            return;
        }

        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (!type.isInstance(entity)) continue;
            if (NpcUtil.isNpc(entity)) continue;
            if (mobsList.stream().noneMatch(mob -> mob.getEntityId() == entity.getEntityId() && (!isTargetDead(entity)))) {
                mobsList.add(entity);
            } else if (mobsList.stream().anyMatch(mob -> mob.getEntityId() == entity.getEntityId() && (isTargetDead(entity) || entity.getDistanceToEntity(mc.thePlayer) > MobRange))) {
                mobsList.remove(entity);
            }
        }
    }

    @SubscribeEvent
    public void onTickSecond(TickEvent.ClientTickEvent event) {
        if (!enabled) return;
        if (MobClass == null || MobClass.isEmpty()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event.phase != TickEvent.Phase.START)
            return;

        if (waitTicks > 0){
            waitTicks--;
            if (lastMacro != null && lastMacro.isPaused()) {
                lastMacro.Unpause();
            }
            return;
        }

        switch (currentState) {
            case NONE: {
                if (hasAliveMobsAround() && PlayerUtils.hasMobsInRadius(MobRange, MobClass)) {
                    MacroHandler.macros.forEach(macro -> {
                        if (macro.isEnabled()) {
                            lastMacro = macro;
                            macro.Pause();
                        }
                    });
                    currentState = States.SEARCHING;
                } else if (!hasAliveMobsAround() || mobsList.isEmpty()) {
                    Reset();
                    return;
                }
                break;
            }
            case SEARCHING: {
                target = PlayerUtils.getClosestMob(mobsList);
                if (target != null) {
                    currentState = States.KILLING;
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindAttack, false);
                    return;
                }
                if (!hasAliveMobsAround() || mobsList.isEmpty()) {
                    Reset();
                    return;
                }
                break;
            }
            case KILLING: {

                if (isTargetDead(target) || target.getDistanceToEntity(mc.thePlayer) > MobRange) {
                    currentState = States.KILLED;
                    target = null;
                    return;
                }

                int weapon = PlayerUtils.getItemInHotbar(true,"Juju", "Terminator", "Bow", "Frozen Scythe");

                if (weapon == -1) {
                    LogUtils.addMessage("You've got no weapon to shoot!");
                    MightyMiner.config.killYogs = false;
                    Reset();
                    return;
                }

                mc.thePlayer.inventory.currentItem = weapon;


                rotation.intLockAngle(
                    AngleUtils.getRequiredYaw(target.posX - mc.thePlayer.posX, target.posZ - mc.thePlayer.posZ),
                    AngleUtils.getRequiredPitch(target.posX - mc.thePlayer.posX, (target.posY + (target.height/2)) - (mc.thePlayer.posY + mc.thePlayer.eyeHeight), target.posZ - mc.thePlayer.posZ),
                    125);

                if (!rotation.completed) return;

                Entity pointedEntity = PlayerUtils.entityIsVisible(target);
                if (pointedEntity != null && pointedEntity.getEntityId() == target.getEntityId()) {
                    // Kill the mob
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, true);
                } else {
                    LogUtils.addMessage("Something is blocking target, waiting for free shot...");
                    KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, false);
                    currentState = States.BLOCKED_VISION;
                    waitTicks = 50;
                }
                break;
            }
            case BLOCKED_VISION: {
                if (isTargetDead(target)) {
                    currentState = States.KILLED;
                    target = null;
                    return;
                }
                // TODO: Move to a better position or just mind your own business idk.
                // Temporary solution: Wait for 50 ticks and try again.
                currentState = States.KILLING;
                break;
            }
            case KILLED: {
                KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, false);
                if (!hasAliveMobsAround()) {
                    mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Drill", "Gauntlet", "Pick");
                    Reset();
                } else {
                    currentState = States.SEARCHING;
                }
                break;
            }
        }
    }

    private boolean isTargetDead(Entity target) {
        if (target == null) return true;
        if (target.isDead) return true;
        if (target instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) target;
            if (entityLiving.getHealth() <= 0) return true;
            if (target instanceof EntitySlime) {
                EntitySlime magmaCube = (EntitySlime) target;
                if (magmaCube.getSlimeSize() <= 1) {
                    return (magmaCube.getHealth() <= 0);
                } else {
                    return (magmaCube.getHealth() <= 1);
                }
            }
        }
        return false;
    }

    private boolean hasAliveMobsAround() {
        return mobsList.stream().anyMatch(entity -> !isTargetDead(entity) && entity.getDistanceToEntity(mc.thePlayer) <= MobRange);
    }

    @SubscribeEvent
    public void onLastRender(RenderWorldLastEvent event){
        if (!enabled) return;
        if(rotation.rotating)
            rotation.update();

        if (target != null) {
            DrawUtils.drawEntity(target, 3, new Color(93, 129, 246, 231), event.partialTicks);
        }
    }
}
