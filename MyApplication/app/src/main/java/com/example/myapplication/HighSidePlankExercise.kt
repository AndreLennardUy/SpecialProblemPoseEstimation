package com.example.myapplication

class HighSidePlankExercise(private val cameraPage: CameraPage) : Exercise() {

    override fun analyzeKeypoints(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean {
        return isHighSidePlankCorrect(keypoints , confidenceThreshold)
    }

    private fun isHighSidePlankCorrect(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float) : Boolean {
        val angles = analyzeBodyPos(keypoints)
        val leftArmAngle = angles[0]
        val rightArmAngle = angles[1]

        val atLeastOneArmCorrect = leftArmAngle in 160.0f..200.0f || rightArmAngle in 160.0f..200.0f
        val bodyAnglesCorrect = angles[2] in 160.0f..200.0f && angles[3] in 160.0f..200.0f

        val score = if (atLeastOneArmCorrect && bodyAnglesCorrect) 100.0 else 0.0
        setScore(score)

        return atLeastOneArmCorrect && bodyAnglesCorrect
    }
}
