package com.jelly.MightyMinerV2.Mixin.Network;

import com.jelly.MightyMinerV2.Event.PacketEvent;
import com.jelly.MightyMinerV2.Event.UpdateScoreboardLineEvent;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "createNetworkManagerAndConnect", at = @At("HEAD"), cancellable = true)
    private static void createNetworkManagerAndConnect(InetAddress address, int serverPort, boolean useNativeTransport, CallbackInfoReturnable<NetworkManager> callbackInfoReturnable) {

        final NetworkManager networkManager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);
        Bootstrap bootstrap = new Bootstrap();

        EventLoopGroup eventLoopGroup;
    }

    @Unique
    private final Map<Integer, String> mightyMinerV2$cachedScoreboard = new HashMap<>();

    @Inject(method = "channelRead0*", at = @At("HEAD"))
    private void read(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
        if (packet.getClass().getSimpleName().startsWith("S")) {
            MinecraftForge.EVENT_BUS.post(new PacketEvent.Received(packet));
        } else if (packet.getClass().getSimpleName().startsWith("C")) {
            MinecraftForge.EVENT_BUS.post(new PacketEvent.Sent(packet));
        }
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) return;
        if (packet instanceof S3DPacketDisplayScoreboard || packet instanceof S3CPacketUpdateScore || packet instanceof S3EPacketTeams) {
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
                String clean = mightyMinerV2$cleanSB(string);
                if (!clean.equals(mightyMinerV2$cachedScoreboard.get(index)) || !mightyMinerV2$cachedScoreboard.containsKey(index)) {
                    mightyMinerV2$cachedScoreboard.put(index, clean);
                    MinecraftForge.EVENT_BUS.post(new UpdateScoreboardLineEvent(string, clean));
                }
                index++;
                if (index > 15) break;
            }
        }
    }

    @Unique
    private String mightyMinerV2$cleanSB(String scoreboard) {
        StringBuilder cleaned = new StringBuilder();

        for (char c : StringUtils.stripControlCodes(scoreboard).toCharArray()) {
            if (c >= 32 && c < 127 || c == 'ൠ') {
                cleaned.append(c);
            }
        }

        return cleaned.toString();
    }
}
