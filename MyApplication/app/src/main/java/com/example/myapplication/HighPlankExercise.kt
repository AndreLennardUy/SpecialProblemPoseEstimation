package com.example.myapplication

// Class that corrects a High Plank implements an Exercise Interface
class HighPlankExercise(private val cameraPage: CameraPage) : Exercise() {
    // Call the Interface Function
    override fun analyzeKeypoints(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean {
        return isHighPlankCorrect(keypoints, confidenceThreshold)
    }

    //Function that give the correct coordinate for a perfect High Plank
    private fun isHighPlankCorrect(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean {
        val angles = analyzeBodyPos(keypoints)
        val (leftArmAngle, rightArmAngle, leftBodyAngle, rightBodyAngle) = angles

        // MODIFY HERE TO HAVE THE POSTURE COORDINATES
        val isArmsStraight = leftArmAngle in 160.0f..180.0f && rightArmAngle in 160.0f..180.0f
        val isBodyAligned = leftBodyAngle in 130.0f..195.0f && rightBodyAngle in 130.0f..195.0f

        return isArmsStraight && isBodyAligned
    }
}
