package com.jelly.MightyMiner.utils;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class NpcUtil {


    /**
     * CREDIT: <a href="https://github.com/BiscuitDevelopment/SkyblockAddons/blob/7334393cc39010a37ebcb41f0beef5fc4bc7f447/src/main/java/codes/biscuit/skyblockaddons/core/npc/NPCUtils.java">...</a>
     * Checks if the given entity is an NPC
     *
     * @param entity the entity to check
     * @return {@code true} if the entity is an NPC, {@code false} otherwise
     */
    public static boolean isNpc(Entity entity) {
        if (!(entity instanceof EntityOtherPlayerMP)) {
            return false;
        }

        EntityLivingBase entityLivingBase = (EntityLivingBase) entity;

        return entity.getUniqueID().version() == 2 && entityLivingBase.getHealth() == 20.0F && !entityLivingBase.isPlayerSleeping() && !entity.isInvisible();
    }

    @SuppressWarnings("unused")
    private static final String[] npcNames = {
            "Golden Goblin",
            "Goblin",
            "Weakling",
            "Fireslinger",
            "Executive Viper",
            "Grunt",
            "Eliza",
            "Fraiser",
            "Wilson",
            "Ceanna",
            "Carlton",
            "Treasure Hoarder",
            "Team Treasuire",
            "Star Centry"
    };

}
