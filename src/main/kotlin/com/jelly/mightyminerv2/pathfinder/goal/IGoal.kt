package com.jelly.mightyminerv2.pathfinder.goal

interface IGoal {
    fun isAtGoal(x: Int, y: Int, z: Int): Boolean
    fun heuristic(x: Int, y: Int, z: Int): Double
}