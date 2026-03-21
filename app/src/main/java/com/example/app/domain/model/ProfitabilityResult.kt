package com.example.app.domain.model

data class ProfitabilityResult(
    val isWorthIt: Boolean,
    val breakEvenStrompreisEurKwh: Double,
    val currentStrompreisEurKwh: Double,
    val deltaEurKwh: Double,           // positiv = aktuell günstiger als Schwelle
    val expectedBtcPerDay: Double,
    val expectedEurPerDay: Double,
    val stromkostenEurPerDay: Double,
    val heizungskostenOelEurKwh: Double
)
