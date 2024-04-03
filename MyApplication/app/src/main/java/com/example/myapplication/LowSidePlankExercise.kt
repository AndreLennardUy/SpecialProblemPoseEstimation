package com.example.myapplication

class LowSidePlankExercise(private val cameraPage: CameraPage) : Exercise() {

    override fun analyzeKeypoints(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean {
       return isLowSidePlankCorrect(keypoints , confidenceThreshold)
    }

    private fun isLowSidePlankCorrect(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float) : Boolean {
        val angles = analyzeBodyPos(keypoints)
        val conditions = listOf(
            angles[0] in 70.0f..110.0f,
            angles[1] in 130.0f..210.0f,
            angles[2] in 70.0f..110.0f,
            angles[3] in 130.0f..210.0f
        )

        val score = calculateConditionsScore(conditions)
        setScore(score);

        val scoreThreshold = 75.0
        return score >= scoreThreshold
    }
}