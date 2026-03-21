package com.example.app.domain.usecase

import com.example.app.domain.model.MarketData
import com.example.app.domain.model.MinerConfig
import com.example.app.domain.model.SaleMode
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
        networkDifficulty = 113_757_508_167_373.0, // aktuelle Difficulty ca. 113 T
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
            boilerEfficiency = 0.85
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
            boilerEfficiency = 0.85
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Difficulty") == true)
    }

    @Test
    fun `oil cost formula is correct`() {
        // Heizkosten Öl = Preis / (10 * Wirkungsgrad) = 1.10 / (10 * 0.85) = 0.12941...
        val result = useCase(listOf(testMiner), testMarket, SaleMode.Instant, 0.85)
        val expected = 1.10 / (10.0 * 0.85)
        assertEquals(expected, result.getOrThrow().heizungskostenOelEurKwh, 0.0001)
    }

    @Test
    fun `btc per day formula is correct`() {
        // tägl. BTC = (hashrate_Hs * 86400) / (difficulty * 2^32) * 3.125
        val result = useCase(listOf(testMiner), testMarket, SaleMode.Instant, 0.85)
        val hashrateHs = 110.0 * 1_000_000_000_000.0
        val expected = (hashrateHs * 86400.0) / (testMarket.networkDifficulty * 4_294_967_296.0) * 3.125
        assertEquals(expected, result.getOrThrow().expectedBtcPerDay, 0.0000001)
    }

    @Test
    fun `hodl mode uses target price instead of market price`() {
        val hodlTarget = 120_000.0
        val instant = useCase(listOf(testMiner), testMarket, SaleMode.Instant, 0.85).getOrThrow()
        val hodl = useCase(listOf(testMiner), testMarket, SaleMode.Hodl(hodlTarget), 0.85).getOrThrow()

        // Ertrag mit Hodl-Preis muss höher sein als mit aktuellem Preis (120k > 85k)
        assertTrue(hodl.expectedEurPerDay > instant.expectedEurPerDay)
        assertEquals(hodlTarget / testMarket.btcPriceEur, hodl.expectedEurPerDay / instant.expectedEurPerDay, 0.001)
    }

    @Test
    fun `inactive miners are excluded from calculation`() {
        val inactiveMiner = testMiner.copy(id = 2, isActive = false)
        val resultBoth = useCase(listOf(testMiner, inactiveMiner), testMarket, SaleMode.Instant, 0.85).getOrThrow()
        val resultOne = useCase(listOf(testMiner), testMarket, SaleMode.Instant, 0.85).getOrThrow()

        assertEquals(resultOne.expectedBtcPerDay, resultBoth.expectedBtcPerDay, 0.0000001)
    }

    @Test
    fun `isWorthIt is true when strompreis below break-even`() {
        // Sehr günstiger Strom (0.01 €/kWh) → sollte sich lohnen
        val cheapMarket = testMarket.copy(currentStrompreisEurKwh = 0.01)
        val result = useCase(listOf(testMiner), cheapMarket, SaleMode.Instant, 0.85).getOrThrow()
        assertTrue(result.isWorthIt)
        assertTrue(result.deltaEurKwh > 0)
    }

    @Test
    fun `isWorthIt is false when strompreis above break-even`() {
        // Sehr teurer Strom (1.00 €/kWh) → sollte sich nicht lohnen
        val expensiveMarket = testMarket.copy(currentStrompreisEurKwh = 1.00)
        val result = useCase(listOf(testMiner), expensiveMarket, SaleMode.Instant, 0.85).getOrThrow()
        assertFalse(result.isWorthIt)
        assertTrue(result.deltaEurKwh < 0)
    }

    @Test
    fun `two active miners double the output`() {
        val miner2 = testMiner.copy(id = 2)
        val single = useCase(listOf(testMiner), testMarket, SaleMode.Instant, 0.85).getOrThrow()
        val dual = useCase(listOf(testMiner, miner2), testMarket, SaleMode.Instant, 0.85).getOrThrow()

        assertEquals(single.expectedBtcPerDay * 2, dual.expectedBtcPerDay, 0.0000001)
        assertEquals(single.stromkostenEurPerDay * 2, dual.stromkostenEurPerDay, 0.001)
    }
}
