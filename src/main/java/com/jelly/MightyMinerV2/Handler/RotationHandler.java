package com.jelly.MightyMinerV2.Handler;

import com.jelly.MightyMinerV2.Util.AngleUtil;
import com.jelly.MightyMinerV2.Util.LogUtil;
import com.jelly.MightyMinerV2.Util.helper.Angle;
import com.jelly.MightyMinerV2.Util.helper.RotationConfiguration;
import com.jelly.MightyMinerV2.Util.helper.Target;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class RotationHandler {
    private static RotationHandler instance;

    public static RotationHandler getInstance() {
        if (instance == null) instance = new RotationHandler();
        return instance;
    }

    private final Minecraft mc = Minecraft.getMinecraft();

    @Getter
    private boolean enabled;
    private long startTime;
    private long endTime;

    private final Angle startRotation = new Angle(0f, 0f);
    private Target target = new Target(new Angle(0, 0));

    @Getter
    private float clientSideYaw = 0;
    @Getter
    private float clientSidePitch = 0;
    /* Not yet
    @Getter
    private float serverSideYaw = 0;
    @Getter
    private float serverSidePitch = 0;
     */

    private int randomMultiplier1 = 1;
    private int randomMultiplier2 = 1;

    @Getter
    private RotationConfiguration configuration;

    void send(String msg){
        LogUtil.send(msg, LogUtil.ELogType.SUCCESS);
    }

    public void easeTo(RotationConfiguration configuration) {
        this.configuration = configuration;
        this.startTime = System.currentTimeMillis();
        this.startRotation.setRotation(configuration.from());
        this.target = configuration.target().get();
        Angle targetAngle = this.target.getAngle();

        Angle change = AngleUtil.getNeededChange(this.startRotation, targetAngle);
        this.endTime = this.startTime + getTime(Math.sqrt(Math.pow(change.getYaw(), 2) + Math.pow(change.getPitch(), 2)), configuration.time());
        this.clientSideYaw = this.startRotation.getYaw();
        this.clientSidePitch = this.startRotation.getPitch();

        Random rand = new Random();
        this.randomMultiplier1 = rand.nextBoolean() ? 1 : -1;
        this.randomMultiplier2 = rand.nextBoolean() ? 1 : -1;

        send("Started. Start: " + this.startRotation + ", End: " + this.target.getAngle() + ", YawRN: " + mc.thePlayer.rotationYaw);
        this.enabled = true;
    }

    public void reset() {
        enabled = false;
        configuration = null;
        startTime = 0;
        endTime = 0;
        send("Ended. YawRN: " + mc.thePlayer.rotationYaw);
    }

    private long getTime(double pythagoras, long time) {
        if (pythagoras < 25) {
            return (long) (time * 0.65);
        }
        if (pythagoras < 45) {
            return (long) (time * 0.77);
        }
        if (pythagoras < 80) {
            return (long) (time * 0.9);
        }
        if (pythagoras > 100) {
            return (long) (time * 1.1);
        }
        return (long) (time * 1.0);
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (!enabled || this.configuration == null || this.configuration.rotationType() != RotationConfiguration.RotationType.CLIENT)
            return;

        if (System.currentTimeMillis() >= this.endTime) {
            // disable
            if (configuration.callback().isPresent()) {
                configuration.callback().get().run();
            }

            Angle changeToEnd = AngleUtil.getNeededChange(AngleUtil.getPlayerAngle(), target.getAngle());
            mc.thePlayer.rotationYaw += changeToEnd.getYaw();
            mc.thePlayer.rotationPitch += changeToEnd.getPitch();
            reset();
            return;
        }

        float timeProgress = (System.currentTimeMillis() - startTime) / (float) (endTime - startTime);
        float rotationProgress = configuration.easeOutBack().apply(timeProgress);

        Angle targetAngle = this.target.getAngle();
        float change = AngleUtil.getNeededChange(this.startRotation, targetAngle).getYaw() / 5;

        Angle c1 = new Angle(this.startRotation.getYaw() + change * randomMultiplier1, this.startRotation.getPitch() + (change / 2) * randomMultiplier2);
        Angle c2 = new Angle(targetAngle.getYaw() - change * randomMultiplier2, targetAngle.getPitch() - (change / 2) * randomMultiplier1);

        Angle newAngle = interpolate(rotationProgress, this.startRotation, c1, c2, targetAngle);

        clientSideYaw = AngleUtil.get360RotationYaw(mc.thePlayer.rotationYaw += newAngle.getYaw() - clientSideYaw);
        clientSidePitch = mc.thePlayer.rotationPitch += newAngle.getPitch() - clientSidePitch;
    }

    private Angle interpolate(float rotationProgress, Angle start, Angle c1, Angle c2, Angle end){
        double newX = bezier(rotationProgress, start.getYaw(), c1.getYaw(), c2.getYaw(), end.getYaw());
        double newY = bezier(rotationProgress, start.getPitch(), c1.getPitch(), c2.getPitch(), end.getPitch());
        return new Angle((float) newX, (float) newY);
    }

    private double bezier(float t, float start, float c1, float c2, float end) {
        return Math.pow((1 - t), 3) * start + 3 * Math.pow((1 - t), 2) * t * c1 + 3 * (1 - t) * Math.pow(t, 2) * c2 + Math.pow(t, 3) * end;
    }
}
