package com.jelly.mightyminerv2.Feature.impl;

import com.jelly.mightyminerv2.Config.MightyMinerConfig;
import com.jelly.mightyminerv2.Event.PacketEvent;
import com.jelly.mightyminerv2.Feature.IFeature;
import com.jelly.mightyminerv2.Handler.RotationHandler;
import com.jelly.mightyminerv2.Util.AngleUtil;
import com.jelly.mightyminerv2.Util.KeyBindUtil;
import com.jelly.mightyminerv2.Util.PathfindUtil;
import com.jelly.mightyminerv2.Util.PlayerUtil;
import com.jelly.mightyminerv2.Util.helper.Clock;
import com.jelly.mightyminerv2.Util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.Util.helper.Target;
import com.jelly.mightyminerv2.Util.helper.route.Route;
import com.jelly.mightyminerv2.Util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.Util.helper.route.TransportMethod;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

// This works under the assumption that the blocks between every node are clear and traversable.
// Checks to determine if the player can move between two nodes must be done beforehand; otherwise, it will cause bugs.

@Getter
public class RouteNavigator implements IFeature {

  private static RouteNavigator instance;

  public static RouteNavigator getInstance() {
    if (instance == null) {
      instance = new RouteNavigator();
    }
    return instance;
  }

  private final Minecraft mc = Minecraft.getMinecraft();

  @Override
  public String getName() {
    return "RouteNavigator";
  }

  @Override
  public boolean isRunning() {
    return this.enabled;
  }

  @Override
  public boolean shouldPauseMacroExecution() {
    return true;
  }

  @Override
  public boolean shouldStartAtLaunch() {
    return false;
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
    if (!this.enabled) {
      return;
    }

    this.enabled = false;
    send("RouteNavigator Stopped");
  }

  @Override
  public void resetStatesAfterStop() {
    this.state = State.STARTING;
    RotationHandler.getInstance().reset();
    PathfindUtil.stop();
  }

  @Override
  public boolean shouldCheckForFailsafe() {
    return true;
  }

  private final Clock timer = new Clock();
  private boolean enabled = false;
  private Route routeToFollow;
  private int currentRouteIndex = -1;
  private int targetRouteIndex = -1;
  private State state = State.STARTING;
  private boolean isQueued = false;
  private NavError navError = NavError.NONE;

  public void queueRoute(final Route routeToFollow) {
    if (this.enabled) {
      return;
    }
    this.routeToFollow = routeToFollow;
    this.currentRouteIndex = -1;
    this.targetRouteIndex = -1;
    this.isQueued = true;
  }

  public void goTo(final int index) {
    if (this.routeToFollow == null || this.routeToFollow.isEmpty()) {
      error("No Route Was Selected or its empty.");
      return;
    }
    this.targetRouteIndex = index;
    this.currentRouteIndex = this.getCurrentIndex(PlayerUtil.getBlockStandingOn()) - 1;
    this.normalizeIndices();
    this.navError = NavError.NONE;
    this.enabled = true;
  }

  public void gotoNext() {
    this.goTo(this.targetRouteIndex + 1);
  }

  public void enable(final Route routeToFollow) {
    this.routeToFollow = routeToFollow;
    this.enabled = true;
    this.targetRouteIndex = -1;
    this.normalizeIndices();
    this.currentRouteIndex = -1;
    this.navError = NavError.NONE;

    send("Enabling AutoAotv.");
  }

  public void pause() {
    this.enabled = false;
    this.timer.reset();
    this.resetStatesAfterStop();

    send("Pausing AutoAotv");
  }

  public void disable() {
    this.enabled = false;
    this.isQueued = false;
    this.routeToFollow = null;
    this.timer.reset();
    this.targetRouteIndex = -1;
    this.currentRouteIndex = -1;
    this.resetStatesAfterStop();

    send("Disabling Aotv");
  }

  public void disable(NavError error) {
    this.navError = error;
    this.disable();
  }

  private void normalizeIndices() {
    this.targetRouteIndex = this.normalizeIndex(this.targetRouteIndex);
    this.currentRouteIndex = this.normalizeIndex(this.currentRouteIndex);
    if (this.targetRouteIndex < this.currentRouteIndex) {
      this.targetRouteIndex += this.routeToFollow.size();
    }
  }

  private int normalizeIndex(final int index) {
    return (index + this.routeToFollow.size()) % this.routeToFollow.size();
  }

  // Needs more work (trust me)
  private int getLookTime(final RouteWaypoint waypoint) {
    if (waypoint.getTransportMethod().ordinal() == 0) {
      return MightyMinerConfig.delayAutoAotvLookDelay;
    }
    return MightyMinerConfig.delayAutoAotvEtherwarpLookDelay;
  }

  // Todo: Add Check to see if player can go to next block of the closestBlock
  public int getCurrentIndex(final BlockPos playerBlock) {
    int index = this.routeToFollow.indexOf(new RouteWaypoint(playerBlock, TransportMethod.ETHERWARP));
    if (index != -1) {
      return index;
    }
    return this.routeToFollow.getClosest(playerBlock).map(routeWaypoint -> this.routeToFollow.indexOf(routeWaypoint)).orElse(-1);
  }

  public boolean succeeded() {
    return !this.enabled && this.navError == NavError.NONE;
  }

  public NavError getNavError() {
    return this.navError;
  }

  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent event) {
    if (!this.enabled || mc.thePlayer == null || mc.theWorld == null) {
      return;
    }

    switch (this.state) {
      case STARTING: {
        this.swapState(State.DETECT_ROUTE, 0);
        break;
      }
      case DETECT_ROUTE: {
        if (this.currentRouteIndex++ == this.targetRouteIndex) {
          this.swapState(State.END_VERIFY, 0);
          return;
        }
        if (this.routeToFollow.get(this.currentRouteIndex).getTransportMethod() == TransportMethod.WALK) {
          this.swapState(State.WALK, 0);
        } else {
          this.swapState(State.ROTATION, 0);
        }

        log("Going To Index: " + this.currentRouteIndex);
        break;
      }
      case ROTATION: {
        // Todo: improve the time thing
        RouteWaypoint nextPoint = this.routeToFollow.get(this.currentRouteIndex);

        RotationHandler.getInstance().easeTo(
            new RotationConfiguration(new Target(nextPoint.toVec3()),
                this.getLookTime(nextPoint),
                null)
                .followTarget(true));
        this.swapState(State.ROTATION_VERIFY, 2000);
        log("Rotating");
        break;
      }
      case ROTATION_VERIFY: {
        if (this.timer.isScheduled() && this.timer.passed()) {
          error("Could not look in time. Disabling.");
          this.disable(NavError.TIME_FAIL);
          return;
        }
        RouteWaypoint target = this.routeToFollow.get(this.currentRouteIndex);

        // Todo: add a better method to verify if rotation was completed or not
        if (!AngleUtil.isLookingAt(target.toVec3(), 2f)) {
          return;
        }
        int sneakTime = 0;
        if (target.getTransportMethod() == TransportMethod.ETHERWARP) {
          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, true);
          sneakTime = 250;
        }
        this.swapState(State.AOTV, sneakTime);
        break;
      }
      case AOTV: {
        if (this.timer.isScheduled() && !this.timer.passed()) {
          return;
        }
        // Todo: test Etherwarp
        KeyBindUtil.rightClick();
        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, false);
        RotationHandler.getInstance().reset();
        this.swapState(State.AOTV_VERIFY, 2000);
        break;
      }
      case AOTV_VERIFY: {
        if (this.timer.isScheduled() && this.timer.passed()) {
          error("Did not receive teleport packet in time. Disabling");
          this.disable(NavError.TIME_FAIL);
          return;
        }
        break;
      }
      case WALK:
        BlockPos source = this.routeToFollow.get(this.currentRouteIndex).toBlockPos();
        if (this.currentRouteIndex == 0) {
          log("queued first");
          BlockPos p = PlayerUtil.getBlockStandingOn();
          Pathfinder.getInstance().queue(p, source);
        }
        if (this.currentRouteIndex + 1 <= this.targetRouteIndex) {
          log("queued next");
          RouteWaypoint target = this.routeToFollow.get(this.currentRouteIndex + 1);
          Pathfinder.getInstance().queue(source, target.toBlockPos());
        }
        if (!Pathfinder.getInstance().isEnabled()) {
          log("Started");
          Pathfinder.getInstance().start();
        }
        this.swapState(State.WALK_VERIFY, 2000);
        log("Walking");
        break;
      case WALK_VERIFY:
        BlockPos targetPos = this.routeToFollow.get(this.currentRouteIndex).toBlockPos();
        if (Pathfinder.getInstance().completedPathTo(targetPos) || PlayerUtil.getBlockStandingOn().equals(targetPos)) {
          log("Completed path. going to next");
//          PathfindUtil.stop();
          this.swapState(State.STARTING, 0);
          log("Done Walking");
          return;
        }

        if (Pathfinder.getInstance().failed()) {
          error("Pathfinding failed");
          this.disable(NavError.PATHFIND_FAILED);
          return;
        }

        break;
      case END_VERIFY: {
        // Todo: add something to verify if player is at the end or not preferably something that checks for distance from end

        if (this.isQueued) {
          this.pause();
        } else {
          this.disable();
        }
        break;
      }
    }
  }

  @SubscribeEvent(receiveCanceled = true)
  public void onPacketReceive(PacketEvent.Received event) {
    if (!this.enabled || this.state != State.AOTV_VERIFY) {
      return;
    }
    if (!(event.packet instanceof S08PacketPlayerPosLook)) {
      return;
    }

    this.swapState(State.STARTING, 0);
    S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook) event.packet;

    Vec3 pos = new Vec3(packet.getX(), packet.getY(), packet.getZ());
    if (pos.distanceTo(this.routeToFollow.get(this.currentRouteIndex).toVec3()) > 6) {
      this.swapState(State.ROTATION, 0);
    }
  }

  @SubscribeEvent
  public void onRender(RenderWorldLastEvent event) {
    if (!this.isQueued) {
      return;
    }

    this.routeToFollow.drawRoute();
  }

  public void swapState(final State toState, final int delay) {
    this.state = toState;
    this.timer.schedule(delay);
  }

  enum State {
    STARTING, DETECT_ROUTE, ROTATION, ROTATION_VERIFY, AOTV, AOTV_VERIFY, WALK, WALK_VERIFY, END_VERIFY
  }

  public enum NavError {
    NONE, TIME_FAIL, PATHFIND_FAILED
  }
}
