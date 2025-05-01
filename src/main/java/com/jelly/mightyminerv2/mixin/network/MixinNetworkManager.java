package com.jelly.mightyminerv2.mixin.network;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.event.UpdateScoreboardLineEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "channelRead0*", at = @At("HEAD"))
    private void read(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
        if (packet.getClass().getSimpleName().startsWith("S")) {
            MinecraftForge.EVENT_BUS.post(new PacketEvent.Received(packet));
        } else if (packet.getClass().getSimpleName().startsWith("C")) {
            MinecraftForge.EVENT_BUS.post(new PacketEvent.Sent(packet));
        }

        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) return;
        if (packet instanceof S3CPacketUpdateScore || packet instanceof S3DPacketDisplayScoreboard || packet instanceof S3EPacketTeams) {
            Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();
            Collection<Score> scores;
            try {
                scores = scoreboard.getSortedScores(scoreboard.getObjectiveInDisplaySlot(1));
            } catch (NullPointerException e) {
                return;
            }
            scores.removeIf(score -> score.getPlayerName().startsWith("#"));

            int index = 0;
            for (Score score : scores) {
                ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(score.getPlayerName());
                String string = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score.getPlayerName());
                String clean = mightyMiner$cleanSB(string);
                if (!clean.equals(mightyMiner$cachedScoreboard.get(index)) || !mightyMiner$cachedScoreboard.containsKey(index)) {
                    mightyMiner$cachedScoreboard.put(index, clean);
                    MinecraftForge.EVENT_BUS.post(new UpdateScoreboardLineEvent(clean));
                }
                index++;
                if (index > 15) break;
            }
        }
    }

    @Unique
    private final Map<Integer, String> mightyMiner$cachedScoreboard = new HashMap<>();

    @Unique
    private String mightyMiner$cleanSB(String scoreboard) {
        StringBuilder cleaned = new StringBuilder();

        for (char c : StringUtils.stripControlCodes(scoreboard).toCharArray()) {
            if (c >= 32 && c < 127 || c == 'ൠ') {
                cleaned.append(c);
            }
        }

        return cleaned.toString();
    }

}
