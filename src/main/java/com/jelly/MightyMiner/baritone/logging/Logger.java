package com.jelly.MightyMiner.baritone.logging;

import com.jelly.MightyMiner.MightyMiner;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.LogManager;

public class Logger {
    // POV : You think this is a token logger
    public static void log(String msg){
        LogManager.getLogger(MightyMiner.MODID).info(msg);
    }
    public static void playerLog(String msg){
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[Baritone] : " + msg));
    }
}
