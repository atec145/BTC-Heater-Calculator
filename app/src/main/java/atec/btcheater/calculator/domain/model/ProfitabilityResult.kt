package atec.btcheater.calculator.domain.model

data class ProfitabilityResult(
    val isWorthIt: Boolean,
    val breakEvenStrompreisEurKwh: Double,
    val currentStrompreisEurKwh: Double,
    val deltaEurKwh: Double,               // positiv = aktuell günstiger als Schwelle
    val expectedBtcPerDay: Double,         // theoretisch 24h
    val expectedEurPerDay: Double,         // theoretisch 24h
    val heizungskostenOelEurKwh: Double,
    // Nur für profitable Stunden (strompreis < breakEven)
    val profitableHoursToday: Int,
    val stromkostenProfitableEur: Double,  // tatsächliche Stromkosten in profitablen Stunden
    val eurProfitableHours: Double,        // BTC-Ertrag in profitablen Stunden
    val nettoProfitableEur: Double,        // Nettoertrag in profitablen Stunden
    val waermeEnergyKwhProfitable: Double,
    val oilLiterAvoided: Double,
    val oilEurSaved: Double,
    val co2KgAvoided: Double,
    // Vergleich: 10L Wasser um 10°C erwärmen
    val kostenWasser10L10K_Miner: Double,
    val kostenWasser10L10K_Oel: Double,
    // Gesamtvorteil aktueller Stunde: BTC-Ertrag + Ölersparnis - Stromkosten
    // Positiv = Miner günstiger als Öl, negativ = Öl günstiger
    val nettovorteilProStunde: Double,
    // Ertrag pro kWh je Miner (immer Live-BTC-Kurs)
    val perMinerYields: List<MinerYieldPerKwh>
)
