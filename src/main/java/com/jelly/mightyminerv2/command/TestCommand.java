package com.jelly.mightyminerv2.command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.mightyminerv2.feature.impl.Pathfinder;
import com.jelly.mightyminerv2.util.Logger;
import net.minecraft.util.BlockPos;

/**
 * Used for debugging features.
 */
@Command(value = "mtest")
public class TestCommand {

    @SubCommand
    public void pathfind(int x, int y, int z) {
        Pathfinder.getInstance().stopAndRequeue(new BlockPos(x, y, z));

        if (!Pathfinder.getInstance().isRunning())
            Pathfinder.getInstance().start();

        Logger.sendMessage("Attempting to start pathfinder.");
    }

    @SubCommand
    public void stoppath() {
        Pathfinder.getInstance().stop();
        Logger.sendMessage("Stopping pathfinder.");
    }

}
