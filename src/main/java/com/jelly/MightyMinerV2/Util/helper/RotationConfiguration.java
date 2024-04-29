package com.jelly.MightyMinerV2.Util.helper;

import java.util.Random;
import java.util.function.Function;

import com.jelly.MightyMinerV2.Util.AngleUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;

import java.util.Optional;

@Getter
@Setter
@Accessors(fluent = true)
public class RotationConfiguration {
    private final Minecraft mc = Minecraft.getMinecraft();
    private Angle from;
    private Optional<Angle> to = Optional.empty();
    private Optional<Target> target = Optional.empty();
    private Optional<Runnable> callback;
    private long time;
    private boolean goingBackToClientSide = false;
    private boolean followTarget = false;
    private RotationType rotationType = RotationType.CLIENT;
    private Ease easeOutBack = Ease.values()[new Random().nextInt(Ease.values().length - 1)];
    private boolean randomness = false;

    public RotationConfiguration(Angle from, Angle to, long time, RotationType rotationType, Runnable callback) {
        this.from = from;
        this.to = Optional.ofNullable(to);
        this.target = Optional.of(new Target(to));
        this.time = time;
        this.rotationType = rotationType;
        this.callback = Optional.ofNullable(callback);
    }

    public RotationConfiguration(Angle from, Target target, long time, RotationType rotationType, Runnable callback) {
        this.from = from;
        this.time = time;
        this.target = Optional.ofNullable(target);
        this.rotationType = rotationType;
        this.callback = Optional.ofNullable(callback);
    }

    public RotationConfiguration(Angle to, long time, Runnable callback) {
//        this.from = AngleHandler.getInstance().getConfiguration() != null && RotationHandler.getInstance().getConfiguration().goingBackToClientSide() ? new Rotation(RotationHandler.getInstance().getServerSideYaw(), RotationHandler.getInstance().getServerSidePitch()) : new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        this.from = AngleUtil.getPlayerAngle();
        this.to = Optional.ofNullable(to);
        this.target = Optional.of(new Target(to));
        this.time = time;
        this.callback = Optional.ofNullable(callback);
    }

    public RotationConfiguration(Angle to, long time, RotationType rotationType, Runnable callback) {
//        this.from = AngleHandler.getInstance().getConfiguration() != null && RotationHandler.getInstance().getConfiguration().goingBackToClientSide() ? new Rotation(RotationHandler.getInstance().getServerSideYaw(), RotationHandler.getInstance().getServerSidePitch()) : new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        this.from = AngleUtil.getPlayerAngle();
        this.to = Optional.ofNullable(to);
        this.target = Optional.of(new Target(to));
        this.time = time;
        this.rotationType = rotationType;
        this.callback = Optional.ofNullable(callback);
    }

    public RotationConfiguration(Target target, long time, Runnable callback) {
//        this.from = AngleHandler.getInstance().getConfiguration() != null && RotationHandler.getInstance().getConfiguration().goingBackToClientSide() ? new Rotation(RotationHandler.getInstance().getServerSideYaw(), RotationHandler.getInstance().getServerSidePitch()) : new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        this.from = AngleUtil.getPlayerAngle();
        this.time = time;
        this.target = Optional.ofNullable(target);
        this.callback = Optional.ofNullable(callback);
    }

    public RotationConfiguration(Target target, long time, RotationType rotationType, Runnable callback) {
//        this.from = AngleHandler.getInstance().getConfiguration() != null && RotationHandler.getInstance().getConfiguration().goingBackToClientSide() ? new Rotation(RotationHandler.getInstance().getServerSideYaw(), RotationHandler.getInstance().getServerSidePitch()) : new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        this.from = AngleUtil.getPlayerAngle();
        this.time = time;
        this.target = Optional.ofNullable(target);
        this.rotationType = rotationType;
        this.callback = Optional.ofNullable(callback);
    }

    public enum RotationType {
        SERVER,
        CLIENT
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public enum Ease {
        EASE_OUT_BACK(x -> 1 + (1 + 2 * (x - 1)) * (x - 1) * (x - 1)),
        EASE_OUT_SINE(x -> (float) Math.sin((x * Math.PI) / 2)),
        EASE_IN_OUT_SINE(x -> (float) (-(Math.cos(x * Math.PI) - 1) / 2)),
        EASE_OUT_QUAD(x -> 1 - (1 - x) * (1 - x)),
        EASE_OUT_CUBIC(x -> 1 - (1 - x) * (1 - x) * (1 - x)),
        EASE_OUT_CIRC(x -> (float) Math.sqrt(1 - (x - 1) * (x - 1))),
        EASE_OUT_MIN_JERK(x -> (float) (6 * Math.pow(x, 5) - 15 * Math.pow(x, 4) + 10 * Math.pow(x, 3)));

        private final Function<Float, Float> easingFunction;

        Ease(Function<Float, Float> easingFunction) {
            this.easingFunction = easingFunction;
        }

        public float apply(float x) {
            return easingFunction.apply(x);
        }
    }
}
