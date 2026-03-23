package com.example.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HeizOel24Response(
    @SerializedName("CurrentPrice") val currentPrice: Double = 0.0
)
