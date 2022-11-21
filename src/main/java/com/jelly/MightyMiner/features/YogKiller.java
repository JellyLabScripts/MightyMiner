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
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;


public class YogKiller {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Rotation rotation = new Rotation();

    private static final ArrayList<EntityMagmaCube> yogsList = new ArrayList<>();

    private static EntityMagmaCube target;

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
        yogsList.clear();
        currentState = States.NONE;
        lastMacro = null;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!enabled) return;
        if (event.phase != TickEvent.Phase.START)
            return;
        if (!MightyMiner.config.killYogs || mc.thePlayer == null || mc.theWorld == null) return;

        if (!PlayerUtils.hasYogInRadius(MightyMiner.config.yogsRadius)) {
            yogsList.clear();
            return;
        }

        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (!(entity instanceof EntityMagmaCube)) continue;
            if (NpcUtil.isNpc(entity)) continue;
            if (yogsList.stream().noneMatch(entityMagmaCube -> entityMagmaCube.getEntityId() == entity.getEntityId() && (!isTargetDead(entity)))) {
                yogsList.add((EntityMagmaCube) entity);
            }
        }
    }

    @SubscribeEvent
    public void onTickSecond(TickEvent.ClientTickEvent event) {
        if (!enabled) return;
        if (event.phase != TickEvent.Phase.START)
            return;
        if (!MightyMiner.config.killYogs || mc.thePlayer == null || mc.theWorld == null) return;

        switch (currentState) {
            case NONE: {
                if (hasAliveYogs() && PlayerUtils.hasYogInRadius(MightyMiner.config.yogsRadius)) {
                    MacroHandler.macros.forEach(macro -> {
                        if (macro.isEnabled()) {
                            lastMacro = macro;
                            macro.Pause();
                        }
                    });
                    currentState = States.SEARCHING;
                } else if (!hasAliveYogs() || yogsList.isEmpty()) {
                    Reset();
                    return;
                }
                break;
            }
            case SEARCHING: {
                target = PlayerUtils.getClosestMob(yogsList);
                if (target != null) {
                    currentState = States.KILLING;
                    return;
                }
                if (!hasAliveYogs() || yogsList.isEmpty()) {
                    Reset();
                    return;
                }
                break;
            }
            case KILLING: {

                if (isTargetDead(target)) {
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
                    250);

                if (!rotation.completed) return;

                Entity pointedEntity = PlayerUtils.entityIsVisible(target);
                if (pointedEntity != null && pointedEntity.getEntityId() == target.getEntityId()) {
                    // Kill the yog
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
                if (waitTicks-- > 0) return;
                currentState = States.KILLING;
                break;
            }
            case KILLED: {
                KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, false);
                if (hasAliveYogs()) {
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
        return target == null || target.isDead || ((target instanceof EntityLiving) && ((EntityLiving) target).getHealth() <= 0);
    }

    private boolean hasAliveYogs() {
        return yogsList.stream().anyMatch(entityMagmaCube -> !isTargetDead(entityMagmaCube));
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
