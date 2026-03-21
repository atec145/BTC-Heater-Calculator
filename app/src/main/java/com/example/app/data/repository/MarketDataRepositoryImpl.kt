package com.example.app.data.repository

import com.example.app.data.remote.MarketDataRemoteDataSource
import com.example.app.domain.model.MarketData
import com.example.app.domain.repository.MarketDataRepository
import com.example.app.domain.repository.MinerConfigRepository
import javax.inject.Inject

class MarketDataRepositoryImpl @Inject constructor(
    private val remoteDataSource: MarketDataRemoteDataSource,
    private val minerConfigRepository: MinerConfigRepository
) : MarketDataRepository {

    override suspend fun getMarketData(): Result<MarketData> = runCatching {
        val oilPrice = minerConfigRepository.getOilPrice()
        remoteDataSource.fetchMarketData(oilPrice)
    }
}
