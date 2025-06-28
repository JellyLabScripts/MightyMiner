package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import lombok.Getter;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameMentionFailsafe extends AbstractFailsafe {

    @Getter
    private static final NameMentionFailsafe instance = new NameMentionFailsafe();

    private static final Pattern SENDER_NAME_PATTERN = Pattern.compile("^.*?(?<senderName>[a-zA-Z0-9_]+)§?f?:");

    @Override
    public int getPriority() { return 10; }

    @Override
    public String getName() { return "NameMentionFailsafe"; }

    @Override
    public Failsafe getFailsafeType() { return Failsafe.NAME_MENTION; }

    @Override
    public boolean onChat(ClientChatReceivedEvent event) {
        String playerName = mc.thePlayer.getName();
        String unformattedMessage = event.message.getUnformattedText();

        String senderName = null;
        Matcher matcher = SENDER_NAME_PATTERN.matcher(unformattedMessage);
        if (matcher.find()) { senderName = matcher.group("senderName"); }

        if (senderName != null && senderName.equalsIgnoreCase(playerName)) { return false; }

        if (unformattedMessage.toLowerCase().contains(playerName.toLowerCase() + " invited ")) { return false; }

        //if (unformattedMessage.toLowerCase().contains("has invited you to join their party!")) { notify() }

        Pattern mentionPattern = Pattern.compile("\\b" + Pattern.quote(playerName) + "\\b", Pattern.CASE_INSENSITIVE);
        return mentionPattern.matcher(unformattedMessage).find();
    }

    @Override
    public boolean react() {
        MacroManager.getInstance().disable();
        warn("Your name was mentioned in chat. Macro disabled.");
        return true;
    }
}