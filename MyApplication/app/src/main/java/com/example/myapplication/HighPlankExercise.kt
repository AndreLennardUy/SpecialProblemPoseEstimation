package com.example.myapplication

// Class that corrects a High Plank implements an Exercise Interface
class HighPlankExercise(private val cameraPage: CameraPage) : Exercise() {
    // Call the Interface Function
    override fun analyzeKeypoints(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean {
        return isHighPlankCorrect(keypoints, confidenceThreshold)
    }

    //Function that give the correct coordinate and score for a perfect High Plank
    private fun isHighPlankCorrect(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean {
        val angles = analyzeBodyPos(keypoints)
        val conditions = listOf(
            angles[0] in 160.0f..180.0f,
            angles[1] in 160.0f..180.0f,
            angles[2] in 130.0f..195.0f,
            angles[3] in 130.0f..195.0f
        )
        val score = calculateConditionsScore(conditions)
        setScore(score);

        val scoreThreshold = 75.0
        return score >= scoreThreshold
    }
}
