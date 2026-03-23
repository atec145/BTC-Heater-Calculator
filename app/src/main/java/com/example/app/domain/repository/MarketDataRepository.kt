package com.example.app.domain.repository

import com.example.app.domain.model.DifficultyEpoch
import com.example.app.domain.model.MarketData

interface MarketDataRepository {
    suspend fun getMarketData(): Result<MarketData>
    suspend fun getDifficultyHistory(): Result<List<DifficultyEpoch>>
}
