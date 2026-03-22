package com.example.app.data.remote.api

import com.example.app.data.remote.dto.HeizOel24PricePoint
import retrofit2.http.GET
import retrofit2.http.Query

interface HeizOel24Api {
    @GET("api/chartapi/GetAveragePriceHistory")
    suspend fun getPriceHistory(
        @Query("countryId") countryId: Int,
        @Query("minDate") minDate: String,
        @Query("maxDate") maxDate: String
    ): List<HeizOel24PricePoint>
}
