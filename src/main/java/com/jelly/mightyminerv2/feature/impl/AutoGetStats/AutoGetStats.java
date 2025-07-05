package com.jelly.mightyminerv2.feature.impl.AutoGetStats;

import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.AbstractInventoryTask;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * AutoGetStats handles a simple internal queue of AbstractInventoryTask,
 * running one at a time in FIFO order. Goes onto the next task (if any) when one fails.
 */
public class AutoGetStats extends AbstractFeature {

    private static AutoGetStats instance;

    private final Queue<AbstractInventoryTask<?>> taskQueue = new ArrayDeque<>();
    private AbstractInventoryTask<?> currentTask;

    private final Clock delay = new Clock();

    public static AutoGetStats getInstance() {
        if (instance == null) {
            instance = new AutoGetStats();
        }
        return instance;
    }

    @Override
    public String getName() {
        return "AutoGetStats";
    }

    @SubscribeEvent
    protected void onTick(TickEvent.ClientTickEvent event) {
        if (!enabled || mc.thePlayer == null || mc.theWorld == null || event.phase == TickEvent.Phase.END)
            return;

        if (currentTask != null) {
            currentTask.onTick();

            if (currentTask.getTaskStatus().isFailure() || currentTask.getTaskStatus().isSuccessful()) {
                currentTask.end();
                currentTask = null;
                delay.schedule(1000);  // 1-second delay between each task
            }
        }
        else if (delay.isScheduled() && delay.passed() && !taskQueue.isEmpty()) {
            currentTask = taskQueue.poll();
            currentTask.init();
        }
    }

    /**
     * Adds a new task to the queue and starts it immediately if idle.
     */
    public void startTask(AbstractInventoryTask<?> task) {

        if (task == null) return;
        taskQueue.add(task);

        // If no current task running, start this one immediately
        if (currentTask == null) {
            currentTask = taskQueue.poll();
            currentTask.init();
            this.enabled = true;
            this.start();
        }
    }

    public boolean hasFinishedAllTasks() {
        return currentTask == null && taskQueue.isEmpty();
    }
}

