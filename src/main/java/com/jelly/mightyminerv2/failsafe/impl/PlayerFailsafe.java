package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.EntityUtil;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerFailsafe extends AbstractFailsafe {

    @Getter
    private static final PlayerFailsafe instance = new PlayerFailsafe();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Map<Entity, Long> playerStaringTimes = new HashMap<>();

    @Override
    public String getName() {
        return "PlayerFailsafe";
    }

    @Override
    public Failsafe getFailsafeType() {
        return Failsafe.TELEPORT;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public boolean onTick(ClientTickEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return false;

        List<Entity> players = mc.theWorld.loadedEntityList.stream().filter(
                (entity) -> entity instanceof AbstractClientPlayer && entity != mc.thePlayer && !EntityUtil.isNpc(entity)
        ).collect(Collectors.toList());

        boolean playerBlocking = false;
        long currentTime = System.currentTimeMillis();
        playerStaringTimes.keySet().removeIf(player -> !players.contains(player));

        for (Entity player : players) {
            double distanceSquared = player.getPosition().distanceSq(mc.thePlayer.getPosition());
            boolean isBlocking = distanceSquared < 3;
            boolean isLookingAtUs = isPlayerLookingAtMe(player);

            if (isLookingAtUs && isBlocking) {
                if (!playerStaringTimes.containsKey(player)) {
                    playerStaringTimes.put(player, currentTime);
                    return false;
                }

                long staringDuration = currentTime - playerStaringTimes.get(player);

                if (staringDuration > 1000) {
                    playerBlocking = true;
                    break;
                }
            } else {
                playerStaringTimes.remove(player);
            }
        }

        return playerBlocking;
    }

    private boolean isPlayerLookingAtMe(Entity player) {
        Vec3 lookVec = player.getLook(1.0F);
        Vec3 playerToMeVec = new Vec3(
                mc.thePlayer.posX - player.posX,
                mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight() - (player.posY + player.getEyeHeight()),
                mc.thePlayer.posZ - player.posZ
        );

        double d0 = playerToMeVec.lengthVector();
        playerToMeVec = playerToMeVec.normalize();

        double dot = lookVec.dotProduct(playerToMeVec);
        double fovCosine = Math.cos(Math.toRadians(30F));
        return dot > fovCosine;
    }

    @Override
    public boolean onChat(ClientChatReceivedEvent event) {
        return event.message.getUnformattedText().toLowerCase().contains(mc.getSession().getUsername().toLowerCase());
    }

    @Override
    public boolean react() {
        warn("Stopping macro due to player nearby.");
        MacroManager.getInstance().disable();
        return true;
    }

}
