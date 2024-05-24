package com.jelly.MightyMinerV2.Mixin.Client;

import com.jelly.MightyMinerV2.Command.OsamaTestCommandNobodyTouchPleaseLoveYou;
import com.jelly.MightyMinerV2.Feature.impl.MithrilMiner;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// From Baritone <3
// Todo: Consider Adding a rotation check To disable omnisprint from within
@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {

    @Shadow
    public float cameraPitch;

    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

//     Need this for jump to work properly
//    @Redirect(
//        method = "jump",
//        at = @At(
//            value = "FIELD",
//            target = "net/minecraft/entity/EntityLivingBase.rotationYaw:F"
//        )
//    )
//    private float overrideYaw(EntityLivingBase self) {
//        if (self instanceof EntityPlayerSP) {
//            return 0;
//        }
//        return self.rotationYaw;
//    }

    @Redirect(
        method = "moveEntityWithHeading",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/EntityLivingBase;moveFlying(FFF)V"
        )
    )
    public void moveRelative(EntityLivingBase instance, float s, float f, float fr) {
        if (!MithrilMiner.getInstance().shouldWalk()) {
            this.moveFlying(s, f, fr);
            return;
        }

        final float originalYaw = this.rotationYaw;
        this.rotationYaw = MithrilMiner.getInstance().getWalkDirection();
        this.moveFlying(s, f, fr);

        this.rotationYaw = originalYaw;
    }
}
