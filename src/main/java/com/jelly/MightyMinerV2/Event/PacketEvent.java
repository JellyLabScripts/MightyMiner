package com.jelly.MightyMinerV2.Event;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PacketEvent{
    public static class Sent extends Event{
        public Packet<?> packet;
        public Sent(Packet<?> packet){
            this.packet = packet;
        }
    }

    public static class Received extends Event{
        public Packet<?> received;
        public Received(Packet<?> received){
            this.received = received;
        }
    }
}
