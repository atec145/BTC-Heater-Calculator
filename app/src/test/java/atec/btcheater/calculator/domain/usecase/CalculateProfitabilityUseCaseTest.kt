package atec.btcheater.calculator.domain.usecase

import atec.btcheater.calculator.domain.model.HourlyPrice
import atec.btcheater.calculator.domain.model.MarketData
import atec.btcheater.calculator.domain.model.MinerConfig
import atec.btcheater.calculator.domain.model.SaleMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateProfitabilityUseCaseTest {

    private lateinit var useCase: CalculateProfitabilityUseCase

    private val testMiner = MinerConfig(
        id = 1, label = "S19 Pro", hashrateThs = 110.0, watt = 3250.0, isActive = true
    )

    private val testMarket = MarketData(
        currentStrompreisEurKwh = 0.10,
        hourlyPrices = emptyList(),
        btcPriceEur = 85_000.0,
        networkDifficulty = 113_757_508_167_373.0,
        heizolPreisEurLiter = 1.10,
        lastUpdated = 0L
    )

    @Before
    fun setUp() {
        useCase = CalculateProfitabilityUseCase()
    }

    @Test
    fun `returns failure when btcPrice is zero`() {
        val result = useCase(
            miners = listOf(testMiner),
            market = testMarket.copy(btcPriceEur = 0.0),
            saleMode = SaleMode.Instant,
            boilerEfficiency = 0.85,
            minerWaermeeffizienz = 0.85
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("BTC") == true)
    }

    @Test
    fun `returns failure when difficulty is zero`() {
        val result = useCase(
            miners = listOf(testMiner),
            market = testMarket.copy(networkDifficulty = 0.0),
            saleMode = SaleMode.Instant,
            boilerEfficiency = 0.85,
            minerWaermeeffizienz = 0.85
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Difficulty") == true)
    }

    @Test
    fun `oil cost formula is correct`() {
        // Heizkosten Öl = Preis / (10 * Wirkungsgrad) = 1.10 / (10 * 0.85) = 0.12941...
        val result = useCase(listOf(testMiner), testMarket, SaleMode.Instant, 0.85, 0.85)
        val expected = 1.10 / (10.0 * 0.85)
        assertEquals(expected, result.getOrThrow().heizungskostenOelEurKwh, 0.0001)
    }

    @Test
    fun `btc per day formula is correct`() {
        // tägl. BTC = (hashrate_Hs * 86400) / (difficulty * 2^32) * 3.125
        val result = useCase(listOf(testMiner), testMarket, SaleMode.Instant, 0.85, 0.85)
        val hashrateHs = 110.0 * 1_000_000_000_000.0
        val expected = (hashrateHs * 86400.0) / (testMarket.networkDifficulty * 4_294_967_296.0) * 3.125
        assertEquals(expected, result.getOrThrow().expectedBtcPerDay, 0.0000001)
    }

    @Test
    fun `hodl mode uses target price instead of market price`() {
        val hodlTarget = 120_000.0
        val instant = useCase(listOf(testMiner), testMarket, SaleMode.Instant, 0.85, 0.85).getOrThrow()
        val hodl = useCase(listOf(testMiner), testMarket, SaleMode.Hodl(hodlTarget), 0.85, 0.85).getOrThrow()

        assertTrue(hodl.expectedEurPerDay > instant.expectedEurPerDay)
        assertEquals(hodlTarget / testMarket.btcPriceEur, hodl.expectedEurPerDay / instant.expectedEurPerDay, 0.001)
    }

    @Test
    fun `inactive miners are excluded from calculation`() {
        val inactiveMiner = testMiner.copy(id = 2, isActive = false)
        val resultBoth = useCase(listOf(testMiner, inactiveMiner), testMarket, SaleMode.Instant, 0.85, 0.85).getOrThrow()
        val resultOne = useCase(listOf(testMiner), testMarket, SaleMode.Instant, 0.85, 0.85).getOrThrow()

        assertEquals(resultOne.expectedBtcPerDay, resultBoth.expectedBtcPerDay, 0.0000001)
    }

    @Test
    fun `isWorthIt is true when strompreis below break-even`() {
        val cheapMarket = testMarket.copy(currentStrompreisEurKwh = 0.01)
        val result = useCase(listOf(testMiner), cheapMarket, SaleMode.Instant, 0.85, 0.85).getOrThrow()
        assertTrue(result.isWorthIt)
        assertTrue(result.deltaEurKwh > 0)
    }

    @Test
    fun `isWorthIt is false when strompreis above break-even`() {
        val expensiveMarket = testMarket.copy(currentStrompreisEurKwh = 1.00)
        val result = useCase(listOf(testMiner), expensiveMarket, SaleMode.Instant, 0.85, 0.85).getOrThrow()
        assertFalse(result.isWorthIt)
        assertTrue(result.deltaEurKwh < 0)
    }

    @Test
    fun `two active miners double the output`() {
        val miner2 = testMiner.copy(id = 2)
        val single = useCase(listOf(testMiner), testMarket, SaleMode.Instant, 0.85, 0.85).getOrThrow()
        val dual = useCase(listOf(testMiner, miner2), testMarket, SaleMode.Instant, 0.85, 0.85).getOrThrow()

        assertEquals(single.expectedBtcPerDay * 2, dual.expectedBtcPerDay, 0.0000001)
    }

    // --- Neue Tests: profitable Stunden & Wärme-Berechnung ---

    @Test
    fun `profitableHoursToday counts only hours below break-even`() {
        // 3 günstige Stunden (0.05 €/kWh), 3 teure Stunden (0.90 €/kWh)
        // Break-even liegt je nach Difficulty irgendwo dazwischen
        val prices = listOf(
            HourlyPrice(epochStart = 0, priceEurKwh = 0.05),
            HourlyPrice(epochStart = 3600, priceEurKwh = 0.05),
            HourlyPrice(epochStart = 7200, priceEurKwh = 0.05),
            HourlyPrice(epochStart = 10800, priceEurKwh = 0.90),
            HourlyPrice(epochStart = 14400, priceEurKwh = 0.90),
            HourlyPrice(epochStart = 18000, priceEurKwh = 0.90)
        )
        val market = testMarket.copy(hourlyPrices = prices)
        val result = useCase(listOf(testMiner), market, SaleMode.Instant, 0.85, 0.85).getOrThrow()

        // Break-even ist abhängig von BTC-Preis und Difficulty — prüfen ob 3 günstige Stunden gezählt werden
        // Da 0.05 < breakEven < 0.90 sein muss bei diesen Parametern:
        assertTrue(result.breakEvenStrompreisEurKwh > 0.05)
        assertTrue(result.breakEvenStrompreisEurKwh < 0.90)
        assertEquals(3, result.profitableHoursToday)
    }

    @Test
    fun `zero profitable hours when all prices above break-even`() {
        val prices = listOf(
            HourlyPrice(epochStart = 0, priceEurKwh = 0.99),
            HourlyPrice(epochStart = 3600, priceEurKwh = 0.99)
        )
        val result = useCase(listOf(testMiner), testMarket.copy(hourlyPrices = prices), SaleMode.Instant, 0.85, 0.85).getOrThrow()
        assertEquals(0, result.profitableHoursToday)
        assertEquals(0.0, result.waermeEnergyKwhProfitable, 0.001)
        assertEquals(0.0, result.oilLiterAvoided, 0.001)
        assertEquals(0.0, result.co2KgAvoided, 0.001)
    }

    @Test
    fun `waerme energy calculation is correct`() {
        // 2 profitable Stunden, Miner 3250 W, Wärmeeffizienz 80 %
        // kWh = 3.25 kW × 2 h × 0.80 = 5.2 kWh
        val prices = listOf(
            HourlyPrice(epochStart = 0, priceEurKwh = 0.05),
            HourlyPrice(epochStart = 3600, priceEurKwh = 0.05)
        )
        val result = useCase(listOf(testMiner), testMarket.copy(hourlyPrices = prices), SaleMode.Instant, 0.85, 0.80).getOrThrow()
        val expected = (3250.0 / 1000.0) * 2 * 0.80
        assertEquals(expected, result.waermeEnergyKwhProfitable, 0.001)
    }

    @Test
    fun `oil liter avoided calculation is correct`() {
        // 5.2 kWh Wärme / (10 kWh/L × 0.85) = 0.6118 L
        val prices = listOf(
            HourlyPrice(epochStart = 0, priceEurKwh = 0.05),
            HourlyPrice(epochStart = 3600, priceEurKwh = 0.05)
        )
        val result = useCase(listOf(testMiner), testMarket.copy(hourlyPrices = prices), SaleMode.Instant, 0.85, 0.80).getOrThrow()
        val waerme = (3250.0 / 1000.0) * 2 * 0.80
        val expectedLiter = waerme / (10.0 * 0.85)
        assertEquals(expectedLiter, result.oilLiterAvoided, 0.001)
    }

    @Test
    fun `co2 savings factor is 2_68 kg per liter`() {
        val prices = listOf(HourlyPrice(epochStart = 0, priceEurKwh = 0.05))
        val result = useCase(listOf(testMiner), testMarket.copy(hourlyPrices = prices), SaleMode.Instant, 0.85, 0.85).getOrThrow()
        assertEquals(result.oilLiterAvoided * 2.68, result.co2KgAvoided, 0.0001)
    }

    @Test
    fun `oil eur saved equals liter times oil price`() {
        val prices = listOf(HourlyPrice(epochStart = 0, priceEurKwh = 0.05))
        val result = useCase(listOf(testMiner), testMarket.copy(hourlyPrices = prices), SaleMode.Instant, 0.85, 0.85).getOrThrow()
        assertEquals(result.oilLiterAvoided * testMarket.heizolPreisEurLiter, result.oilEurSaved, 0.001)
    }

    @Test
    fun `no hourly prices returns zero profitable hours`() {
        val result = useCase(listOf(testMiner), testMarket.copy(hourlyPrices = emptyList()), SaleMode.Instant, 0.85, 0.85).getOrThrow()
        assertEquals(0, result.profitableHoursToday)
        assertEquals(0.0, result.waermeEnergyKwhProfitable, 0.001)
    }

    @Test
    fun `stromkosten profitable uses actual hourly prices`() {
        // Stunde 1: 0.05 €/kWh, Stunde 2: 0.06 €/kWh — beide profitable
        // Kosten = 3.25 kW × (0.05 + 0.06) = 0.3575 €
        val prices = listOf(
            HourlyPrice(epochStart = 0, priceEurKwh = 0.05),
            HourlyPrice(epochStart = 3600, priceEurKwh = 0.06)
        )
        val result = useCase(listOf(testMiner), testMarket.copy(hourlyPrices = prices), SaleMode.Instant, 0.85, 0.85).getOrThrow()
        val expected = (3250.0 / 1000.0) * (0.05 + 0.06)
        assertEquals(expected, result.stromkostenProfitableEur, 0.001)
    }

    @Test
    fun `perMinerYields contains one entry per active miner`() {
        val miner2 = testMiner.copy(id = 2, label = "S19j", hashrateThs = 90.0, watt = 3050.0)
        val result = useCase(listOf(testMiner, miner2), testMarket, SaleMode.Instant, 0.85, 0.85).getOrThrow()
        assertEquals(2, result.perMinerYields.size)
        assertEquals(testMiner.id, result.perMinerYields[0].minerId)
        assertEquals(miner2.id, result.perMinerYields[1].minerId)
    }

    @Test
    fun `perMinerYields always uses live btc price regardless of hodl mode`() {
        val hodlTarget = 200_000.0
        val instantResult = useCase(listOf(testMiner), testMarket, SaleMode.Instant, 0.85, 0.85).getOrThrow()
        val hodlResult = useCase(listOf(testMiner), testMarket, SaleMode.Hodl(hodlTarget), 0.85, 0.85).getOrThrow()
        assertEquals(instantResult.perMinerYields[0].satsPerKwh, hodlResult.perMinerYields[0].satsPerKwh)
        assertEquals(instantResult.perMinerYields[0].eurPerKwh, hodlResult.perMinerYields[0].eurPerKwh, 0.0001)
    }

    @Test
    fun `perMinerYields sats per kwh formula is correct`() {
        val result = useCase(listOf(testMiner), testMarket, SaleMode.Instant, 0.85, 0.85).getOrThrow()
        val kwhPerDay = (testMiner.watt / 1000.0) * 24.0
        val hashrateHs = testMiner.hashrateThs * 1_000_000_000_000.0
        val btcPerDay = (hashrateHs * 86400.0) / (testMarket.networkDifficulty * 4_294_967_296.0) * 3.125
        val expectedSats = ((btcPerDay * 100_000_000.0) / kwhPerDay).toLong()
        assertEquals(expectedSats, result.perMinerYields[0].satsPerKwh)
    }

    @Test
    fun `perMinerYields inactive miner is excluded`() {
        val inactive = testMiner.copy(id = 2, isActive = false)
        val result = useCase(listOf(testMiner, inactive), testMarket, SaleMode.Instant, 0.85, 0.85).getOrThrow()
        assertEquals(1, result.perMinerYields.size)
        assertEquals(testMiner.id, result.perMinerYields[0].minerId)
    }

    @Test
    fun `wasser vergleich physik korrekt`() {
        // Q = 10 kg × 4186 J/kg/K × 10 K = 418600 J = 0.1163 kWh
        // Miner: 0.1163 / 0.85 × 0.10 = 0.01368 €
        // Öl:    0.1163 / (10 × 0.85) × 1.10 = 0.01505 €
        val result = useCase(listOf(testMiner), testMarket, SaleMode.Instant, 0.85, 0.85).getOrThrow()
        val waterKwh = 10.0 * 4186.0 * 10.0 / 3_600_000.0
        val expectedMiner = waterKwh / 0.85 * 0.10
        val expectedOel = waterKwh / (10.0 * 0.85) * 1.10
        assertEquals(expectedMiner, result.kostenWasser10L10K_Miner, 0.0001)
        assertEquals(expectedOel, result.kostenWasser10L10K_Oel, 0.0001)
    }
}
