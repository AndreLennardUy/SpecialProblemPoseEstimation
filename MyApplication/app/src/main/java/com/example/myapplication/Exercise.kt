package com.example.myapplication

interface Exercise {
    fun analyzeKeypoints(keypoints: Array<Pair<Float, Float>>, confidenceThreshold: Float): Boolean
}
