package com.jelly.mightyminerv2.pathfinder.costs

class ActionCosts(
    SPRINT_MOVEMENT_FACTOR: Double = 0.13,
    WALKING_MOVEMENT_FACTOR: Double = 0.1,
    SNEAKING_MOVEMENT_FACTOR: Double = 0.03,
    JUMP_BOOST_LEVEL: Int = -1
) {
    val INF_COST = 1e6
    val N_BLOCK_FALL_COST: DoubleArray = generateNBlocksFallCost()
    val ONE_UP_LADDER_COST: Double = 1 / (0.12 * 9.8) // 1 / (0.12b/t upward velocity * gravity)
    val ONE_DOWN_LADDER_COST: Double = 1 / 0.15 // 1 / .15b/t downward velocity

    val JUMP_ONE_BLOCK_COST: Double

    val ONE_BLOCK_WALK_COST = 1 / actionTime(getWalkingFriction(WALKING_MOVEMENT_FACTOR))
    val ONE_BLOCK_SPRINT_COST = 1 / actionTime(getWalkingFriction(SPRINT_MOVEMENT_FACTOR))
    val ONE_BLOCK_SNEAK_COST = 1 / actionTime(getWalkingFriction(SNEAKING_MOVEMENT_FACTOR))

    val ONE_BLOCK_WALK_IN_WATER_COST =
        20 * actionTime(getWalkingInWaterFriction(WALKING_MOVEMENT_FACTOR))
    val ONE_BLOCK_WALK_OVER_SOUL_SAND_COST = ONE_BLOCK_WALK_COST * 2

    val WALK_OFF_ONE_BLOCK_COST = ONE_BLOCK_WALK_COST * 0.8
    val CENTER_AFTER_FALL_COST = ONE_BLOCK_WALK_COST * 0.2

    val SPRINT_MULTIPLIER = WALKING_MOVEMENT_FACTOR / SPRINT_MOVEMENT_FACTOR

    init {
        // to discourage jumping unless necessary
        var vel = 0.42 + (JUMP_BOOST_LEVEL + 1) * 0.1
        var height = 0.0
        var time = 1.0
        for (i in 1..20) {
            height += vel
            vel = (vel - 0.08) * 0.98
            if (vel < 0)
                break
            time++
        }
        JUMP_ONE_BLOCK_COST = time + fallDistanceToTicks(height - 1)
    }

    private fun getWalkingFriction(landMovementFactor: Double): Double {
        return landMovementFactor * ((0.16277136) / (0.91 * 0.91 * 0.91))
    }

    private fun getWalkingInWaterFriction(landMovementFactor: Double): Double {
        return 0.02 + (landMovementFactor - 0.02) * (1.0 / 3.0)
    }

    private fun actionTime(friction: Double): Double {
        return friction * 10
    }

    fun motionYAtTick(tick: Int): Double {
        var velocity = -0.0784000015258789
        for (i in 1..tick) {
            velocity = (velocity - 0.08) * 0.9800000190734863
        }
        return velocity
    }

    fun fallDistanceToTicks(distance: Double): Double {
        if (distance == 0.0) return 0.0
        var tmpDistance = distance
        var tickCount = 0
        while (true) {
            val fallDistance = downwardMotionAtTick(tickCount)
            if (tmpDistance <= fallDistance) {
                return tickCount + tmpDistance / fallDistance
            }
            tmpDistance -= fallDistance
            tickCount++
        }
    }

    private fun downwardMotionAtTick(tick: Int): Double {
        return (Math.pow(0.98, tick.toDouble()) - 1) * -3.92
    }

    private fun generateNBlocksFallCost(): DoubleArray {
        val timeCost = DoubleArray(257)
        var currentDistance = 0.0
        var targetDistance = 1
        var tickCount = 0

        while (true) {
            val velocityAtTick = downwardMotionAtTick(tickCount)

            if (currentDistance + velocityAtTick >= targetDistance) {
                timeCost[targetDistance] =
                    tickCount + (targetDistance - currentDistance) / velocityAtTick
                targetDistance++
                if (targetDistance > 256) break
                continue
            }

            currentDistance += velocityAtTick
            tickCount++
        }
        return timeCost
    }
}
