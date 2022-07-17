package com.jelly.MightyMiner;

import com.jelly.MightyMiner.features.Baritone;
import com.jelly.MightyMiner.macros.MacroHandler;
import com.jelly.MightyMiner.render.BlockRenderer;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = MightyMiner.MODID, version = MightyMiner.VERSION)
public class MightyMiner {
    public static final String MODID = "examplemod";
    public static final String VERSION = "1.0";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        System.out.println("DIRT BLOCK >> "+ Blocks.dirt.getUnlocalizedName());

        MinecraftForge.EVENT_BUS.register(new Baritone());
        MinecraftForge.EVENT_BUS.register(new MacroHandler());
        MinecraftForge.EVENT_BUS.register(new BlockRenderer());
    }



}
