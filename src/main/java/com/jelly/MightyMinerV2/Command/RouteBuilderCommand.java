package com.jelly.MightyMinerV2.Command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.MightyMinerV2.Feature.impl.RouteBuilder;
import com.jelly.MightyMinerV2.Handler.RouteHandler;
import com.jelly.MightyMinerV2.Util.LogUtil;
import com.jelly.MightyMinerV2.Util.helper.route.TransportMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

@Command(value = "rb", aliases = {"route", "routebuilder", "builder"})
public class RouteBuilderCommand {
    @Main
    public void main() {
        if (!RouteBuilder.getInstance().isRunning()) {
            LogUtil.send("Enable RouteBuilder First.", LogUtil.ELogType.ERROR);
            return;
        }
        LogUtil.send("Use these commands to manage your routes.", LogUtil.ELogType.SUCCESS);
        success("   1. /rb list -> List all available routes.");
        success("   2. /rb select <route-name> -> Select the specified route name. A new route will be created if none exist.");
        success("   3. /rb add -> Add the block player is standing on to selected route.");
        success("   4. /rb remove -> Remove the block player is standing on from selected route.");
        success("   5. /rb replace <index> -> Replaces Specified Index from the route with block player is standing on.");
        success("   6. /rb clear <route-name> -> Deletes the route.");
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

        LogUtil.send(sb.toString(), LogUtil.ELogType.SUCCESS);
    }

    @SubCommand
    public void select(final String routeName) {
        RouteHandler.getInstance().selectRoute(routeName);
    }

    @SubCommand
    public void add(final String name) {
        if (!RouteBuilder.getInstance().isRunning()) return;
        RouteBuilder.getInstance().addToRoute(name.equalsIgnoreCase("aotv") ? TransportMethod.AOTV : TransportMethod.ETHERWARP);
    }

    @SubCommand
    public void remove() {
        if (!RouteBuilder.getInstance().isRunning()) return;
        RouteBuilder.getInstance().removeFromRoute();
    }

    @SubCommand
    public void clear(final String routeName) {
        if (!RouteBuilder.getInstance().isRunning()) return;
        RouteHandler.getInstance().clearRoute(routeName);
    }

    @SubCommand
    public void replace(final int indexToReplace) {
        if (!RouteBuilder.getInstance().isRunning()) return;
        if (indexToReplace <= 0) return;
        RouteBuilder.getInstance().replaceNode(indexToReplace - 1);
    }

    private void success(final String message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§a" + message));
    }
}
