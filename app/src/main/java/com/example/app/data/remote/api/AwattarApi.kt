package com.example.app.data.remote.api

import com.example.app.data.remote.dto.AwattarResponse
import retrofit2.http.GET

interface AwattarApi {
    @GET("v1/marketdata")
    suspend fun getMarketData(): AwattarResponse
}
