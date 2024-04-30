package com.jelly.MightyMinerV2.Command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.MightyMinerV2.Handler.RotationHandler;
import com.jelly.MightyMinerV2.Util.LogUtil;
import com.jelly.MightyMinerV2.Util.helper.Angle;
import com.jelly.MightyMinerV2.Util.helper.RotationConfiguration;
import com.jelly.MightyMinerV2.Util.helper.Target;

@Command(value = "set")
public class OsamaTestCommandNobodyTouchPleaseLoveYou {
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
        Target t2 = new Target(new Angle(90, 0));
        RotationConfiguration conf = new RotationConfiguration(t2, 1000, RotationConfiguration.RotationType.SERVER, () -> LogUtil.send("Second Rotation Ended", LogUtil.ELogType.SUCCESS));
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
        RotationHandler.getInstance().reset();
    }
}
