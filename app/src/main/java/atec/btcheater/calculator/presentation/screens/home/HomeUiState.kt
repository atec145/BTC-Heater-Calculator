package atec.btcheater.calculator.presentation.screens.home

import atec.btcheater.calculator.domain.model.HourlyPrice
import atec.btcheater.calculator.domain.model.MinerConfig
import atec.btcheater.calculator.domain.model.ProfitabilityResult
import atec.btcheater.calculator.domain.model.SaleMode

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
