package com.jelly.MightyMinerV2.Mixin.Render;

import com.jelly.MightyMinerV2.Handler.RotationHandler;
import com.jelly.MightyMinerV2.Mixin.Client.EntityPlayerSPAccessor;
import com.jelly.MightyMinerV2.Mixin.Client.MinecraftAccessor;
import com.jelly.MightyMinerV2.Util.AngleUtil;
import com.jelly.MightyMinerV2.Util.helper.RotationConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModelBiped.class, priority = Integer.MAX_VALUE)
public class MixinModelBiped {
    @Unique
    private final Minecraft mightyMinerV2$mc = Minecraft.getMinecraft();
    @Shadow
    public ModelRenderer bipedHead;

    @Inject(method = {"setRotationAngles"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBiped;swingProgress:F")})
    public void onSetRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn, CallbackInfo ci) {
        if (!RotationHandler.getInstance().isEnabled() || RotationHandler.getInstance().getConfiguration() != null && RotationHandler.getInstance().getConfiguration().rotationType() != RotationConfiguration.RotationType.SERVER
            || entityIn == null || !entityIn.equals(mightyMinerV2$mc.thePlayer) )
            return;

        this.bipedHead.rotateAngleX = ((EntityPlayerSPAccessor) entityIn).getLastReportedPitch() / 57.295776f;
        float partialTicks = ((MinecraftAccessor) mightyMinerV2$mc).getTimer().renderPartialTicks;
        float yawOffset = mightyMinerV2$mc.thePlayer.renderYawOffset + AngleUtil.normalizeAngle(mightyMinerV2$mc.thePlayer.renderYawOffset - mightyMinerV2$mc.thePlayer.prevRenderYawOffset) * partialTicks;
        float calcNetHead = MathHelper.wrapAngleTo180_float(((EntityPlayerSPAccessor) entityIn).getLastReportedYaw() - yawOffset);
        this.bipedHead.rotateAngleY = calcNetHead / 57.295776f;
    }
}
