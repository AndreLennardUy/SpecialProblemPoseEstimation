package com.example.myapplication

abstract class  Exercise{

    private var score = 0.00;

    public fun setScore(score: Double){
        this.score = score;
    }

    public fun getScore(): Double{
        return this.score;
    }
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

    // Calculate all the angles needed for the base analysis.
    protected fun calculateAllAngles(keypoints: Array<Pair<Float, Float>>): List<Float> {
        val leftWrist = keypoints[9]
        val leftElbow = keypoints[7]
        val leftShoulder = keypoints[5]
        val rightWrist = keypoints[10]
        val rightElbow = keypoints[8]
        val rightShoulder = keypoints[6]
        val leftHip = keypoints[11]
        val leftKnee = keypoints[13]
        val rightHip = keypoints[12]
        val rightKnee = keypoints[14]

        // Calculate angles, assuming calculateAngle can handle all input correctly.
        val leftArmAngle = calculateAngle(leftWrist, leftElbow, leftShoulder)
        val rightArmAngle = calculateAngle(rightWrist, rightElbow, rightShoulder)
        val leftBodyAngle = calculateAngle(leftShoulder, leftHip, leftKnee)
        val rightBodyAngle = calculateAngle(rightShoulder, rightHip, rightKnee)

        return listOf(leftArmAngle, rightArmAngle, leftBodyAngle, rightBodyAngle)
    }

    // Analyzes the body position for the basic exercise
    // Accept a list of triples representing indices for additional angles.
    public open fun analyzeBodyPos(
        keypoints: Array<Pair<Float, Float>>,
        additionalAngleIndices: List<Triple<Int, Int, Int>> = emptyList() // Default to no additional angles
    ): List<Float> {
        // Ensure the keypoints array contains enough elements.
        if (keypoints.size < 17) {
            val defaultAngle = -1f
            // Return a list of default angles plus a space for each additional angle requested.
            return List(4 + additionalAngleIndices.size) { defaultAngle }
        }

        // Calculate the base angles.
        val baseAngles = calculateAllAngles(keypoints)

        // Calculate additional angles.
        val additionalAngles = additionalAngleIndices.map { (first, mid, last) ->
            calculateAngle(keypoints[first], keypoints[mid], keypoints[last])
        }

        // Return the combined list of base and additional angles.
        return baseAngles + additionalAngles
    }

    fun calculateConditionsScore(conditions: List<Boolean>): Double {
        val conditionsMet = conditions.count { it }
        return if (conditions.isNotEmpty()) {
            (conditionsMet.toDouble() / conditions.size) * 100
        } else {
            0.0
        }
    }
}
