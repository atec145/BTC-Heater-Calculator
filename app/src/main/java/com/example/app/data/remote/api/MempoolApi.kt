package com.example.app.data.remote.api

import com.example.app.data.remote.dto.MempoolDifficultyResponse
import retrofit2.http.GET

interface MempoolApi {
    @GET("api/v1/difficulty-adjustment")
    suspend fun getDifficultyAdjustment(): MempoolDifficultyResponse
}
