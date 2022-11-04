package com.jelly.MightyMiner.macros.macros;

import com.jelly.MightyMiner.macros.Macro;

public class CommissionMacro extends Macro {
    //AutoMineBaritone mineBaritone;

    public enum Quest {
        ROYAL_MINES,

    }
    @Override
    protected void onEnable() {
       // mineBaritone.mineFor(new BlockPos(5, 1, -9));
       // LogUtils.addMessage(BlockUtils.getRelativeBlock(0, 0, 1).toString());
    }

   /* @Override
    public void onTick(TickEvent.Phase phase) {
        mineBaritone.onTickEvent(phase);
    }

    @Override
    public void onOverlayRenderEvent(RenderGameOverlayEvent event) {
        mineBaritone.onOverlayRenderEvent(event);
    }

    @Override
    public void onLastRender(RenderWorldLastEvent event) {
        mineBaritone.onRenderEvent();
    }*/

    @Override
    protected void onDisable() {
       // mineBaritone.disableBaritone();

    }
    /*
    private MineBehaviour getMineBehaviour(){
        return new MineBehaviour(
                AutoMineType.DYNAMIC,
                false,
                true,
                false,
                50,
                8,
                new ArrayList<Block>(){
                    {
                        add(Blocks.chest);
                        add(Blocks.trapped_chest);
                    }
                },
                null,
                256,
                0
        );
    }*/
}
