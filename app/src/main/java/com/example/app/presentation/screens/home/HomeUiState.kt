package com.example.app.presentation.screens.home

import com.example.app.domain.model.HourlyPrice
import com.example.app.domain.model.MinerConfig
import com.example.app.domain.model.ProfitabilityResult
import com.example.app.domain.model.SaleMode

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val result: ProfitabilityResult? = null,
    val hourlyPrices: List<HourlyPrice> = emptyList(),
    val activeMiners: List<MinerConfig> = emptyList(),
    val saleMode: SaleMode = SaleMode.Instant,
    val btcPriceEur: Double = 0.0,
    val lastUpdated: String = ""
)
