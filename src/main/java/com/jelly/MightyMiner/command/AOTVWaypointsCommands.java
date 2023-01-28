package com.jelly.MightyMiner.command;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.gui.AOTVWaypointsGUI;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class AOTVWaypointsCommands implements ICommand {

    private final KeyBinding keyBinding = new KeyBinding("Open Waypoints Settings", Keyboard.KEY_NEXT, "MightyMiner");
    private final KeyBinding keyBinding2 = new KeyBinding("Add current position to selected waypoint list", Keyboard.KEY_EQUALS, "MightyMiner");
    private final KeyBinding keyBinding3 = new KeyBinding("Delete current position from selected waypoint list", Keyboard.KEY_MINUS, "MightyMiner");

    public AOTVWaypointsCommands() {
        ClientRegistry.registerKeyBinding(keyBinding);
        ClientRegistry.registerKeyBinding(keyBinding2);
        ClientRegistry.registerKeyBinding(keyBinding3);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (keyBinding.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new AOTVWaypointsGUI());
        }
        if (keyBinding2.isPressed()) {
            if (MightyMiner.aotvWaypoints.getSelectedRoute() == null) return;
            boolean added = MightyMiner.aotvWaypoints.addCoord(MightyMiner.aotvWaypoints.getSelectedRoute(), new AOTVWaypointsGUI.Waypoint(String.valueOf(MightyMiner.aotvWaypoints.getSelectedRoute().waypoints.size()), BlockUtils.getPlayerLoc().down()));
            if (added)
                LogUtils.addMessage("Added current position (" + BlockUtils.getPlayerLoc().getX() + ", " + BlockUtils.getPlayerLoc().getY() + ", " + BlockUtils.getPlayerLoc().getZ() + ") to selected waypoint list");
            AOTVWaypointsGUI.SaveWaypoints();
        }
        if (keyBinding3.isPressed()) {
            AOTVWaypointsGUI.Waypoint waypointToDelete = null;
            if (MightyMiner.aotvWaypoints.getSelectedRoute() == null) return;
            for (AOTVWaypointsGUI.Waypoint waypoint : MightyMiner.aotvWaypoints.getSelectedRoute().waypoints) {
                if (BlockUtils.getPlayerLoc().down().equals(new BlockPos(waypoint.x, waypoint.y, waypoint.z))) {
                    waypointToDelete = waypoint;
                }
            }
            if (waypointToDelete != null) {
                MightyMiner.aotvWaypoints.removeCoord(MightyMiner.aotvWaypoints.getSelectedRoute(), waypointToDelete);
                LogUtils.addMessage("Removed current position (" + BlockUtils.getPlayerLoc().getX() + ", " + BlockUtils.getPlayerLoc().getY() + ", " + BlockUtils.getPlayerLoc().getZ() + ") from selected waypoint list");
                AOTVWaypointsGUI.SaveWaypoints();
            } else {
                LogUtils.addMessage("AOTV Waypoints - No waypoint found at your current position");
            }
        }
    }

    @Override
    public String getCommandName() {
        return "waypoints";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<String>() {
            {
                add("wp");
            }
        };
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        Minecraft.getMinecraft().displayGuiScreen(new AOTVWaypointsGUI());
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(@NotNull ICommand o) {
        return 0;
    }
}
