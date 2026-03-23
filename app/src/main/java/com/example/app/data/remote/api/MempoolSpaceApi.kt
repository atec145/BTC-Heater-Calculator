package com.example.app.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query

interface MempoolSpaceApi {
    // Gibt Array von Arrays zurück: [timestamp, blockHeight, difficulty, changePercent]
    @GET("api/v1/mining/difficulty-adjustments")
    suspend fun getDifficultyAdjustments(
        @Query("interval") interval: String = "all"
    ): List<List<Double>>
}
