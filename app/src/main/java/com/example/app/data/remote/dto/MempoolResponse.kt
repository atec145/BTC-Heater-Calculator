package com.example.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MempoolDifficultyResponse(
    @SerializedName("difficulty") val difficulty: Double,
    @SerializedName("progressPercent") val progressPercent: Double,
    @SerializedName("difficultyChange") val difficultyChange: Double
)
