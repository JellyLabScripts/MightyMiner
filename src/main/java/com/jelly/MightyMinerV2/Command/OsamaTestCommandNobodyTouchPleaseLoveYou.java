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
    public void main(){

    }

    @SubCommand(aliases = {"rot"})
    public void rotate(){
        Target t = new Target(new Angle(90, 0));
        RotationConfiguration conf = new RotationConfiguration(t, 700, () -> LogUtil.send("Rotated", LogUtil.ELogType.SUCCESS));
        RotationHandler.getInstance().easeTo(conf);
    }

    @SubCommand
    public void stop(){
        RotationHandler.getInstance().reset();
    }
}
