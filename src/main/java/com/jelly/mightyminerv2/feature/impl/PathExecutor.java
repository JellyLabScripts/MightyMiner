package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.pathfinder.calculate.Path;
import com.jelly.mightyminerv2.util.*;
import com.jelly.mightyminerv2.util.helper.Angle;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import kotlin.Pair;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PathExecutor {

    private static PathExecutor instance;
    private final Minecraft mc = Minecraft.getMinecraft();
    @Getter
    private final Deque<Path> pathQueue = new LinkedList<>();
    private final Map<Long, List<Long>> map = new HashMap<>();
    private final List<BlockPos> blockPath = new ArrayList<>();
    private final Clock stuckTimer = new Clock();
    @Getter
    private boolean enabled = false;
    private Path prev;
    private Path curr;
    private boolean failed = false;
    private boolean succeeded = false;

    private boolean pastTarget = false;

    private Random random = new Random();
    private double lastPitch = 10 + (15 - 10) * random.nextDouble();
    private Clock dynamicPitch = new Clock();

    private int target = 0;
    private int previous = -1;
    private long nodeChangeTime = 0;

    private boolean interpolated = true;
    private float interpolYawDiff = 0f;

    @Getter
    private State state = State.STARTING_PATH;

    @Setter
    private boolean allowSprint = true;
    @Setter
    private boolean allowInterpolation = false;

    public static PathExecutor getInstance() {
        if (instance == null) {
            instance = new PathExecutor();
        }
        return instance;
    }

    public void queuePath(Path path) {
        if (path.getPath().isEmpty()) {
            error("Path is empty");
            failed = true;
            return;
        }

        BlockPos start = path.getStart();
        Path lastPath = (this.curr != null) ? this.curr : this.pathQueue.peekLast();

        if (lastPath != null && !lastPath.getGoal().isAtGoal(start.getX(), start.getY(), start.getZ())) {
            error("This path segment does not start at last path's goal. LastpathGoal: " + lastPath.getGoal() + ", ThisPathStart: " + start);
            failed = true;
            return;
        }

        this.pathQueue.offer(path);
    }

    public void start() {
        this.state = State.STARTING_PATH;
        this.enabled = true;
    }

    public void stop() {
        this.enabled = false;
        this.pathQueue.clear();
        this.blockPath.clear();
        this.map.clear();
        this.curr = null;
        this.prev = null;
        this.target = 0;
        this.previous = -1;
        this.pastTarget = false;
        this.state = State.END;
        this.interpolYawDiff = 0f;
        this.allowSprint = true;
        this.allowInterpolation = false;
        this.nodeChangeTime = 0;
        this.interpolated = true;
        StrafeUtil.enabled = false;
        RotationHandler.getInstance().stop();
        KeyBindUtil.releaseAllExcept();
    }

    public void clearQueue() {
        this.pathQueue.clear();
        this.curr = null;
        this.succeeded = true;
        this.failed = false;
        this.interpolated = false;
        this.target = 0;
        this.previous = -1;
    }

    public boolean onTick() {
        if (!enabled) {
            return false;
        }

        if (this.stuckTimer.isScheduled() && this.stuckTimer.passed()) {
            log("Was Stuck For a Second.");
            this.failed = true;
            this.succeeded = false;
            this.stop();
        }

        BlockPos playerPos = PlayerUtil.getBlockStandingOn();
        if (this.curr != null) {
            // this is utterly useless but im useless as well
            List<Long> blockHashes = this.map.get(this.pack(playerPos.getX(), playerPos.getZ()));
            int current = -1;
            if (blockHashes != null && !blockHashes.isEmpty()) {
                int bestY = -1;
                double playerY = mc.thePlayer.posY;
                for (Long blockHash : blockHashes) {
                    Pair<Integer, Integer> block = this.unpack(blockHash);
                    int blockY = block.getFirst();
                    int blockTarget = block.getSecond();
                    if (blockTarget > this.previous) {
                        if (bestY == -1 || (blockY < playerY && blockY > bestY) || (blockY >= playerY && blockY < bestY)) {
                            bestY = block.getFirst();
                            current = blockTarget;
                        }
                    }
                }
            }

            if (current != -1 && current > previous) {
                this.previous = current;
                this.target = current + 1;
                this.state = State.TRAVERSING;
                this.pastTarget = false;
                this.interpolated = false;
                this.interpolYawDiff = 0;
                this.nodeChangeTime = System.currentTimeMillis();
                log("changed target from " + this.previous + " to " + this.target);
                RotationHandler.getInstance().stop();
            }

            if (Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ) < 0.05) {
                if (!this.stuckTimer.isScheduled()) {
                    this.stuckTimer.schedule(1000);
                }
            } else {
                this.stuckTimer.reset();
            }
        } else {
            if (this.stuckTimer.isScheduled()) {
                this.stuckTimer.reset();
            }
            if (this.pathQueue.isEmpty()) {
                return true;
            }
        }

        if (this.curr == null || this.target == this.blockPath.size()) {
            log("Path traversed");
            if (this.pathQueue.isEmpty()) {
                log("Pathqueue is empty");
                if (this.curr != null) {
                    this.curr = null;
                    this.target = 0;
                    this.previous = -1;
                }
                this.state = State.WAITING;
                return true;
            }
            this.succeeded = true;
            this.failed = false;
            this.prev = this.curr;
            this.target = 1;
            this.previous = 0;
            loadPath(this.pathQueue.poll());
            if (this.target == this.blockPath.size()) {
                return true;
            }
            log("loaded new path target: " + this.target + ", prev: " + this.previous);
        }

        BlockPos target = this.blockPath.get(this.target);

        if (this.target < this.blockPath.size() - 1) {
            BlockPos nextTarget = this.blockPath.get(this.target + 1);
            double playerDistToNext = playerPos.distanceSq(nextTarget);
            double targetDistToNext = target.distanceSq(nextTarget);

            if ((this.pastTarget || (this.pastTarget = playerDistToNext > targetDistToNext)) && playerDistToNext < targetDistToNext) {
                this.previous = this.target;
                this.target++;
                target = this.blockPath.get(this.target);
                log("walked past target");
            }
        }

        boolean onGround = mc.thePlayer.onGround;

        int targetX = target.getX();
        int targetZ = target.getZ();
        double horizontalDistToTarget = Math.hypot(mc.thePlayer.posX - targetX - 0.5, mc.thePlayer.posZ - targetZ - 0.5);
        float yaw = AngleUtil.getRotationYaw360(mc.thePlayer.getPositionVector(), new Vec3(targetX + 0.5, 0.0, targetZ + 0.5));
        float yawDiff = Math.abs(AngleUtil.get360RotationYaw() - yaw);

        if (this.interpolYawDiff == 0) {
            this.interpolYawDiff = yaw - AngleUtil.get360RotationYaw();
        }

        if (yawDiff > 3 && !RotationHandler.getInstance().isEnabled()) {
            float rotYaw = yaw;

            // look at a block thats at least 5 blocks away instead of looking at the target which helps reduce buggy rotation
            for (int i = this.target; i < this.blockPath.size(); i++) {
                BlockPos rotationTarget = this.blockPath.get(i);
                if (Math.hypot(mc.thePlayer.posX - rotationTarget.getX(), mc.thePlayer.posZ - rotationTarget.getZ()) > 5) {
                    rotYaw = AngleUtil.getRotation(rotationTarget).yaw;
                    break;
                }
            }

            float time = MightyMinerConfig.fixrot ? MightyMinerConfig.rottime : Math.max(300, (long) (400 - horizontalDistToTarget * MightyMinerConfig.rotmult));

            if (!dynamicPitch.isScheduled() || dynamicPitch.passed()) {
                lastPitch = 10 + (15 - 10) * random.nextDouble();
                dynamicPitch.schedule(1000);
            }

            // TODO: Implement back route miner
            RotationHandler.getInstance().easeTo(
                    new RotationConfiguration(
                            new Angle(rotYaw, (float) lastPitch),
                            (long) time, null
                    )
            );
        }

        float ipYaw = yaw;
        if (onGround && horizontalDistToTarget >= 8 && this.allowInterpolation && !this.interpolated) {
            float time = 200f;
            long timePassed = Math.min((long) time, System.currentTimeMillis() - this.nodeChangeTime);
            ipYaw -= this.interpolYawDiff * (1 - (timePassed / time));
            if (timePassed == time) {
                this.interpolated = true;
            }
        }

        StrafeUtil.enabled = yawDiff > 3;
        StrafeUtil.yaw = ipYaw;

        Vec3 pos = new Vec3(mc.thePlayer.posX, playerPos.getY() + 0.5, mc.thePlayer.posZ);
        Vec3 vec4Rot = AngleUtil.getVectorForRotation(yaw);
        boolean shouldJump = BlockUtil.canWalkBetween(this.curr.getCtx(), pos, pos.addVector(vec4Rot.xCoord, 1, vec4Rot.zCoord));
        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, true);
        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSprint, this.allowSprint && yawDiff < 40 && !shouldJump);
        if (shouldJump && onGround) {
            mc.thePlayer.jump();
            this.state = State.JUMPING;
        }
        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindJump, shouldJump);

        return playerPos.distanceSqToCenter(target.getX(), target.getY(), target.getZ()) < 100;
    }

    public void loadPath(Path path) {
        this.blockPath.clear();
        this.map.clear();

        this.curr = path;
        this.blockPath.addAll(this.curr.getSmoothedPath());
        for (int i = 0; i < this.blockPath.size(); i++) {
            BlockPos pos = this.blockPath.get(i);
            this.map.computeIfAbsent(this.pack(pos.getX(), pos.getZ()), k -> new ArrayList<>()).add(this.pack(pos.getY(), i));
        }
    }

    public void onRender() {
        System.out.println("OnRender");
        if (this.target != -1 && this.target < this.blockPath.size()) {
            System.out.println("valtarg");
            BlockPos playerPos = PlayerUtil.getBlockStandingOn();
            BlockPos target = this.blockPath.get(this.target);
            int targetX = target.getX();
            int targetZ = target.getZ();
            float yaw = AngleUtil.getRotationYaw360(mc.thePlayer.getPositionVector(), new Vec3(targetX + 0.5, 0.0, targetZ + 0.5));
            Vec3 pos = new Vec3(mc.thePlayer.posX, playerPos.getY() + 0.5, mc.thePlayer.posZ);
            Vec3 vec4Rot = AngleUtil.getVectorForRotation(yaw);
            Vec3 newV = pos.addVector(vec4Rot.xCoord, playerPos.getY() + 1, vec4Rot.zCoord);
            RenderUtil.drawBlock(new BlockPos(MathHelper.floor_double(newV.xCoord), MathHelper.floor_double(newV.yCoord), MathHelper.floor_double(newV.zCoord)),
                    new Color(255, 0, 0, 255));
            RenderUtil.drawBlock(playerPos, new Color(255, 255, 0, 100));
        }
    }

    public Path getPreviousPath() {
        return this.prev;
    }

    public Path getCurrentPath() {
        return this.curr;
    }

    public boolean failed() {
        return !this.enabled && this.failed;
    }

    public boolean ended() {
        return !this.enabled && this.succeeded;
    }

    private long pack(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    public Pair<Integer, Integer> unpack(long packed) {
        return new Pair<>((int) (packed >> 32), (int) packed);
    }

    void log(String message) {
        Logger.sendLog(getMessage(message));
    }

    void send(String message) {
        Logger.sendMessage(getMessage(message));
    }

    void error(String message) {
        Logger.sendError(getMessage(message));
    }

    void note(String message) {
        Logger.sendNote(getMessage(message));
    }

    String getMessage(String message) {
        return "[PathExecutor] " + message;
    }

    enum State {
        STARTING_PATH, TRAVERSING, JUMPING, WAITING, END
    }
}
