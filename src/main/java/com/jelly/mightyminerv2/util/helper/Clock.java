package com.jelly.mightyminerv2.util.helper;

public class Clock {

    private long deltaTime;
    private boolean paused;
    private boolean scheduled;
    private long endTime;
    private long startTime;

    // stopwatch

    public void schedule(long milliseconds) {
        this.endTime = System.currentTimeMillis() + milliseconds;
        this.deltaTime = milliseconds;
        this.scheduled = true;
        this.paused = false;
    }

    public void schedule(double milliseconds) {
        this.endTime = (System.currentTimeMillis() + (long) milliseconds);
        this.deltaTime = (long) milliseconds;
        this.scheduled = true;
        this.paused = false;
    }

    public boolean passed() {
        return System.currentTimeMillis() >= endTime;
    }

    public void pause() {
        if (scheduled && !paused) {
            deltaTime = endTime - System.currentTimeMillis();
            paused = true;
        }
    }

    public void resume() {
        if (scheduled && paused) {
            endTime = System.currentTimeMillis() + deltaTime;
            paused = false;
        }
    }

    public long getRemainingTime() {
        if (this.paused) {
            return deltaTime;
        }
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    public void start(boolean reset) {
        if (!this.scheduled || reset) {
            this.startTime = System.currentTimeMillis();
        } else {
            this.resumeTimer();
        }
        this.scheduled = true;
    }

    public void stop(boolean reset) {
        if (!this.scheduled || reset) {
            this.reset();
        } else {
            this.pauseTimer();
        }
    }

    public long getTimePassed() {
        if (!this.scheduled || this.paused) {
            return deltaTime;
        }
        return System.currentTimeMillis() - startTime;
    }

    public void pauseTimer() {
        if (scheduled && !paused) {
            deltaTime = System.currentTimeMillis() - startTime;
            paused = true;
        }
    }

    public void resumeTimer() {
        if (scheduled && paused) {
            startTime = System.currentTimeMillis() - deltaTime;
            paused = false;
        }
    }

    public void reset() {
        scheduled = false;
        paused = false;
        endTime = 0;
        deltaTime = 0;
    }

    public boolean isScheduled() {
        return this.scheduled;
    }
}
