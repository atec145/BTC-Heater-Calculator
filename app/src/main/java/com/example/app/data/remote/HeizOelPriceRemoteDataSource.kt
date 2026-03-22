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
        // API liefert Preis in €/100 Liter (z.B. 180.0 = 180€/100L = 1.80€/L)
        history.lastOrNull()?.price?.let { if (it > 0) it / 100.0 else null }
    } catch (e: Exception) {
        null
    }
}
