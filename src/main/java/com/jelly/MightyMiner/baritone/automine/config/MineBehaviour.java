package com.jelly.MightyMiner.baritone.automine.config;

public class MineBehaviour {
    boolean shiftWhenMine;
    int rotationTime;

    public MineBehaviour(boolean shiftWhenMine, int rotationTime) {
        this.shiftWhenMine = shiftWhenMine;
        this.rotationTime = rotationTime;
    }

    public boolean isShiftWhenMine() {
        return shiftWhenMine;
    }

    public int getRotationTime() {
        return rotationTime;
    }
}
