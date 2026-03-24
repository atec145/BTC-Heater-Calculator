package atec.btcheater.calculator.data.repository

import atec.btcheater.calculator.data.remote.MarketDataRemoteDataSource
import atec.btcheater.calculator.data.remote.api.MempoolSpaceApi
import atec.btcheater.calculator.domain.model.DifficultyEpoch
import atec.btcheater.calculator.domain.model.MarketData
import atec.btcheater.calculator.domain.repository.MarketDataRepository
import atec.btcheater.calculator.domain.repository.MinerConfigRepository
import javax.inject.Inject

class MarketDataRepositoryImpl @Inject constructor(
    private val remoteDataSource: MarketDataRemoteDataSource,
    private val minerConfigRepository: MinerConfigRepository,
    private val mempoolSpaceApi: MempoolSpaceApi
) : MarketDataRepository {

    override suspend fun getMarketData(): Result<MarketData> = runCatching {
        val oilPrice = minerConfigRepository.getOilPrice()
        val netzaufschlagEurKwh = minerConfigRepository.getNetzaufschlagCtKwh() / 100.0
        remoteDataSource.fetchMarketData(oilPrice, netzaufschlagEurKwh)
    }

    override suspend fun getDifficultyHistory(): Result<List<DifficultyEpoch>> = runCatching {
        val adjustments = mempoolSpaceApi.getDifficultyAdjustments()
        // API liefert Epochen von NEU nach ALT → erste 10 nehmen und umkehren für Chart
        val count = adjustments.size().coerceAtMost(10)
        (0 until count).map { i ->
            val entry = adjustments[i].asJsonArray
            DifficultyEpoch(
                timestamp = entry[0].asLong,
                blockHeight = entry[1].asInt,
                difficulty = entry[2].asDouble
            )
        }.reversed()
    }
}
