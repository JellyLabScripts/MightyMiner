package com.jelly.mightyminerv2.feature.impl.BlockMiner.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.util.AngleUtil;
import com.jelly.mightyminerv2.util.BlockUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.List;

/**
 * ApplyAbilityState
 * <p>
 * State responsible for activating the mining ability.
 * Waits for 1 second ({@code timer1}) and then right-clicks.
 * Then waits for 1 more second ({{@code timer2}}) to transition into the next state
 * <p>
 * If the player uses pickobulus, rotate to the farthest blue wool before right-clicking
 * <p>
 * Automatically throws error if it presses 2 times consecutively
 */
public class ApplyAbilityState implements BlockMinerState {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Clock timer1 = new Clock();
    private final Clock timer2 = new Clock();

    private final long COOLDOWN = 1000; // 1-second cooldown for activating ability

    @Override
    public void onStart(BlockMiner blockMiner) {
        log("Entering Apply Ability State");

        // Start the cooldown timer
        timer2.reset();
        timer1.reset();
        timer1.schedule(COOLDOWN);

        // Check if the pickaxe ability is pickobulus
        if (BlockMiner.getInstance().getPickaxeAbility() == BlockMiner.PickaxeAbility.PICKOBULUS) {
            final BlockPos blueWool = getFarthestBlueWool();

            if(blueWool == null) {
                log("Cannot find blue wool. Skipping the rotation.");
                return;
            }

            final List<Vec3> points = BlockUtil.bestPointsOnBestSide(blueWool);
            Vec3 targetPoint = new Vec3(
                blueWool.getX() + 0.5,
                blueWool.getY() + 0.5,
                blueWool.getZ() + 0.5
            );

            if(!points.isEmpty()) {
                targetPoint = points.get(0);
            }

            if(targetPoint != null) {
                log("Rotating to blue wool");
                RotationHandler.getInstance().easeTo(new RotationConfiguration(
                        AngleUtil.getRotation(targetPoint),
                        MightyMinerConfig.getRandomRotationTime(),
                        null
                ));
            }
        }

        // Release all keys to prepare for the right click
        if(Minecraft.getMinecraft().currentScreen == null) {
            KeyBindUtil.releaseAllExcept();
        }
    }

    @Override
    public BlockMinerState onTick(BlockMiner blockMiner) {

        // If the first timer has ended and the player is not rotating, press right-click
        if (timer1.isScheduled() && timer1.passed() && !RotationHandler.getInstance().isEnabled()) {
            timer1.reset();
            timer2.reset();
            timer2.schedule(COOLDOWN);
            KeyBindUtil.rightClick();
        }

        // If the second timer has ended, transition back to the starting state
        if (timer2.isScheduled() && timer2.passed()) {
            return new StartingState();
        }

        // Wait for the timer to expire
        return this;
    }

    private BlockPos getFarthestBlueWool() {
        BlockPos playerBlockPos = mc.thePlayer.getPosition();

        BlockPos farthestBlueWool = null;
        double maxDistance = 0;

        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    final BlockPos checkPos = playerBlockPos.add(x, y, z);
                    final Block block = mc.theWorld.getBlockState(checkPos).getBlock();

                    if (block == Blocks.wool && mc.theWorld.getBlockState(checkPos).getValue(BlockColored.COLOR) == EnumDyeColor.LIGHT_BLUE) {
                        double distance = playerBlockPos.distanceSq(checkPos);

                        if (distance > maxDistance) {
                            maxDistance = distance;
                            farthestBlueWool = checkPos;
                        }
                    }
                }
            }
        }

        return farthestBlueWool;
    }

    @Override
    public void onEnd(BlockMiner blockMiner) {
        log("Exiting Apply Ability State");
    }
}
