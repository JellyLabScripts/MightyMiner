package com.jelly.MightyMinerV2.Event;

import lombok.Getter;
import net.minecraftforge.fml.common.eventhandler.Event;

@Getter
public class UpdateScoreboardLineEvent extends Event {
    private final String line;

    public UpdateScoreboardLineEvent(String line) {
        this.line = line;
    }

}
