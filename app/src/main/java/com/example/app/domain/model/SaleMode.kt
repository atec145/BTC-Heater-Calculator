package com.example.app.domain.model

sealed class SaleMode {
    data object Instant : SaleMode()
    data class Hodl(val targetPriceEur: Double) : SaleMode()
}
