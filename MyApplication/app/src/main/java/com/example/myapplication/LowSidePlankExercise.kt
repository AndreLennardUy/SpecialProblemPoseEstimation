package com.example.myapplication

class LowSidePlankExercise(private val cameraPage: CameraPage) : Exercise() {

    override fun analyzeKeypoints(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean {
       return isLowSidePlankCorrect(keypoints , confidenceThreshold)
    }

    private fun isLowSidePlankCorrect(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float) : Boolean {
        val angles = analyzeBodyPos(keypoints)
        val leftArmAngle = angles[0]
        val rightArmAngle = angles[1]

        val oneArmRightAngle = leftArmAngle in 30.0f..140.0f || rightArmAngle in 30.0f..140.0f
        val bodyAnglesCorrect = angles[2] in 130.0f..180.0f && angles[3] in 130.0f..180.0f

        val score = if (oneArmRightAngle && bodyAnglesCorrect) 100.0 else 0.0
        setScore(score)

        return oneArmRightAngle && bodyAnglesCorrect
    }
}