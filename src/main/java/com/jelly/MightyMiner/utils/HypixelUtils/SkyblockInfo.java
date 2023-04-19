package com.jelly.MightyMiner.utils.HypixelUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jelly.MightyMiner.events.ReceivePacketEvent;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

// Credits to skytils

public class SkyblockInfo {

    public static String map;
    private static JsonObject data;

    public static boolean onCrystalHollows()
    {
        return Objects.equals(SkyblockInfo.map, MAPS.CrystalHollows.map);
    }

    public enum MAPS {
        PrivateIsland("dynamic"),
        SpiderDen("combat_1"),
        CrimsonIsle("crimson_isle"),
        TheEnd("combat_3"),
        GoldMine("mining_1"),
        DeepCaverns("mining_2"),
        DwarvenMines("mining_3"),
        CrystalHollows("crystal_hollows"),
        FarmingIsland("farming_1"),
        ThePark("foraging_1"),
        Dungeon("dungeon"),
        DungeonHub("dungeon_hub"),
        Hub("hub"),
        DarkAuction("dark_auction"),
        JerryWorkshop("winter");

        public final String map;
        MAPS(String map) {
            this.map = map;
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event)
    {
        map = null;
    }

    @SubscribeEvent
    public void onChatMessage(ReceivePacketEvent event)
    {
        if (event.packet instanceof S02PacketChat) {
            String unformatted = ((S02PacketChat) event.packet).getChatComponent().getUnformattedText();
            if (unformatted.startsWith("{") && unformatted.endsWith("}")) {
                try {
                    Gson g = new Gson();
                    JsonObject data = g.fromJson(unformatted, JsonObject.class);
                    if (data.has("server")) {
                        if (data.has("gametype") && data.has("mode") && data.has("map")) {
                            SkyblockInfo.data = data;
                            map = SkyblockInfo.data.get("mode").getAsString();
                        }
                    }
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        }
    }
}
