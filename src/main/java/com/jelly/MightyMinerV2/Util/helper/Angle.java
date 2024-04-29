package com.jelly.MightyMinerV2.Util.helper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Angle {
    private float yaw;
    private float pitch;

    public Angle(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void setRotation(Angle rotation) {
        this.yaw = rotation.getYaw();
        this.pitch = rotation.getPitch();
    }

    public float getValue() {
        return Math.abs(this.yaw) + Math.abs(this.pitch);
    }

    @Override
    public String toString() {
        return "Rotation{" + "yaw=" + yaw + ", pitch=" + pitch + "}";
    }
}
