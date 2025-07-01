package com.jelly.mightyminerv2.command;

import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.Logger;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommandHandler {

    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        String msg = event.message.trim();

        if (!msg.startsWith("/mm")) return;

        event.setCanceled(true); // stop from reaching server

        String[] args = msg.split(" ");

        if (args.length < 2) {
            Logger.sendMessage("Usage: /mm <start|stop|status|config|help>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "start":
                if (MacroManager.getInstance().getCurrentMacro() == null) {
                    Logger.sendMessage("No macro selected! Please select a macro in GUI first.");
                    return;
                }

                MacroManager.getInstance().enable();
                Logger.sendMessage("Started macro: " + MacroManager.getInstance().getCurrentMacro().getName());
                break;

            case "stop":
                MacroManager.getInstance().disable();
                Logger.sendMessage("Stopped macro.");
                break;

            case "status":
                if (MacroManager.getInstance().isEnabled() && MacroManager.getInstance().getCurrentMacro() != null) {
                    Logger.sendMessage("Macro Running: " + MacroManager.getInstance().getCurrentMacro().getName());
                } else {
                    Logger.sendMessage("No macro is running.");
                }
                break;

            case "config":
                Logger.sendMessage("To configure MightyMiner, edit:");
                Logger.sendMessage(".mightyminer/config.json");
                break;

            case "help":
                Logger.sendMessage("/mm start §7- Start selected macro");
                Logger.sendMessage("/mm stop §7- Stop macro");
                Logger.sendMessage("/mm status §7- Show current macro");
                Logger.sendMessage("/mm config §7- Show config file location");
                break;

            default:
                Logger.sendMessage("Unknown /mm command.");
                break;
        }
    }
}
