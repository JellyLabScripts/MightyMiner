package com.jelly.mightyminerv2.mixin.network;

import com.jelly.mightyminerv2.event.BlockDestroyEvent;
import com.jelly.mightyminerv2.event.UpdateEntityEvent;
import com.jelly.mightyminerv2.event.SpawnParticleEvent;
import com.jelly.mightyminerv2.event.UpdateScoreboardEvent;
import com.jelly.mightyminerv2.event.UpdateTablistEvent;
import com.jelly.mightyminerv2.event.UpdateTablistFooterEvent;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.ScoreboardUtil;
import com.jelly.mightyminerv2.util.TablistUtil;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S25PacketBlockBreakAnim;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.network.play.server.S3CPacketUpdateScore.Action;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

  @Inject(method = "handleParticles", at = @At(value = "HEAD"))
  public void handleParticles(S2APacketParticles packetIn, CallbackInfo ci) {
    SpawnParticleEvent event = new SpawnParticleEvent(
        packetIn.getParticleType(),
        packetIn.isLongDistance(),
        packetIn.getXCoordinate(),
        packetIn.getYCoordinate(),
        packetIn.getZCoordinate(),
        packetIn.getXOffset(),
        packetIn.getYOffset(),
        packetIn.getZOffset(),
        packetIn.getParticleArgs()
    );
    MinecraftForge.EVENT_BUS.post(event);
  }

  @Unique
  private final List<String> mightyMinerV2$previousTablist = new ArrayList<>();
  @Unique
  private final List<String> mightyMinerV2$previousFooter = new ArrayList<>();

  @Inject(method = "handlePlayerListItem", at = @At(value = "RETURN"))
  public void handlePlayerListItem(S38PacketPlayerListItem packetIn, CallbackInfo ci) {
    List<String> tablist = new ArrayList<>();
    List<NetworkPlayerInfo> players =
        TablistUtil.playerOrdering.sortedCopy(Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap());

    GuiPlayerTabOverlay tabOverlay = Minecraft.getMinecraft().ingameGUI.getTabList();

    for (NetworkPlayerInfo info : players) {
      tablist.add(StringUtils.stripControlCodes(tabOverlay.getPlayerName(info)));
    }
    if (tablist.equals(mightyMinerV2$previousTablist)) {
      return;
    }
    mightyMinerV2$previousTablist.clear();
    mightyMinerV2$previousTablist.addAll(tablist);
    TablistUtil.setCachedTablist(tablist);
    MinecraftForge.EVENT_BUS.post(new UpdateTablistEvent(tablist, System.currentTimeMillis()));
  }

  @Inject(method = "handlePlayerListHeaderFooter", at = @At("RETURN"))
  public void handlePlayerListHeaderFooter(S47PacketPlayerListHeaderFooter packetIn, CallbackInfo ci) {
    List<String> footer = new ArrayList<>();
    if (packetIn.getFooter() == null) {
      return;
    }
    for (String s : packetIn.getFooter().getFormattedText().split("\n")) {
      footer.add(StringUtils.stripControlCodes(s));
    }
    if (footer.equals(mightyMinerV2$previousFooter)) {
      return;
    }
    mightyMinerV2$previousFooter.clear();
    mightyMinerV2$previousFooter.addAll(footer);
    TablistUtil.setCachedTabListFooter(footer);
    MinecraftForge.EVENT_BUS.post(new UpdateTablistFooterEvent(footer));
  }

  @Inject(method = "handleBlockBreakAnim", at = @At("HEAD"))
  public void handleBlockBreakAnim(S25PacketBlockBreakAnim packetIn, CallbackInfo ci) {
    MinecraftForge.EVENT_BUS.post(new BlockDestroyEvent(packetIn.getPosition(), packetIn.getProgress()));
  }

  // Entity spawn stuff
  @Inject(method = "handleEntityMetadata", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/DataWatcher;updateWatchedObjectsFromList(Ljava/util/List;)V", shift = Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
  public void handleEntityMetadata(S1CPacketEntityMetadata packetIn, CallbackInfo ci, Entity entity) {
    if (entity instanceof EntityArmorStand && entity.hasCustomName()) { // right now care about stands with custom name
      MinecraftForge.EVENT_BUS.post(new UpdateEntityEvent((EntityLivingBase) entity));
    }
  }

//  @Inject(method = "handleSpawnObject", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
//  public void handleSpawnObject(S0EPacketSpawnObject packetIn, CallbackInfo ci, double d0, double d1, double d2, Entity entity) {
//    if (packetIn.getType() == 78) {
//      mightyMinerv2$entities.add(packetIn.getEntityID());
//    }
//  }

  @Inject(method = "handleSpawnMob", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
  public void handleSpawnMob(S0FPacketSpawnMob packetIn, CallbackInfo ci, double d0, double d1, double d2, float f, float f1,
      EntityLivingBase entitylivingbase, Entity[] aentity, List list) {
    MinecraftForge.EVENT_BUS.post(new UpdateEntityEvent(entitylivingbase));
  }

  @Inject(method = "handleSpawnPlayer", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
  public void handleSpawnPlayer(S0CPacketSpawnPlayer packetIn, CallbackInfo ci, double d0, double d1, double d2, float f, float f1,
      EntityOtherPlayerMP entityotherplayermp, int i, List list) {
    MinecraftForge.EVENT_BUS.post(new UpdateEntityEvent(entityotherplayermp));
  }

  @Redirect(method = "handleDestroyEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;removeEntityFromWorld(I)Lnet/minecraft/entity/Entity;"))
  public Entity handleDestroyEntities(WorldClient instance, int entityID) {
    Entity entity = instance.removeEntityFromWorld(entityID);
    if (entity instanceof EntityLivingBase) {
      MinecraftForge.EVENT_BUS.post(new UpdateEntityEvent((EntityLivingBase) entity, (byte) 1));
    }
    return entity;
  }

  @Inject(method = "handleEntityMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPositionAndRotation2(DDDFFIZ)V"), locals = LocalCapture.CAPTURE_FAILHARD)
  public void handleEntityMovement(S14PacketEntity packetIn, CallbackInfo ci, Entity entity, double d0, double d1, double d2, float f, float f1) {
    if (entity instanceof EntityLivingBase) {
      int nX = (int) Math.round(d0) / 3;
      int nZ = (int) Math.round(d2) / 3;
      if ((int) Math.round(entity.posX) != nX || (int) Math.round(entity.posZ) != nZ) {
        MinecraftForge.EVENT_BUS.post(new UpdateEntityEvent((EntityLivingBase) entity, pack(nX, nZ)));
      }
    }
  }

  @Inject(method = "handleEntityTeleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/S18PacketEntityTeleport;getYaw()B"), locals = LocalCapture.CAPTURE_FAILHARD)
  public void handleEntityTeleport(S18PacketEntityTeleport packetIn, CallbackInfo ci, Entity entity, double d0, double d1, double d2) {
    if (entity instanceof EntityLivingBase) {
      int nX = (int) Math.round(d0) / 3;
      int nZ = (int) Math.round(d2) / 3;
      if ((int) Math.round(entity.posX) != nX || (int) Math.round(entity.posZ) != nZ) {
        MinecraftForge.EVENT_BUS.post(new UpdateEntityEvent((EntityLivingBase) entity, pack(nX, nZ)));
      }
    }
  }

  @Unique
  private long pack(int x, int z) {
    return ((long) x << 32) | (z & 0xFFFFFFFFL);
  }

  // Scoreboard
  // why the fuck did i do this (to learn how scoreboard works - wasted 1-2 day(s))
  // the other method was clearly better
  // fuck me and my autistic brain

  @Unique
  Map<String, SortedMap<Integer, String>> mightyMinerv2$scoreboard = new HashMap<>();
  @Unique
  String[] mightyMinerv2$objectiveNames = new String[19];

  @Inject(method = "handleDisplayScoreboard", at = @At("TAIL"))
  public void handleDisplayScoreboard(S3DPacketDisplayScoreboard packetIn, CallbackInfo ci) {
    mightyMinerv2$objectiveNames[packetIn.func_149371_c()] = packetIn.func_149370_d();
    ScoreboardUtil.scoreObjNames = Arrays.copyOf(mightyMinerv2$objectiveNames, 19);
  }

  // for some reason this doesn't capture scoreobjective1
  @Inject(method = "handleScoreboardObjective", at = @At("TAIL"))
  public void handleScoreboardObjective(S3BPacketScoreboardObjective packetIn, CallbackInfo ci) {
    String objName = packetIn.func_149339_c();
    SortedMap<Integer, String> removedValue = mightyMinerv2$scoreboard.remove(objName);
    if (packetIn.func_149338_e() != 1) {
      mightyMinerv2$scoreboard.put(objName, packetIn.func_149338_e() == 0 ? new TreeMap<>(Comparator.reverseOrder()) : removedValue);
    }

    String sidebarObjName = mightyMinerv2$objectiveNames[1];
    SortedMap<Integer, String> sidebar = mightyMinerv2$scoreboard.get(sidebarObjName);

    if (objName.equals(sidebarObjName) && sidebar != null) {
      MinecraftForge.EVENT_BUS.post(new UpdateScoreboardEvent(new ArrayList<>(sidebar.values()), System.currentTimeMillis()));
    }

    ScoreboardUtil.scoreboard = new HashMap<>(mightyMinerv2$scoreboard);
  }

  @Inject(method = "handleUpdateScore", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getObjective(Ljava/lang/String;)Lnet/minecraft/scoreboard/ScoreObjective;"), locals = LocalCapture.CAPTURE_FAILHARD)
  public void handleUpdateScorePRE(S3CPacketUpdateScore packetIn, CallbackInfo ci, Scoreboard scoreboard) {
    try {
      if (!StringUtils.isNullOrEmpty(packetIn.getObjectiveName())) {
        int score = scoreboard.getValueFromObjective(packetIn.getPlayerName(), scoreboard.getObjective(packetIn.getObjectiveName())).getScorePoints();
        SortedMap<Integer, String> objective = mightyMinerv2$scoreboard.get(packetIn.getObjectiveName());
        String text = objective.remove(score);
        if (text != null && packetIn.getScoreAction() == Action.CHANGE) {
          objective.put(packetIn.getScoreValue(), text);
        }

        String sidebarObjName = mightyMinerv2$objectiveNames[1];
        SortedMap<Integer, String> sidebar = mightyMinerv2$scoreboard.get(sidebarObjName);

        if (packetIn.getObjectiveName().equals(sidebarObjName) && sidebar != null) {
          MinecraftForge.EVENT_BUS.post(new UpdateScoreboardEvent(new ArrayList<>(sidebar.values()), System.currentTimeMillis()));
        }

        ScoreboardUtil.scoreboard = new HashMap<>(mightyMinerv2$scoreboard);
      }
    } catch (Exception e) {
      Logger.sendNote("Couldn't Handle Update Score. Action: " + packetIn.getScoreAction());
      e.printStackTrace();
    }
  }

  @Inject(method = "handleTeams", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
  public void handleTeams(S3EPacketTeams packetIn, CallbackInfo ci, Scoreboard scoreboard, ScorePlayerTeam scoreplayerteam) {
    try {
      if (packetIn.getAction() == 1 || packetIn.getAction() == 4) {
        scoreplayerteam.getMembershipCollection().forEach(it -> {
          scoreboard.getObjectivesForEntity(it).forEach((a, b) -> {
            mightyMinerv2$scoreboard.get(a.getName()).remove(b.getScorePoints());
          });
        });
      } else {
        scoreplayerteam.getMembershipCollection().forEach(it -> {
          scoreboard.getObjectivesForEntity(it).forEach((a, b) -> {
            mightyMinerv2$scoreboard.get(a.getName()).put(b.getScorePoints(),
                mightyMinerv2$sanitizeString(scoreplayerteam.getColorPrefix() + b.getPlayerName() + scoreplayerteam.getColorSuffix()));
          });
        });
      }

      SortedMap<Integer, String> sidebar = mightyMinerv2$scoreboard.get(mightyMinerv2$objectiveNames[1]);

      if (sidebar != null) {
        MinecraftForge.EVENT_BUS.post(new UpdateScoreboardEvent(new ArrayList<>(sidebar.values()), System.currentTimeMillis()));
      }

      ScoreboardUtil.scoreboard = new HashMap<>(mightyMinerv2$scoreboard);
    } catch (Exception e) {
      Logger.sendNote("Couldn't Handle Update Teams");
      e.printStackTrace();
    }
  }

  @Unique
  public String mightyMinerv2$sanitizeString(String scoreboard) {
    char[] arr = scoreboard.toCharArray();
    StringBuilder cleaned = new StringBuilder();
    for (int i = 0; i < arr.length; i++) {
      char c = arr[i];
      // required for locationtracker to work
      if (c >= 32 && c < 127 || (c == '⏣' || c == 'ф')) {
        cleaned.append(c);
      }
      if (c == 167) {
        i++;
      }
    }
    return cleaned.toString();
  }
}
