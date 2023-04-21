package com.jelly.MightyMiner.baritone.automine.structures;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.BlockPos;

public class BlockNode {

    @Setter @Getter
    BlockPos pos;
    @Getter @Setter
    BlockType type;

    @Getter
    boolean isFullPath; // only use in last blockNode of path

    public BlockNode(BlockPos pos, BlockType type) {
        this.pos = pos;
        this.type = type;
    }

    public BlockNode(boolean isFullPath) {
        this.isFullPath = isFullPath;
    }


}
