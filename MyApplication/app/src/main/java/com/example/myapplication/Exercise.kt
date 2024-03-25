package com.example.myapplication

abstract class  Exercise{
   abstract fun analyzeKeypoints(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean

    // Calculates the angle formed by three points.
    public fun calculateAngle(a: Pair<Float, Float>, b: Pair<Float, Float>, c: Pair<Float, Float>): Float {
        val angleRadians = Math.atan2((c.second - b.second).toDouble(),
            (c.first - b.first).toDouble()
        ) - Math.atan2((a.second - b.second).toDouble(), (a.first - b.first).toDouble())
        var angleDegrees = Math.toDegrees(angleRadians.toDouble()).toFloat()

        // Normalize the angle to a positive value
        angleDegrees = if (angleDegrees < 0) angleDegrees + 360 else angleDegrees

        return angleDegrees
    }
}
