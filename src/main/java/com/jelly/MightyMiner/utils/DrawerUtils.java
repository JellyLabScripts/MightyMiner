package com.jelly.MightyMiner.utils;

import com.jelly.MightyMiner.macros.macros.CommissionMacro;
import com.jelly.MightyMiner.macros.macros.MithrilMacro;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;

public class DrawerUtils {
    @SubscribeEvent
    public void onRenderWorldLastPlayerESP(RenderWorldLastEvent event) {
        /*
        if (MithrilMacro.path != null) {
            DrawUtils.drawMiniBlockBox(MithrilMacro.path.getLeft(), Color.RED, 1.0f);
            DrawUtils.drawMiniBlockBox(MithrilMacro.path.getMiddle(), Color.GREEN, 1.0f);
            DrawUtils.drawMiniBlockBox(MithrilMacro.path.getRight(), Color.YELLOW, 1.0f);
            DrawUtils.drawLine(event, MithrilMacro.path.getLeft(), MithrilMacro.path.getMiddle(), 1.0f, Color.BLUE);
            DrawUtils.drawLine(event, MithrilMacro.path.getMiddle(), MithrilMacro.path.getRight(), 1.0f, Color.BLUE);
        }

         */
    }

}
