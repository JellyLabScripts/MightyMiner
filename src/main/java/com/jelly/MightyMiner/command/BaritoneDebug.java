package com.jelly.MightyMiner.command;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.calculations.AStarPathFinder;
import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathFinderBehaviour;
import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathMode;
import com.jelly.MightyMiner.baritone.automine.calculations.exceptions.NoBlockException;
import com.jelly.MightyMiner.baritone.automine.calculations.exceptions.NoPathException;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.baritone.automine.config.PathFindSetting;
import com.jelly.MightyMiner.baritone.automine.config.WalkBaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.baritone.automine.movementgrapth.debug.Visualiser;
import com.jelly.MightyMiner.baritone.automine.movementgrapth.graph.GraphCreator;
import com.jelly.MightyMiner.baritone.automine.movementgrapth.graph.node.GraphNode;
import com.jelly.MightyMiner.baritone.automine.structures.Path;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;

import java.util.Arrays;

import static com.jelly.MightyMiner.MightyMiner.config;
import static com.jelly.MightyMiner.MightyMiner.coordsConfig;

public class BaritoneDebug extends CommandBase {

    private final String CLEAN = "" + EnumChatFormatting.RESET + EnumChatFormatting.AQUA + EnumChatFormatting.ITALIC;
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "baritone";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Bill gates";
    }

    public void displayUsage(){
        Minecraft mc = Minecraft.getMinecraft();
        mc.thePlayer.addChatMessage(new ChatComponentText((""+ EnumChatFormatting.DARK_AQUA + EnumChatFormatting.BOLD + "---------------------------------------------")));
        LogUtils.addMessage("Usage: " + CLEAN + "/baritone < goto | dgbgraph >");
        LogUtils.addMessage("Start going somewhere using baritone " + CLEAN + "/baritone goto 0 5 0");
        mc.thePlayer.addChatMessage(new ChatComponentText((""+EnumChatFormatting.DARK_AQUA + EnumChatFormatting.BOLD + "---------------------------------------------")));
    }


    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            displayUsage();
            return;
        }

        String name = args[0];

        if (args.length == 1 && args[0].matches("goto")) {
            LogUtils.addMessage("Usage: " + CLEAN + "/baritone" + name + " <goto-name>");
            return;
        }

        switch (name) {
            case "goto":
                if(args.length < 4){
                    LogUtils.addMessage("Usage: " + CLEAN + "/baritone goto x y z");
                    return;
                }

                try {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);
                    int z = Integer.parseInt(args[3]);

                    AutoMineBaritone autoMineBaritone = new AutoMineBaritone(new WalkBaritoneConfig(0, 256, 5));

                    autoMineBaritone.goTo(new BlockPos(x, y, z));
                }catch (NumberFormatException e){
                    LogUtils.addMessage("Usage: " + CLEAN + "/baritone goto x y z");
                    return;
                }

                break;
            case "dgbgraph":
                if(args.length < 4){
                    LogUtils.addMessage("Usage: " + CLEAN + "/baritone goto x y z");
                    return;
                }

                try {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);
                    int z = Integer.parseInt(args[3]);
                    BlockPos target = new BlockPos(x, y, z);


                    BaritoneConfig cfg = new WalkBaritoneConfig(0, 256, 5);

                    MightyMiner.pathfindPool.submit(() -> {

                        Path path;
                        BlockPos playerFloorPos;
                        AStarPathFinder pathFinder;
                        pathFinder = new AStarPathFinder(new PathFinderBehaviour(
                                cfg.getForbiddenPathfindingBlocks() == null ? null : cfg.getForbiddenPathfindingBlocks(),
                                cfg.getAllowedPathfindingBlocks() == null ? null : cfg.getAllowedPathfindingBlocks(),
                                cfg.getMaxY(),
                                cfg.getMinY(),
                                cfg.getMineType() == MiningType.DYNAMIC ? 30 : 4,
                                cfg.getMineType() == MiningType.STATIC
                        ));

                        if (!cfg.isMineFloor()) {
                            playerFloorPos = BlockUtils.getPlayerLoc().down();
                            pathFinder.addToBlackList(playerFloorPos);
                        }

                        try {

                            path = pathFinder.getPath(PathMode.GOTO, target);

                            System.out.println("Starting GraphCreator");

                            GraphCreator graphCreator = new GraphCreator();

                            GraphNode graph = graphCreator.createGraph(path);

                            Visualiser.INSTANCE.update(graph);

                        } catch (NoPathException e) {
                            Logger.playerLog("Pathfind failed: " + e);
                        }
                    });

                } catch (NumberFormatException e){
                    LogUtils.addMessage("Usage: " + CLEAN + "/baritone dgbgraph x y z");
                }
                break;

            default:
                displayUsage();

        }

    }
}
