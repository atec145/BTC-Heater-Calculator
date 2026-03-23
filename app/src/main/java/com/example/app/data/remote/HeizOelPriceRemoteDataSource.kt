package com.example.app.data.remote

import com.example.app.data.remote.api.HeizOel24Api
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class HeizOelPriceRemoteDataSource @Inject constructor(
    private val api: HeizOel24Api
) {
    suspend fun fetchCurrentPrice(): Double? = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = sdf.format(Date())
        val weekAgo = sdf.format(Date(System.currentTimeMillis() - 7 * 24 * 3600 * 1000L))
        val response = api.getPriceHistory(
            countryId = 2,
            minDate = weekAgo,
            maxDate = today
        )
        // API liefert CurrentPrice in €/100 Liter (z.B. 145.6 = 145.6€/100L = 1.456€/L)
        if (response.currentPrice > 0) response.currentPrice / 100.0 else null
    } catch (e: Exception) {
        null
    }
}
