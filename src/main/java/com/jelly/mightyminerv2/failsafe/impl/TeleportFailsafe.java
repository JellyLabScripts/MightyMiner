package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.failsafe.FailsafeManager;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.feature.impl.LagDetector;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.macro.commissionmacro.CommissionMacro;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.util.List;

public class TeleportFailsafe extends AbstractFailsafe {

  private static final TeleportFailsafe instance = new TeleportFailsafe();
  private final LagDetector lagDetector = LagDetector.getInstance();
  private final Minecraft mc = Minecraft.getMinecraft();
  private final Clock deathCheckClock = new Clock();
  private boolean monitoringDeathMessages = false;

  public static TeleportFailsafe getInstance() {
    return instance;
  }

  @Override
  public String getName() {
    return "TeleportFailsafe";
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
  public boolean onPacketReceive(PacketEvent.Received event) {
    if (!(event.packet instanceof S08PacketPlayerPosLook)) {
      return false;
    }

    startDeathMessageMonitoring();
    return true;
  }

  @Override
  public boolean react() {
    warn("Stopping macro due to teleport.");
    MacroManager.getInstance().disable();
    return true;
  }

  private void startDeathMessageMonitoring() {
    monitoringDeathMessages = true;
    deathCheckClock.schedule(3000); // Monitor for 3 seconds
  }

  @SubscribeEvent
  public void handleClientTick(ClientTickEvent event) {
    if (monitoringDeathMessages) {
      if (deathCheckClock.passed() || isPlayerRecentlyDied()) {
        if (isPlayerRecentlyDied()) {
          // Remove from failsafe queue if a death message is detected
          warn("Death message detected, canceling teleport failsafe.");
          FailsafeManager.getInstance().removeFailsafeFromQueue(this);
          /*Commissionmacro state should be set to pathing or pathing should be restarted because other wise it causes bugs (iam too dumb for this)
          if(MacroManager.getInstance().getCurrentMacro().getName() == "Commission Macro") {
          }*/
        }
        monitoringDeathMessages = false;
      }
    }
  }


  private boolean isPlayerRecentlyDied() {
    List<String> chatMessages = mc.ingameGUI.getChatGUI().getSentMessages();

    for (int i = chatMessages.size() - 1; i >= Math.max(0, chatMessages.size() - 10); i--) {
      String message = chatMessages.get(i).toLowerCase();

      if (message.contains("fell into the void") ||
              message.contains("was knocked into the void by") ||
              message.contains("was slain by") ||
              message.contains("burned to death") ||
              message.contains("fell to death") ||
              message.contains("fell to their death with help from") ||
              message.contains("suffocated") ||
              message.contains("drowned") ||
              message.contains("was pricked to death by a cactus") ||
              message.contains("died")) {
        return true;
      }
    }
    return false;
  }

}
