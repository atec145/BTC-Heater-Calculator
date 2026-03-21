package com.example.app.domain.model

data class HourlyPrice(
    val epochStart: Long,   // Unix timestamp seconds
    val priceEurKwh: Double
)
