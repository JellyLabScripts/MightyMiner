package com.jelly.MightyMinerV2.Event;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

public class UpdateTablistFooterEvent extends Event {
    public final List<String> footer;

    public UpdateTablistFooterEvent(List<String> footer) {
        this.footer = footer;
    }
}
