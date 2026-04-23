package atec.btcheater.calculator.domain.usecase

import atec.btcheater.calculator.domain.model.MarketData
import atec.btcheater.calculator.domain.model.MinerConfig
import atec.btcheater.calculator.domain.model.MinerYieldPerKwh
import atec.btcheater.calculator.domain.model.ProfitabilityResult
import atec.btcheater.calculator.domain.model.SaleMode
import javax.inject.Inject

class CalculateProfitabilityUseCase @Inject constructor() {

    operator fun invoke(
        miners: List<MinerConfig>,
        market: MarketData,
        saleMode: SaleMode,
        boilerEfficiency: Double,     // z.B. 0.85
        minerWaermeeffizienz: Double  // z.B. 0.85 — Anteil der Elektrizität der als Wärme im Puffer ankommt
    ): Result<ProfitabilityResult> {
        val activeMiners = miners.filter { it.isActive }
        val totalHashrateThs = activeMiners.sumOf { it.hashrateThs }
        val totalWatt = activeMiners.sumOf { it.watt }

        val btcPrice = when (saleMode) {
            is SaleMode.Instant -> market.btcPriceEur
            is SaleMode.Hodl -> saleMode.targetPriceEur
        }

        if (btcPrice <= 0) return Result.failure(IllegalStateException("BTC-Preis ist 0 oder ungültig"))
        if (market.networkDifficulty <= 0) return Result.failure(IllegalStateException("Network Difficulty ist 0 oder ungültig"))

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

        val kwhPerHour = totalWatt / 1000.0
        val btcPerHour = btcPerDay / 24.0

        // Profitable Stunden mit echten Stundenpreisen
        val profitableHoursList = market.hourlyPrices.filter { it.priceEurKwh < breakEven }
        val profitableHours = profitableHoursList.size
        val stromkostenProfitable = profitableHoursList.sumOf { kwhPerHour * it.priceEurKwh }
        val eurProfitable = btcPerHour * profitableHours * btcPrice
        val nettoProfitable = eurProfitable - stromkostenProfitable

        // Thermische Energie nur für profitable Stunden
        val waermeEnergyKwh = kwhPerHour * profitableHours * minerWaermeeffizienz

        // Heizöl-Äquivalent
        val oilLiter = if (boilerEfficiency > 0) waermeEnergyKwh / (10.0 * boilerEfficiency) else 0.0
        val oilEurSaved = oilLiter * market.heizolPreisEurLiter
        val co2KgAvoided = oilLiter * 2.68

        // Nettovorteil aktuelle Stunde: BTC-Ertrag/h + Ölersparnis/h - Stromkosten/h
        // = delta × kWhPerHour (Break-Even-Delta in absoluten €/h)
        val nettovorteilProStunde = btcPerHour * btcPrice +
                kwhPerHour * minerWaermeeffizienz * heizölKostenEurKwh -
                kwhPerHour * market.currentStrompreisEurKwh

        // Sats & € pro kWh je Miner — immer Live-BTC-Kurs (market.btcPriceEur)
        val liveBtcPrice = market.btcPriceEur
        val perMinerYields = activeMiners.mapNotNull { miner ->
            val kwhPerDayMiner = (miner.watt / 1000.0) * 24.0
            if (kwhPerDayMiner <= 0 || miner.hashrateThs <= 0) return@mapNotNull null
            val hashrateHsMiner = miner.hashrateThs * 1_000_000_000_000.0
            val btcPerDayMiner = (hashrateHsMiner * 86400.0) / (market.networkDifficulty * 4_294_967_296.0) * 3.125
            val sats = ((btcPerDayMiner * 100_000_000.0) / kwhPerDayMiner).toLong()
            val eur = sats * (liveBtcPrice / 100_000_000.0)
            MinerYieldPerKwh(minerId = miner.id, label = miner.label, satsPerKwh = sats, eurPerKwh = eur)
        }

        // Vergleich: 10L Wasser um 10°C erwärmen = 0,1163 kWh Wärmebedarf
        val waterHeatKwh = 10.0 * 4186.0 * 10.0 / 3_600_000.0
        val kostenWasserMiner = if (minerWaermeeffizienz > 0)
            waterHeatKwh / minerWaermeeffizienz * market.currentStrompreisEurKwh else 0.0
        val kostenWasserOel = if (boilerEfficiency > 0)
            waterHeatKwh / (10.0 * boilerEfficiency) * market.heizolPreisEurLiter else 0.0

        return Result.success(
            ProfitabilityResult(
                isWorthIt = delta > 0,
                breakEvenStrompreisEurKwh = breakEven,
                currentStrompreisEurKwh = market.currentStrompreisEurKwh,
                deltaEurKwh = delta,
                expectedBtcPerDay = btcPerDay,
                expectedEurPerDay = eurPerDay,
                heizungskostenOelEurKwh = heizölKostenEurKwh,
                profitableHoursToday = profitableHours,
                stromkostenProfitableEur = stromkostenProfitable,
                eurProfitableHours = eurProfitable,
                nettoProfitableEur = nettoProfitable,
                waermeEnergyKwhProfitable = waermeEnergyKwh,
                oilLiterAvoided = oilLiter,
                oilEurSaved = oilEurSaved,
                co2KgAvoided = co2KgAvoided,
                kostenWasser10L10K_Miner = kostenWasserMiner,
                kostenWasser10L10K_Oel = kostenWasserOel,
                nettovorteilProStunde = nettovorteilProStunde,
                perMinerYields = perMinerYields
            )
        )
    }
}
