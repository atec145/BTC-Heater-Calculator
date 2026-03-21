package com.example.app.domain.usecase

import com.example.app.domain.model.MarketData
import com.example.app.domain.model.MinerConfig
import com.example.app.domain.model.ProfitabilityResult
import com.example.app.domain.model.SaleMode
import javax.inject.Inject

class CalculateProfitabilityUseCase @Inject constructor() {

    operator fun invoke(
        miners: List<MinerConfig>,
        market: MarketData,
        saleMode: SaleMode,
        boilerEfficiency: Double // z.B. 0.85
    ): ProfitabilityResult {
        val activeMiners = miners.filter { it.isActive }
        val totalHashrateThs = activeMiners.sumOf { it.hashrateThs }
        val totalWatt = activeMiners.sumOf { it.watt }

        val btcPrice = when (saleMode) {
            is SaleMode.Instant -> market.btcPriceEur
            is SaleMode.Hodl -> saleMode.targetPriceEur
        }

        // tägl. BTC = (Hashrate in H/s × 86400) / (Difficulty × 2^32) × 3.125
        val hashrateHs = totalHashrateThs * 1_000_000_000_000.0
        val btcPerDay = (hashrateHs * 86400.0) / (market.networkDifficulty * 4_294_967_296.0) * 3.125
        val eurPerDay = btcPerDay * btcPrice

        // Stromkosten pro Tag
        val kwhPerDay = (totalWatt / 1000.0) * 24.0
        val stromkostenPerDay = kwhPerDay * market.currentStrompreisEurKwh

        // Ölheizung: Kosten pro kWh Wärme
        val heizölKostenEurKwh = market.heizolPreisEurLiter / (10.0 * boilerEfficiency)

        // Break-Even Strompreis: Mining-Einnahmen pro kWh ≥ Ölheizungskosten
        // eurPerDay / kwhPerDay = Mining-Ertrag pro kWh Strom
        // Schwelle: Mining-Ertrag/kWh = Ölkosten/kWh → Strom kostet maximal X
        // eurPerDay - stromkostenPerDay ≥ heizölKostenEurKwh * kwhPerDay - kwhPerDay * strompreis
        // vereinfacht: breakEven = (eurPerDay/kwhPerDay) - heizölKostenEurKwh + heizölKostenEurKwh
        // Schwelle: Strompreis bei dem Mining-Nettoertrag = 0 vs Ölheizung
        // Mining lohnt wenn: eurPerDay > kwhPerDay * strompreis + kwhPerDay * heizölKostenEurKwh - kwhPerDay * heizölKostenEurKwh
        // Eigentliche Formel: Mining günstiger als Öl wenn Strompreis < (eurPerDay/kwhPerDay)
        // Aber Öl hat auch Kosten: Heizung mit Öl kostet heizölKostenEurKwh pro kWh
        // Break-Even: eurPerDay/kwhPerDay + heizölKostenEurKwh = Strompreis (dann ist Mining + Heizen gleich teuer wie nur Öl heizen)
        val breakEven = if (kwhPerDay > 0) (eurPerDay / kwhPerDay) + heizölKostenEurKwh else 0.0
        val delta = breakEven - market.currentStrompreisEurKwh

        return ProfitabilityResult(
            isWorthIt = delta > 0,
            breakEvenStrompreisEurKwh = breakEven,
            currentStrompreisEurKwh = market.currentStrompreisEurKwh,
            deltaEurKwh = delta,
            expectedBtcPerDay = btcPerDay,
            expectedEurPerDay = eurPerDay,
            stromkostenEurPerDay = stromkostenPerDay,
            heizungskostenOelEurKwh = heizölKostenEurKwh
        )
    }
}
