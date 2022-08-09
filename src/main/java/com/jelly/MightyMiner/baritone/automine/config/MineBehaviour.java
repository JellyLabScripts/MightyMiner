package com.jelly.MightyMiner.baritone.automine.config;

public class MineBehaviour {
    boolean shiftWhenMine;
    int rotationTime;
    int restartTimeThreshold;

    public MineBehaviour(boolean shiftWhenMine, int rotationTime, int restartTimeThreshold) {
        this.shiftWhenMine = shiftWhenMine;
        this.rotationTime = rotationTime;
        this.restartTimeThreshold = restartTimeThreshold;
    }

    public boolean isShiftWhenMine() {
        return shiftWhenMine;
    }

    public int getRotationTime() {
        return rotationTime;
    }

    public int getRestartTimeThreshold(){
        return restartTimeThreshold;
    }
}
