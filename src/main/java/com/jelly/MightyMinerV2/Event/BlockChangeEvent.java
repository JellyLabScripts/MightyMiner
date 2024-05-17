package com.jelly.MightyMinerV2.Event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jetbrains.annotations.NotNull;

public class BlockChangeEvent extends Event {
    public BlockPos pos;
    public IBlockState old;
    public IBlockState update;

    public BlockChangeEvent(BlockPos pos, IBlockState old, IBlockState update) {
        this.pos = pos;
        this.old = old;
        this.update = update;
    }
}
