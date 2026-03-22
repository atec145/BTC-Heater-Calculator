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
        val history = api.getPriceHistory(
            countryId = 1,
            minDate = weekAgo,
            maxDate = today
        )
        history.lastOrNull()?.price?.takeIf { it > 0 }
    } catch (e: Exception) {
        null
    }
}
