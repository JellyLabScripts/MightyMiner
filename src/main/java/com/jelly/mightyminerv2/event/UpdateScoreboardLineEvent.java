package com.jelly.mightyminerv2.event;

import lombok.Getter;
import net.minecraftforge.fml.common.eventhandler.Event;

@Getter
public class UpdateScoreboardLineEvent extends Event {
    private final String dirtyLine;
    private final String cleanLine;

    public UpdateScoreboardLineEvent(final String dirtyLine, final String cleanLine) {
        this.dirtyLine = dirtyLine;
        this.cleanLine = cleanLine;
    }

}
