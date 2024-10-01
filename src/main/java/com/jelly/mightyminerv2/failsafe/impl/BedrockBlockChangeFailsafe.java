package com.jelly.mightyminerv2.failsafe.impl;

import net.minecraft.network.play.server.S23PacketBlockChange;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;


/*Hours wasted on this: None, this was easy as fuck*/
public class BedrockBlockChangeFailsafe extends AbstractFailsafe {

    private static final BedrockBlockChangeFailsafe instance = new BedrockBlockChangeFailsafe();

    public static BedrockBlockChangeFailsafe getInstance() {
        return instance;
    }

    private final Clock timer = new Clock();
    private final List<Long> bedrockChangeTimestamps = new ArrayList<>();
    private static final int THRESHOLD = 10; // Maximum allowed bedrock changes
    private static final long TIME_WINDOW = 100; // 0.1-second time window
    private static final int RADIUS = 10;

    @Override
    public String getName() {
        return "BedrockBlockChangeFailsafe";
    }

    @Override
    public Failsafe getFailsafeType() {
        return Failsafe.BLOCK_CHANGE;
    }

    @Override
    public int getPriority() {
        return 7; // Higher priority
    }

    @Override
    public boolean onTick(ClientTickEvent event) {
        long currentTime = System.currentTimeMillis();

        // Clean up old entries from the list to only keep relevant ones within TIME_WINDOW
        bedrockChangeTimestamps.removeIf(timestamp -> currentTime - timestamp > TIME_WINDOW);

        if (bedrockChangeTimestamps.size() >= THRESHOLD) {
            Logger.sendWarning("Too many Bedrock block changes in the last " + TIME_WINDOW / 1000 + " seconds. Triggering failsafe.");
            return true;
        }

        return false;
    }

    @Override
    public boolean onPacketReceive(PacketEvent.Received event) {
        if (event.packet instanceof S23PacketBlockChange) {
            S23PacketBlockChange packet = (S23PacketBlockChange) event.packet;
            BlockPos blockPos = packet.getBlockPosition();
            net.minecraft.block.Block block = packet.getBlockState().getBlock();

            if (block == Blocks.bedrock) {
                BlockPos playerPos = mc.thePlayer.getPosition();

                double distance = playerPos.distanceSq(blockPos.getX(), blockPos.getY(), blockPos.getZ());

                double radiusSquared = RADIUS * RADIUS;

                if (distance <= radiusSquared) {
                    long currentTime = System.currentTimeMillis();
                    bedrockChangeTimestamps.add(currentTime);
//                    log("Bedrock block change detected at: " + currentTime + " within radius at position " + blockPos);
                }
            }
        }

        return false;
    }


    @Override
    public boolean react() {
        // Disable macro (iam a lazy mf and haven`t done more here)
        MacroManager.getInstance().disable();
        Logger.sendWarning("Too many Bedrock block changes nearby! Disabling macro.");
        return true;
    }

    @Override
    public void resetStates() {
        this.bedrockChangeTimestamps.clear(); // Clear the recorded timestamps
        this.timer.reset();
    }
}
