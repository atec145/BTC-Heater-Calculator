package com.example.app.data.repository

import com.example.app.data.remote.MarketDataRemoteDataSource
import com.example.app.data.remote.api.MempoolSpaceApi
import com.example.app.domain.model.DifficultyEpoch
import com.example.app.domain.model.MarketData
import com.example.app.domain.repository.MarketDataRepository
import com.example.app.domain.repository.MinerConfigRepository
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
        adjustments.takeLast(10).map { entry ->
            DifficultyEpoch(
                timestamp = entry[0].toLong(),
                blockHeight = entry[1].toInt(),
                difficulty = entry[2]
            )
        }
    }
}
