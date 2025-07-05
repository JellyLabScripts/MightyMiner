package com.jelly.mightyminerv2.feature;

import com.jelly.mightyminerv2.event.BlockChangeEvent;
import com.jelly.mightyminerv2.event.BlockDestroyEvent;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.event.UpdateTablistEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe.Failsafe;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.Clock;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFeature {

    protected final Minecraft mc = Minecraft.getMinecraft();
    protected final Clock timer = new Clock();
    @Getter
    protected List<Failsafe> failsafesToIgnore;
    protected boolean enabled = false;

    public AbstractFeature() {
        this.failsafesToIgnore = new ArrayList<>();
    }

    public abstract String getName();

    /**
     * Returns whether the feature is currently running.
     * This is determined by the internal 'enabled' flag,
     * which is toggled through start, stop, pause, and resume logic.
     *
     * IMPORTANT:This is different from isEnabled()
     *
     * @return true if the feature is active and running
     */
    public boolean isRunning() {
        return this.enabled;
    }

    /**
     * Indicates whether the feature is marked as enabled by config or default logic.
     * IMPORTANT: This is independent of whether it is currently running.
     *
     * @return true if the feature is considered enabled
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     * Starts the feature. Should be overridden by subclasses
     * to initialize or enable feature-specific logic.
     * NOTE: This does NOT automatically set 'enabled' to true.
     */
    public void start() {
    }

    /**
     * Stops the feature and resets internal state.
     * This also disables the feature by setting 'enabled' to false.
     */
    public void stop() {
        this.enabled = false;
        this.resetStatesAfterStop();
    }

    /**
     * Temporarily disables the feature without resetting internal state.
     * Can be resumed later with {@link #resume()}.
     */
    // Temporarily disables the feature
    public void pause() {
        this.enabled = false;
    }

    /**
     * Resumes a paused feature by setting 'enabled' to true.
     */
    // Re-enables a previously paused feature
    public void resume() {
        this.enabled = true;
    }

    /**
     * Override this method to clean up or reset custom states when the feature stops.
     */
    // Cleanup hook for subclasses
    public void resetStatesAfterStop() {
    }

    /**
     * Determines whether this feature should auto-start on application launch.
     *
     * @return true if the feature should start automatically
     */
    public boolean shouldStartAtLaunch() {
        return false;
    }

    /**
     * Indicates if failsafe checks should be skipped for this feature.
     *
     * @return true to bypass failsafe logic
     */
    public boolean shouldNotCheckForFailsafe() {
        return false;
    }

    /**
     * Checks whether the internal timer is currently running
     * and has not yet completed its duration.
     *
     * @return true if the timer is scheduled and still in progress
     */
    protected boolean isTimerRunning() {
        return this.timer.isScheduled() && !this.timer.passed();
    }

    /**
     * Checks whether the internal timer is scheduled and has completed.
     *
     * @return true if the timer is scheduled and has elapsed
     */
    protected boolean hasTimerEnded() {
        return this.timer.isScheduled() && this.timer.passed();
    }

    protected void onTick(ClientTickEvent event) {
    }

    protected void onRender(RenderWorldLastEvent event) {
    }

    protected void onChat(String message) {
    }

    protected void onTablistUpdate(UpdateTablistEvent event) {
    }

    protected void onOverlayRender(RenderGameOverlayEvent event) {
    }

    protected void onPacketReceive(PacketEvent.Received event) {
    }

    protected void onWorldLoad(WorldEvent.Load event) {

    }

    protected void onWorldUnload(WorldEvent.Unload event) {

    }

    protected void onBlockChange(BlockChangeEvent event) {
    }

    protected void onBlockDestroy(BlockDestroyEvent event) {
    }

    protected void onKeyEvent(InputEvent.KeyInputEvent event) {
    }

    protected void log(String message) {
        Logger.sendLog(formatMessage(message));
    }

    protected void send(String message) {
        Logger.sendMessage(formatMessage(message));
    }

    protected void logError(String message) {
        Logger.sendLog(formatMessage("Error: " + message));
    }

    protected void sendError(String message) {
        Logger.sendError(formatMessage(message));
    }

    protected void warn(String message) {
        Logger.sendWarning(formatMessage(message));
    }

    protected void note(String message) {
        Logger.sendNote(formatMessage(message));
    }

    protected String formatMessage(String message) {
        return "[" + getName() + "] " + message;
    }
}
