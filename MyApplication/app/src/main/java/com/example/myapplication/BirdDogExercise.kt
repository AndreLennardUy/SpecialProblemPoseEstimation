package com.example.myapplication

// Class that corrects a Bird Dog Exercise and extends from the Exercise abstract class
class BirdDogExercise(private val cameraPage: CameraPage) : Exercise() {
    // Call the abstract method from Exercise class
    override fun analyzeKeypoints(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean {
        return isBirdDogCorrect(keypoints, confidenceThreshold)
    }

    //Function that checks for the correct position in the Bird Dog exercise
    private fun isBirdDogCorrect(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean {
        val angles = analyzeBodyPos(keypoints)

        // Determine which arm and leg are straight based on confidence scores
        val isLeftArmRightLegStraight = angles[0] in 160.0f..210.0f && angles[3] in 160.0f..210.0f
        val isRightArmLeftLegStraight = angles[1] in 160.0f..210.0f && angles[2] in 160.0f..210.0f

        val conditions = listOf(
            isLeftArmRightLegStraight || isRightArmLeftLegStraight,
            if(isLeftArmRightLegStraight) angles[2] in 60.0f..120.0f else angles[3] in 60.0f..120.0f
        )

        val score = calculateConditionsScore(conditions)
        setScore(score)

        val scoreThreshold = 75.0
        return score >= scoreThreshold
    }
}
