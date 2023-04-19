package com.jelly.MightyMiner.config.aotv;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

public class Root {

    @Expose
    @Getter
    @Setter
    JsonObject routes;

    @Expose
    @Getter
    @Setter
    String selectedRoute;
}
