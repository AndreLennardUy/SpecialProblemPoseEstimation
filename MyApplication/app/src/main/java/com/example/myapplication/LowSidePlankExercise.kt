package com.example.myapplication

class LowSidePlankExercise(private val cameraPage: CameraPage) : Exercise() {

    override fun analyzeKeypoints(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean {
       return isLowSidePlankCorrect(keypoints , confidenceThreshold)
    }

    private fun isLowSidePlankCorrect(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float) : Boolean {
        val angles = analyzeBodyPos(keypoints)
        val (leftArmAngle , leftBodyAngle , rightArmAngle , rightBodyAngle) = angles

        val isLeftArmStraight = leftArmAngle in 70.0..110.0
        val isLeftBodyAligned = leftBodyAngle in 130.0..210.0

        val isRightArmStraight = rightArmAngle in 70.0..110.0
        val isRightBodyAligned = rightBodyAngle in 130.0..210.0

        return (isLeftArmStraight and isLeftBodyAligned) or (isRightArmStraight and isRightBodyAligned)
    }
}