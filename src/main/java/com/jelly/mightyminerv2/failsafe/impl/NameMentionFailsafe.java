package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class NameMentionFailsafe extends AbstractFailsafe {

    @Getter
    private static final NameMentionFailsafe instance = new NameMentionFailsafe();

    @Override
    public int getPriority() { return 10; }

    @Override
    public String getName() { return "NameMentionFailsafe"; }

    @Override
    public Failsafe getFailsafeType() { return Failsafe.NAME_MENTION; }

    @Override
    public boolean onChat(ClientChatReceivedEvent event) {
        String playerName = mc.thePlayer.getName();
        String message = event.message.getUnformattedText();
        if (message.contains(playerName)) {
            warn("Your name was mentioned in chat:");
            return true;
        }

        return false;
    }

    @Override
    public boolean react() {
        MacroManager.getInstance().disable();
        warn("Your name was mentioned in chat. Macro disabled.");
        return true;
    }
}