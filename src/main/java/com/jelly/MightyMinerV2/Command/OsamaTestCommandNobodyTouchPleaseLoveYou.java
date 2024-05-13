package com.jelly.MightyMinerV2.Command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.MightyMinerV2.Feature.impl.RouteNavigator;
import com.jelly.MightyMinerV2.Handler.GameStateHandler;
import com.jelly.MightyMinerV2.Handler.RouteHandler;
import com.jelly.MightyMinerV2.Util.LogUtil;
import com.jelly.MightyMinerV2.Util.RenderUtil;
import com.jelly.MightyMinerV2.Util.ScoreboardUtil;
import com.jelly.MightyMinerV2.Util.TablistUtil;
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
        final String footer = String.join(" ", TablistUtil.getCachedTablistFooter());
        LogUtil.send(footer, LogUtil.ELogType.SUCCESS);
        LogUtil.send("NotActive: " + footer.contains("Cookie Buff Not active!"), LogUtil.ELogType.SUCCESS);
        LogUtil.send("IsCookieActive: " + GameStateHandler.getInstance().isCookieActive(), LogUtil.ELogType.SUCCESS);
        LogUtil.send("IsGodpotActive: " + GameStateHandler.getInstance().isGodpotActive(), LogUtil.ELogType.SUCCESS);
    }

    @SubCommand
    public void aotv() {
        if (RouteHandler.getInstance().getSelectedRoute().isEmpty()) {
            LogUtil.send("Selected Route is empty.", LogUtil.ELogType.SUCCESS);
            return;
        }
        RouteNavigator.getInstance().queueRoute(RouteHandler.getInstance().getSelectedRoute());
        RouteNavigator.getInstance().goTo(36);
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (entTodraw != null) {
            RenderUtil.drawBox(((EntityLivingBase) entTodraw).getEntityBoundingBox(), Color.CYAN);
        }
    }
}
