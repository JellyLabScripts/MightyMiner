package com.jelly.MightyMinerV2.Command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.MightyMinerV2.Handler.RotationHandler;
import com.jelly.MightyMinerV2.Util.KeyBindUtil;
import com.jelly.MightyMinerV2.Util.LogUtil;
import com.jelly.MightyMinerV2.Util.RenderUtil;
import com.jelly.MightyMinerV2.Util.helper.Angle;
import com.jelly.MightyMinerV2.Util.helper.RotationConfiguration;
import com.jelly.MightyMinerV2.Util.helper.Target;
import net.minecraft.client.Minecraft;
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
        LogUtil.send("", LogUtil.ELogType.SUCCESS);
        LogUtil.send("Queued Client Side Rotation", LogUtil.ELogType.SUCCESS);
        Target t1 = new Target(new Angle(-10, 0));
        Target t2 = new Target(new Angle(90, 0));
        RotationConfiguration conf = new RotationConfiguration(t1, 700, RotationConfiguration.RotationType.CLIENT, () -> LogUtil.send("First Rotation Ended", LogUtil.ELogType.SUCCESS));
        RotationConfiguration conf2 = new RotationConfiguration(t2, 1000, RotationConfiguration.RotationType.CLIENT, () -> LogUtil.send("Second Rotation Ended", LogUtil.ELogType.SUCCESS));
        RotationHandler.getInstance().queueRotation(conf).queueRotation(conf2).start();
    }

    @SubCommand(aliases = {"rot"})
    public void rotate() {
        LogUtil.send("", LogUtil.ELogType.SUCCESS);
        LogUtil.send("Server Side Rotation with Ease Back", LogUtil.ELogType.SUCCESS);
        Target t1 = new Target(new Angle(-10, 0));
        entTodraw = (Entity) Minecraft.getMinecraft().theWorld.loadedEntityList.stream().filter(ent -> ent instanceof EntityLivingBase && ent != Minecraft.getMinecraft().thePlayer).toArray()[0];
        Target t2 = new Target(entTodraw);
        t2.additionalY(1.5f);
        RotationConfiguration conf = new RotationConfiguration(t2, 1000, RotationConfiguration.RotationType.SERVER, KeyBindUtil::leftClick);
        conf.followTarget(true);
        conf.easeBackToClientSide(true);
        RotationHandler.getInstance().easeTo(conf);
    }

    @SubCommand(aliases = {"rot2"})
    public void rotate2() {
        LogUtil.send("", LogUtil.ELogType.SUCCESS);
        LogUtil.send("Queued Server Side Rotation", LogUtil.ELogType.SUCCESS);
        Target t1 = new Target(new Angle(-10, 0));
        Target t2 = new Target(new Angle(90, 0));
        RotationConfiguration conf = new RotationConfiguration(t1, 700, RotationConfiguration.RotationType.SERVER, () -> LogUtil.send("First Rotation Ended", LogUtil.ELogType.SUCCESS));
        RotationConfiguration conf2 = new RotationConfiguration(t2, 1000, RotationConfiguration.RotationType.SERVER, () -> LogUtil.send("Second Rotation Ended", LogUtil.ELogType.SUCCESS));
        RotationHandler.getInstance().queueRotation(conf).queueRotation(conf2).start();
    }

    @SubCommand
    public void stop() {
//        RotationHandler.getInstance().reset();
        if (RotationHandler.getInstance().getConfiguration() != null) {
            RotationHandler.getInstance().getConfiguration().followTarget(false);
        } else {
            RotationHandler.getInstance().reset();
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (entTodraw != null) {
            RenderUtil.drawBox(((EntityLivingBase) entTodraw).getEntityBoundingBox(), Color.CYAN);
        }
    }
}
