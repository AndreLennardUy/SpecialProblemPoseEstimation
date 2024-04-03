package com.example.myapplication

class HighSidePlankExercise(private val cameraPage: CameraPage) : Exercise() {

    override fun analyzeKeypoints(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean {
        return isHighSidePlankCorrect(keypoints , confidenceThreshold)
    }

    private fun isHighSidePlankCorrect(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float) : Boolean {
        val angles = analyzeBodyPos(keypoints)
        val conditions = listOf(
            angles[0] in 160.0f..200.0f,
            angles[1] in 160.0f..200.0f,
            angles[2] in 160.0f..200.0f,
            angles[3] in 160.0f..200.0f
        )

        val score = calculateConditionsScore(conditions)
        setScore(score);

        val scoreThreshold = 75.0
        return score >= scoreThreshold
    }
}
