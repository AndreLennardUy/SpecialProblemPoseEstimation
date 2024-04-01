package com.example.myapplication

class HighSidePlankExercise(private val cameraPage: CameraPage) : Exercise() {

    override fun analyzeKeypoints(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean {
        return isHighSidePlankCorrect(keypoints , confidenceThreshold)
    }

    private fun isHighSidePlankCorrect(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float) : Boolean {
        val angles = analyzeBodyPos(keypoints)
        val (leftArmAngle , leftBodyAngle , rightArmAngle , rightBodyAngle) = angles

        val isLeftArmStraight = leftArmAngle in 160.0..200.0
        val isLeftBodyAligned = leftBodyAngle in 160.0..200.0

        val isRightArmStraight = rightArmAngle in 160.0..200.0
        val isRightBodyAligned = rightBodyAngle in 160.0..200.0

        return (isLeftArmStraight and isLeftBodyAligned) or (isRightArmStraight and isRightBodyAligned)
    }
}
