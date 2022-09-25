package com.jelly.MightyMiner.baritone.autowalk.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovementInput;

//inspired by baritone api
public class BaritoneMovement extends MovementInput {

    public BaritoneMovement(){}

    @Override
    public void updatePlayerMoveState() {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        jump = InputHandler.isInputForcedDown(Input.JUMP);

        if (InputHandler.isInputForcedDown(Input.MOVE_FORWARD)) {
            this.moveForward++;
        }

        if (InputHandler.isInputForcedDown(Input.MOVE_BACK)) {
            this.moveForward--;
        }

        if (InputHandler.isInputForcedDown(Input.MOVE_LEFT)) {
            this.moveStrafe++;
        }

        if (InputHandler.isInputForcedDown(Input.MOVE_RIGHT)) {
            this.moveStrafe--;
        }

        if (InputHandler.isInputForcedDown(Input.SNEAK)) {
            this.moveStrafe *= 0.3D;
            this.moveForward *= 0.3D;
        }
    }
}
