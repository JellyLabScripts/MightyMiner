package com.jelly.MightyMiner.world;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameState {
    private final Minecraft mc = Minecraft.getMinecraft();


    public enum EffectState{
        ON,
        INDETERMINABLE,
        OFF,
    }

    public IChatComponent header;
    public IChatComponent footer;
    public EffectState cookie;
    public EffectState godPot;

    private static final Pattern PATTERN_ACTIVE_EFFECTS = Pattern.compile(
            "\u00a7r\u00a7r\u00a77You have a \u00a7r\u00a7cGod Potion \u00a7r\u00a77active! \u00a7r\u00a7d([0-9]*?:?[0-9]*?:?[0-9]*)\u00a7r");

    public String serverIP;

    public void update() {
        if(mc.getCurrentServerData() != null) {
            if (mc.getCurrentServerData().serverIP != null) {
                serverIP = mc.getCurrentServerData().serverIP;
            }
        }
        checkFooter();

    }


    private void checkFooter() {
        //
        boolean foundGodPot = false;
        boolean foundCookieText = false;
        boolean loaded = false;

        if (footer != null) {
            String formatted = footer.getFormattedText();
            for (String line : formatted.split("\n")) {
                Matcher activeEffectsMatcher = PATTERN_ACTIVE_EFFECTS.matcher(line);
                if (activeEffectsMatcher.matches()) {
                    foundGodPot = true;
                } else if (line.contains("\u00a7d\u00a7lCookie Buff")) {
                    foundCookieText = true;
                } else if (foundCookieText && line.contains("Not active! Obtain")) {
                    foundCookieText = false;
                    cookie = EffectState.OFF;
                } else if (foundCookieText) {
                    foundCookieText = false;
                    cookie = EffectState.ON;;
                }
                if(line.contains("Active")) {
                    loaded = true;
                }
            }
            godPot = foundGodPot ? EffectState.ON : EffectState.OFF;
            if(!loaded){
                godPot = EffectState.INDETERMINABLE;
                cookie = EffectState.INDETERMINABLE;
            }
        }
    }

}

