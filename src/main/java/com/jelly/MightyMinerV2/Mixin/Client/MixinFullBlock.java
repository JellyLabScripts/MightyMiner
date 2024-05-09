package com.jelly.MightyMinerV2.Mixin.Client;

import com.jelly.MightyMinerV2.Config.MightyMinerConfig;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({BlockStainedGlassPane.class})
public abstract class MixinFullBlock extends BlockPane {

    protected MixinFullBlock(Material materialIn, boolean canDrop) {
        super(materialIn, canDrop);
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        if (MightyMinerConfig.fullblock)
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        else
            super.setBlockBoundsBasedOnState(worldIn, pos);
    }
}