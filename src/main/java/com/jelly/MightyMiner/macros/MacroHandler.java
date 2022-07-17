package com.jelly.MightyMiner.macros;

import com.jelly.MightyMiner.features.Baritone;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class MacroHandler {
    Minecraft mc  = Minecraft.getMinecraft();
    Baritone baritone = new Baritone();
    BlockPos blockPos;
    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event){
        if(Keyboard.isKeyDown(Keyboard.KEY_F)) {
            baritone.walkTo(blockPos);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_G)) {
            blockPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        }

    }
}
