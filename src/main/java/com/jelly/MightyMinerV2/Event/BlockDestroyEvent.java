package com.jelly.MightyMinerV2.Event;

import lombok.Getter;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;

@Getter
public class BlockDestroyEvent extends Event {

  private final BlockPos block;
  private final float progress;

  public BlockDestroyEvent(final BlockPos block, final float progress) {
    this.block = block;
    this.progress = progress;
  }

}
