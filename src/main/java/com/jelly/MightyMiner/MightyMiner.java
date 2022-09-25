package com.jelly.MightyMiner;

import com.jelly.MightyMiner.baritone.autowalk.movement.InputHandler;
import com.jelly.MightyMiner.config.Config;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.macros.GemstoneMacro;
import com.jelly.MightyMiner.render.BlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import rosegoldaddons.Main;

@Mod(modid = MightyMiner.MODID, version = MightyMiner.VERSION)
public class MightyMiner {
    public static final String MODID = "mightyminer";
    public static final String VERSION = "1.0";

    public static Config config;

    //thx pizza for fixing this
    public static void onStartGame(){
        config = new Config();
    }


    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new MacroHandler());
        MinecraftForge.EVENT_BUS.register(new KeybindHandler());
        MinecraftForge.EVENT_BUS.register(new BlockRenderer());
        MinecraftForge.EVENT_BUS.register(new InputHandler());
        KeybindHandler.initializeCustomKeybindings();
        MacroHandler.initializeMacro();

        Minecraft.getMinecraft().gameSettings.gammaSetting = 100;
    }





}
