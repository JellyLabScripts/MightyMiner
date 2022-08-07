package com.jelly.MightyMiner.mixins;

import com.jelly.MightyMiner.MightyMiner;
import gg.essential.universal.UMinecraft;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(value = FMLHandshakeMessage.ModList.class, remap = false)
public class MixinFMLHandshake {
    @Shadow
    private Map<String, String> modTags;

    @Inject(method = "<init>(Ljava/util/List;)V", at = @At("RETURN"))
    private void hideMODID(List<ModContainer> modList, CallbackInfo ci) {
        if(!Minecraft.getMinecraft().isIntegratedServerRunning()) {
            modTags.remove(MightyMiner.MODID);
        }
    }
}