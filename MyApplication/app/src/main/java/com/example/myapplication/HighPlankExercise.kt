package com.example.myapplication

// Class that corrects a High Plank implements an Exercise Interface
class HighPlankExercise(private val cameraPage: CameraPage) : Exercise {
    // Call the Interface Function
    override fun analyzeKeypoints(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean {
        return isHighPlankCorrect(keypoints, confidenceThreshold)
    }

    //Function that give the correct coordinate for a perfect High Plank
    private fun isHighPlankCorrect(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean {
        val angles = calculateHighPlankAngles(keypoints)
        val (leftArmAngle, rightArmAngle, leftBodyAngle, rightBodyAngle) = angles

        // MODIFY HERE TO HAVE THE POSTURE COORDINATES
        val isArmsStraight = leftArmAngle in 160.0f..180.0f && rightArmAngle in 160.0f..180.0f
        val isBodyAligned = leftBodyAngle in 130.0f..195.0f && rightBodyAngle in 130.0f..195.0f

        return isArmsStraight && isBodyAligned
    }

    // Calculates angles relevant to determining the correctness of the high plank position.
    private fun calculateHighPlankAngles(keypoints: Array<Pair<Float, Float>>): List<Float> {
        // Default angles in case of insufficient keypoints. These could be set to indicate an error.
        val defaultAngle = -1f

        // Ensure the keypoints array contains enough elements.
        if (keypoints.size < 17) {
            // Return a list of default angles indicating an error or unexpected condition.
            return List(4) { defaultAngle }
        }

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

        // Return the calculated angles.
        return listOf(leftArmAngle, rightArmAngle, leftBodyAngle, rightBodyAngle)
    }

    // Calculates the angle formed by three points.
    private fun calculateAngle(a: Pair<Float, Float>, b: Pair<Float, Float>, c: Pair<Float, Float>): Float {
        val angleRadians = Math.atan2((c.second - b.second).toDouble(),
            (c.first - b.first).toDouble()
        ) - Math.atan2((a.second - b.second).toDouble(), (a.first - b.first).toDouble())
        var angleDegrees = Math.toDegrees(angleRadians.toDouble()).toFloat()

        // Normalize the angle to a positive value
        angleDegrees = if (angleDegrees < 0) angleDegrees + 360 else angleDegrees

        return angleDegrees
    }
}
