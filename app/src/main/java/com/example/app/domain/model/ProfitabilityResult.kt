package com.example.app.domain.model

data class ProfitabilityResult(
    val isWorthIt: Boolean,
    val breakEvenStrompreisEurKwh: Double,
    val currentStrompreisEurKwh: Double,
    val deltaEurKwh: Double,           // positiv = aktuell günstiger als Schwelle
    val expectedBtcPerDay: Double,
    val expectedEurPerDay: Double,
    val stromkostenEurPerDay: Double,
    val heizungskostenOelEurKwh: Double,
    // Nur für profitable Stunden (strompreis < breakEven)
    val profitableHoursToday: Int,
    val waermeEnergyKwhProfitable: Double, // thermische Energie die in den Puffer geht
    val oilLiterAvoided: Double,           // Liter Heizöl die ersetzt werden
    val oilEurSaved: Double,               // Ersparnis vs. Ölheizung
    val co2KgAvoided: Double               // CO₂-Einsparung (2,68 kg/L Heizöl)
)
