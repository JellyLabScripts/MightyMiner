package com.jelly.MightyMinerV2.Command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.MightyMinerV2.Feature.impl.AutoAotv;
import com.jelly.MightyMinerV2.Handler.RouteHandler;
import com.jelly.MightyMinerV2.Util.LogUtil;
import com.jelly.MightyMinerV2.Util.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

@Command(value = "set")
public class OsamaTestCommandNobodyTouchPleaseLoveYou {
    private static OsamaTestCommandNobodyTouchPleaseLoveYou instance;

    public static OsamaTestCommandNobodyTouchPleaseLoveYou getInstance() {
        if (instance == null) instance = new OsamaTestCommandNobodyTouchPleaseLoveYou();
        return instance;
    }

    Entity entTodraw = null;

    @Main
    public void main() {

    }

    @SubCommand
    public void aotv() {
        if (RouteHandler.getInstance().getSelectedRoute().isEmpty()) {
            LogUtil.send("Selected Route is empty.", LogUtil.ELogType.SUCCESS);
            return;
        }
        AutoAotv.getInstance().enable(RouteHandler.getInstance().getSelectedRoute());
    }


    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (entTodraw != null) {
            RenderUtil.drawBox(((EntityLivingBase) entTodraw).getEntityBoundingBox(), Color.CYAN);
        }
    }
}
