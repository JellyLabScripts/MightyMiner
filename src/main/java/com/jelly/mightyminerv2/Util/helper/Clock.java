package com.jelly.mightyminerv2.Util.helper;

import lombok.Getter;

public class Clock {
    private long remainingTime;
    @Getter
    private boolean paused;
    @Getter
    private boolean scheduled;
    @Getter
    private long endTime;
    @Getter
    private long startTime;

    private void start(){
        this.startTime = System.currentTimeMillis();
    }

    public void schedule(long milliseconds) {
        this.endTime = System.currentTimeMillis() + milliseconds;
        this.remainingTime = milliseconds;
        this.scheduled = true;
        this.paused = false;
    }

    public void schedule(double milliseconds) {
        this.endTime = (System.currentTimeMillis() + (long) milliseconds);
        this.remainingTime = (long) milliseconds;
        this.scheduled = true;
        this.paused = false;
    }

    public long getRemainingTime() {
        if (paused) {
            return remainingTime;
        }
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    private long getTimePassed(){
        return System.currentTimeMillis() - startTime;
    }

    public boolean passed() {
        return System.currentTimeMillis() >= endTime;
    }

    public void pause() {
        if (scheduled && !paused) {
            remainingTime = endTime - System.currentTimeMillis();
            paused = true;
        }
    }

    public void resume() {
        if (scheduled && paused) {
            endTime = System.currentTimeMillis() + remainingTime;
            paused = false;
        }
    }

    public void reset() {
        scheduled = false;
        paused = false;
        endTime = 0;
        remainingTime = 0;
    }
}
