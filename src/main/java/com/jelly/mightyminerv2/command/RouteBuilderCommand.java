package com.jelly.mightyminerv2.command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.RouteBuilder;
import com.jelly.mightyminerv2.handler.RouteHandler;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.route.WaypointType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

@Command(value = "rb", aliases = {"route", "routebuilder", "builder"})
public class RouteBuilderCommand {

    @Main
    public void main() {
        if (isRouteBuilderNotRunning()) return;

        Logger.sendMessage("Use these commands to manage your routes.");
        info("   1. /rb list -> List all available routes.");
        info("   2. /rb select <route-name> -> Select the specified route name. A new route will be created if none exist.");
        info("   3. /rb add <walk|etherwarp|mine> -> Add the block player is standing on to selected route.");
        info("   4. /rb remove <index> -> Remove the block player is standing on from selected route.");
        info("   5. /rb replace <index> <walk|etherwarp|mine> -> Replaces Specified Index from the route with block player is standing on.");
        info("   6. /rb delete <route-name> -> Deletes the route.");
    }

    @SubCommand
    public void list() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available Routes: ");

        RouteHandler.getInstance().getRoutes().forEach((key, val) -> {
            String str = key;
            if (RouteHandler.getInstance().getSelectedRoute().equals(val)) str += "*";
            sb.append(str).append(", ");
        });

        Logger.sendMessage(sb.toString());
    }

    @SubCommand
    public void reload() {
        RouteHandler.getInstance().loadData();
        Logger.sendMessage("Refreshed routes file.");
    }

    @SubCommand
    public void select(final String routeName) {
        MightyMinerConfig.selectedRoute = routeName;
        RouteHandler.getInstance().selectRoute(routeName);
        Logger.sendMessage("Selected route: " + routeName);
    }

    @SubCommand
    public void add(final String name) {
        if (isRouteBuilderNotRunning()) return;
        WaypointType type = WaypointType.ETHERWARP;

        if (name.equalsIgnoreCase("walk")) {
            type = WaypointType.WALK;
        } else if (name.equalsIgnoreCase("mine")) {
            type = WaypointType.MINE;
        } else if (!name.equalsIgnoreCase("etherwarp")) {
            Logger.sendError("You must specify a proper option. Run /rb for more information.");
            return;
        }

        RouteBuilder.getInstance().addToRoute(type);
    }

    @SubCommand
    public void remove(int index) {
        if (isRouteBuilderNotRunning()) return;
        RouteBuilder.getInstance().removeFromRoute(index - 1);
        Logger.sendMessage("Removed point at index: " + index);
    }

    @SubCommand
    public void delete(final String routeName) {
        if (isRouteBuilderNotRunning()) return;
        RouteHandler.getInstance().deleteRoute(routeName);
        Logger.sendMessage("Deleted Route: " + routeName);
    }

    @SubCommand
    public void replace(final int indexToReplace, final String name) {
        if (isRouteBuilderNotRunning()) return;
        if (indexToReplace <= 0) return;
        WaypointType type = WaypointType.ETHERWARP;

        if (name.equalsIgnoreCase("walk")) {
            type = WaypointType.WALK;
        } else if (name.equalsIgnoreCase("mine")) {
            type = WaypointType.MINE;
        } else if (!name.equalsIgnoreCase("etherwarp")) {
            Logger.sendError("You must specify a proper option. Run /rb for more information.");
            return;
        }

        RouteBuilder.getInstance().replaceNode(indexToReplace - 1);
        Logger.sendMessage("Replaced index " + indexToReplace + " with " + type.name().charAt(0) + type.name().substring(1).toLowerCase());
    }

    private boolean isRouteBuilderNotRunning() {
        if (!RouteBuilder.getInstance().isRunning()) {
            Logger.sendError("Route Builder is not enabled! Enable it by pressing the keybind configured in config.");
            return true;
        }

        return false;
    }

    private void info(final String message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§e" + message));
    }

}
