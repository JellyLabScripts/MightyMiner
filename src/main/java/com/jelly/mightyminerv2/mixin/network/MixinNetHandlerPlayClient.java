package com.jelly.mightyminerv2.mixin.network;

import com.jelly.mightyminerv2.event.BlockDestroyEvent;
import com.jelly.mightyminerv2.event.SpawnObjectEvent;
import com.jelly.mightyminerv2.event.SpawnParticleEvent;
import com.jelly.mightyminerv2.event.UpdateTablistEvent;
import com.jelly.mightyminerv2.event.UpdateTablistFooterEvent;
import com.jelly.mightyminerv2.util.TablistUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S25PacketBlockBreakAnim;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

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

  @Inject(method = "handleSpawnObject", at = @At(value = "HEAD"))
  public void handleSpawnObject(S0EPacketSpawnObject packetIn, CallbackInfo ci) {
    SpawnObjectEvent event = new SpawnObjectEvent(
        packetIn.getEntityID(),
        packetIn.getX() / 32f,
        packetIn.getY() / 32f,
        packetIn.getZ() / 32f,
        packetIn.getSpeedX(),
        packetIn.getSpeedY(),
        packetIn.getSpeedZ(),
        packetIn.getYaw(),
        packetIn.getPitch(),
        packetIn.getType()
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
}
