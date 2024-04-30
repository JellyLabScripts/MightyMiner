package com.jelly.MightyMinerV2.Handler;

import com.jelly.MightyMinerV2.Event.MotionUpdateEvent;
import com.jelly.MightyMinerV2.Util.AngleUtil;
import com.jelly.MightyMinerV2.Util.LogUtil;
import com.jelly.MightyMinerV2.Util.helper.Angle;
import com.jelly.MightyMinerV2.Util.helper.RotationConfiguration;
import com.jelly.MightyMinerV2.Util.helper.Target;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Deque;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RotationHandler {
    private static RotationHandler instance;

    public static RotationHandler getInstance() {
        if (instance == null) instance = new RotationHandler();
        return instance;
    }

    // Using this as a Queue
    private final Deque<RotationConfiguration> rotations = new ConcurrentLinkedDeque<>();
    private final Minecraft mc = Minecraft.getMinecraft();

    @Getter
    private boolean enabled;
    private long startTime;
    private long endTime;

    private final Angle startRotation = new Angle(0f, 0f);
    private Target target = new Target(new Angle(0, 0));

    // I know there is a better way to do this. I'm unable to brain rn
    private float lastBezierYaw = 0;
    private float lastBezierPitch = 0;

    private float clientSideYaw = 0;
    private float clientSidePitch = 0;
    private float serverSideYaw = 0;
    private float serverSidePitch = 0;

    private int randomMultiplier1 = 1;
    private int randomMultiplier2 = 1;

    @Getter
    private RotationConfiguration configuration;
    private final Random random = new Random();

    // For Debug Purpose
    void send(String msg) {
        LogUtil.send(msg, LogUtil.ELogType.SUCCESS);
    }

    // So that I can do RotationHandler.getInstance().queueRotation(config).start()
    // But RotationHandler.getInstance().easeTo(config) works as well
    // So both works. This is here to add more randomization.
    // Or in case we need to look at a few things that we already know of, so we can just queue them and wait for it to finish.
    public RotationHandler queueRotation(RotationConfiguration config) {
        this.rotations.add(config);
        return instance;
    }

    // Todo: Needs more testing and tweaking
    // Todo: Improve code
    // Todo: Remove Aids
    public RotationHandler queueRotation(RotationConfiguration config, boolean splitRotations) {
        if (!splitRotations) return queueRotation(config);
        Angle startAngle = config.from().isPresent() ? config.from().get() : AngleUtil.getPlayerAngle();
        Angle change = AngleUtil.getNeededChange(startAngle, config.target().get().getAngle());
        double dist = pythagoras(change.getYaw(), change.getPitch());
        long time = getTime(dist, config.time());

        if (random.nextInt(180) > dist) {
            return queueRotation(config);
        }

        float randomNumber = 0.5f + new Random().nextFloat() * 0.2f;
        float newIntermediateTargetYaw = startAngle.getYaw() + change.getYaw() * randomNumber;
        float newIntermediateTargetPitch = startAngle.getPitch() + change.getPitch() * randomNumber;
        RotationConfiguration config1 = new RotationConfiguration(
                new Angle(newIntermediateTargetYaw, newIntermediateTargetPitch),
                (long) (time * randomNumber),
                () -> LogUtil.send("First Rotation Ended", LogUtil.ELogType.DEBUG));

        if (config.from().isPresent()) config1.from(config.from());

        RotationConfiguration config2 = new RotationConfiguration(config.target().get(),
                (long) (time * (1.1 - randomNumber)),
                config.callback().orElse(() -> {
                }));

        queueRotation(config1);
        queueRotation(config2);
        return instance;
    }

    // For Queue
    public void start() {
        if (this.rotations.isEmpty()) {
            send("Rotation queue is empty");
            return;
        }
        this.easeTo(rotations.poll());
    }

    public void easeTo(RotationConfiguration configuration) {
        this.configuration = configuration;
        this.startTime = System.currentTimeMillis();
        this.startRotation.setRotation(configuration.from().orElse(AngleUtil.getPlayerAngle()));

        // We will be using a Target object always as the end. We won't switch between angle/rotation and we will always use a Target Object.
        // We are always setting a target object in RotationConfiguration so we dont have to check if target.isPresent(), it will always be present
        // unless it was changed/removed manually.
        this.target = configuration.target().get();

        Angle change = AngleUtil.getNeededChange(this.startRotation, this.target.getTargetAngle());
        this.endTime = this.startTime + getTime(pythagoras(change.getYaw(), change.getPitch()), configuration.time());

        // These multipliers define which way the rotation curves. This is randomized so that the curve is randomized
        this.randomMultiplier1 = random.nextBoolean() ? 1 : -1;
        this.randomMultiplier2 = random.nextBoolean() ? 1 : -1;

        this.lastBezierYaw = 0;
        this.lastBezierPitch = 0;

        if (configuration.rotationType() == RotationConfiguration.RotationType.SERVER) {
            if (serverSideYaw == 0 && serverSidePitch == 0) {
                serverSideYaw = mc.thePlayer.rotationYaw;
                serverSidePitch = mc.thePlayer.rotationPitch;
            } else {
                this.startRotation.setYaw(AngleUtil.get360RotationYaw(serverSideYaw));
                this.startRotation.setPitch(serverSidePitch);
            }
        }

        this.enabled = true;
    }

    public void reset() {
        this.enabled = false;
        this.configuration = null;
        this.target = null;
        this.rotations.clear();
        this.startTime = this.endTime = 0L;
        this.clientSideYaw = this.clientSidePitch
                = this.serverSideYaw = this.serverSidePitch
                = this.lastBezierYaw = this.lastBezierPitch = 0;
    }

    private double pythagoras(float yaw, float pitch) {
        return Math.sqrt(yaw * yaw + pitch * pitch);
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
            handleRotationEnd();
            return;
        }

        Angle bezierAngle = getBezierAngle();

        mc.thePlayer.rotationYaw += bezierAngle.getYaw() - lastBezierYaw;
        mc.thePlayer.rotationPitch += bezierAngle.getPitch() - lastBezierPitch;
        lastBezierYaw = bezierAngle.getYaw();
        lastBezierPitch = bezierAngle.getPitch();
    }

    private Angle getBezierAngle() {
        float timeProgress = (System.currentTimeMillis() - this.startTime) / (float) (this.endTime - this.startTime);
        float rotationProgress = configuration.easeFunction().apply(timeProgress);

        Angle bezierEnd = AngleUtil.getNeededChange(this.startRotation, this.target.getTargetAngle());
        Angle control1 = new Angle(bezierEnd.getYaw() * 0.05f * this.randomMultiplier1, bezierEnd.getYaw() * 0.1f * this.randomMultiplier2);
        Angle control2 = new Angle(bezierEnd.getYaw() - bezierEnd.getYaw() * 0.05f * this.randomMultiplier2, bezierEnd.getPitch() - bezierEnd.getYaw() * 0.1f * this.randomMultiplier1);

        double bezierYawSoFar = bezier(rotationProgress, control1.getYaw(), control2.getYaw(), bezierEnd.getYaw());
        double bezierPitchSoFar = bezier(rotationProgress, control1.getPitch(), control2.getPitch(), bezierEnd.getPitch());
        return new Angle((float) bezierYawSoFar, (float) bezierPitchSoFar);
    }

    private double bezier(float t, float c1, float c2, float end) {
        return 3 * Math.pow((1 - t), 2) * t * c1 + 3 * (1 - t) * Math.pow(t, 2) * c2 + Math.pow(t, 3) * end;
    }

    private void handleRotationEnd() {
        if (this.configuration.followTarget()) {
            this.easeTo(configuration);
            return;
        }

        configuration.callback().ifPresent(Runnable::run);

        if (!this.rotations.isEmpty()) {
            this.easeTo(this.rotations.poll());
            return;
        }

        if (this.configuration.rotationType() == RotationConfiguration.RotationType.SERVER && this.configuration.easeBackToClientSide()) {
            RotationConfiguration newConf = new RotationConfiguration(AngleUtil.getPlayerAngle(), this.configuration.time(), RotationConfiguration.RotationType.SERVER, () -> {
            });
            this.easeTo(newConf);
            return;
        }

        Angle change = AngleUtil.getNeededChange(AngleUtil.getPlayerAngle(), target.getTargetAngle());
        mc.thePlayer.rotationYaw += change.getYaw();
        mc.thePlayer.rotationPitch += change.getPitch();
        reset();
    }

//    private double bezier(float t, float start, float c1, float c2, float end) {
//        return Math.pow((1 - t), 3) * start + 3 * Math.pow((1 - t), 2) * t * c1 + 3 * (1 - t) * Math.pow(t, 2) * c2 + Math.pow(t, 3) * end;
//    }

    @SubscribeEvent(receiveCanceled = true)
    public void onMotionUpdatePre(MotionUpdateEvent.Pre event) {
        if (!enabled || this.configuration == null || this.configuration.rotationType() != RotationConfiguration.RotationType.SERVER)
            return;

        if (System.currentTimeMillis() >= this.endTime) {
            handleRotationEnd();
            if (!enabled) return;
        }

        clientSideYaw = mc.thePlayer.rotationYaw;
        clientSidePitch = mc.thePlayer.rotationPitch;

        Angle bezierAngle = getBezierAngle();

        serverSideYaw += bezierAngle.getYaw() - lastBezierYaw;
        serverSidePitch += bezierAngle.getPitch() - lastBezierPitch;

        mc.thePlayer.rotationYaw = serverSideYaw;
        mc.thePlayer.rotationPitch = serverSidePitch;

        lastBezierYaw = bezierAngle.getYaw();
        lastBezierPitch = bezierAngle.getPitch();
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onMotionUpdatePost(MotionUpdateEvent.Post event) {
        if (!enabled || this.configuration == null || this.configuration.rotationType() != RotationConfiguration.RotationType.SERVER)
            return;

        mc.thePlayer.rotationYaw = clientSideYaw;
        mc.thePlayer.rotationPitch = clientSidePitch;
    }
}
