package com.jelly.MightyMiner.command;

import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import com.jelly.MightyMiner.baritone.automine.calculations.exceptions.NoPathException;
import com.jelly.MightyMiner.baritone.automine.config.WalkBaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.structures.BlockNode;
import com.jelly.MightyMiner.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.jelly.MightyMiner.MightyMiner.coordsConfig;

public class BaritoneDebug extends CommandBase {

    private final String CLEAN = "" + EnumChatFormatting.RESET + EnumChatFormatting.AQUA + EnumChatFormatting.ITALIC;
    Minecraft mc = Minecraft.getMinecraft();

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
        LogUtils.addMessage("Usage: " + CLEAN + "/baritone < goto >");
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
           /* case "kill":
                AutoMineBaritone autoMineBaritone = new AutoMineBaritone(new WalkBaritoneConfig(0, 256, 5));
                String[] mobNames = new String[]{"Villager"};
                List<Entity> entities = mc.theWorld.loadedEntityList.stream().filter(entity -> entity instanceof EntityArmorStand).filter(entity -> mc.thePlayer.getPositionEyes(1).distanceTo(entity.getPositionVector()) <= 50).collect(Collectors.toList());
                List<Entity> filtered = entities.stream().filter(v -> (!v.getName().contains(mc.thePlayer.getName()) && Arrays.stream(mobNames).anyMatch(mobsName -> {
                    String mobsName1 = StringUtils.stripControlCodes(mobsName);
                    String vName = StringUtils.stripControlCodes(v.getName());
                    String vCustomNameTag = StringUtils.stripControlCodes(v.getCustomNameTag());
                    return vName.toLowerCase().contains(mobsName1.toLowerCase()) || vCustomNameTag.toLowerCase().contains(mobsName1.toLowerCase());
                }))).collect(Collectors.toList());

                List<LinkedList<BlockNode>> paths = new ArrayList<>();
                for(Entity e : filtered){
                    try {
                        paths.add(autoMineBaritone.getGotoPath(new BlockPos(e.posX, e.posY, e.posZ)).getBlocksInPath());
                    }catch (NoPathException ignored){}
                }
                paths.sort("");*/

            default:
                displayUsage();

        }

    }
}
