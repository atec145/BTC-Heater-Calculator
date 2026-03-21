package com.example.app.data.remote

import com.example.app.data.remote.api.AwattarApi
import com.example.app.data.remote.api.BlockchainInfoApi
import com.example.app.data.remote.api.CoinGeckoApi
import com.example.app.domain.model.HourlyPrice
import com.example.app.domain.model.MarketData
import javax.inject.Inject

class MarketDataRemoteDataSource @Inject constructor(
    private val awattarApi: AwattarApi,
    private val blockchainInfoApi: BlockchainInfoApi,
    private val coinGeckoApi: CoinGeckoApi
) {
    suspend fun fetchMarketData(heizolPreis: Double, netzaufschlagEurKwh: Double = 0.0985): MarketData {
        val awattar = awattarApi.getMarketData()
        val difficulty = blockchainInfoApi.getDifficulty()
        val coinGecko = coinGeckoApi.getPrice()

        val now = System.currentTimeMillis()
        val hourlyPrices = awattar.data.map { point ->
            HourlyPrice(
                epochStart = point.startTimestamp / 1000,
                priceEurKwh = point.marketpriceEurMwh / 1000.0 + netzaufschlagEurKwh
            )
        }.sortedBy { it.epochStart }

        // Aktuellen Stundenpreis finden (nächste Stunde die noch läuft)
        val currentPrice = hourlyPrices
            .lastOrNull { it.epochStart * 1000 <= now }
            ?.priceEurKwh
            ?: hourlyPrices.firstOrNull()?.priceEurKwh
            ?: 0.0

        val btcPrice = coinGecko["bitcoin"]?.get("eur") ?: 0.0

        return MarketData(
            currentStrompreisEurKwh = currentPrice,
            hourlyPrices = hourlyPrices,
            btcPriceEur = btcPrice,
            networkDifficulty = difficulty,
            heizolPreisEurLiter = heizolPreis,
            lastUpdated = now / 1000
        )
    }
}
