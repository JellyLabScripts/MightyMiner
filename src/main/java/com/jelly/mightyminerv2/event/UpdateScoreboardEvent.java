package com.jelly.mightyminerv2.event;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

public class UpdateScoreboardEvent extends Event {
    public final List<String> scoreboard;
    public final long timestamp;

    public UpdateScoreboardEvent(List<String> scoreboard, long timestamp) {
        this.scoreboard = scoreboard;
        this.timestamp = timestamp;
    }
}
