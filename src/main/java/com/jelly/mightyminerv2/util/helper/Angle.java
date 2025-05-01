package com.jelly.mightyminerv2.util.helper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Angle {
    public float yaw;
    public float pitch;

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

    public float lengthSqrt() {
        return (float) Math.sqrt(this.yaw * this.yaw + this.pitch * this.pitch);
    }

    @Override
    public String toString() {
        return "Rotation{" + "yaw=" + yaw + ", pitch=" + pitch + "}";
    }
}
