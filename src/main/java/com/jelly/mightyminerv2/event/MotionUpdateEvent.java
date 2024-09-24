package com.jelly.mightyminerv2.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class MotionUpdateEvent extends Event {
    public float yaw;
    public float pitch;

    public MotionUpdateEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

//    public static class Pre extends MotionUpdateEvent {
//        public Pre(final float yaw, final float pitch) {
//            super(yaw, pitch);
//        }
//    }

//    public static class Post extends MotionUpdateEvent {
//        public Post(final float yaw, final float pitch) {
//            super(yaw, pitch);
//        }
//    }
}
