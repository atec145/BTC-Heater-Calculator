package atec.btcheater.calculator.domain.model

data class MarketData(
    val currentStrompreisEurKwh: Double,
    val hourlyPrices: List<HourlyPrice>,
    val btcPriceEur: Double,
    val networkDifficulty: Double,
    val heizolPreisEurLiter: Double,   // manuell oder API
    val lastUpdated: Long              // Unix timestamp seconds
)
