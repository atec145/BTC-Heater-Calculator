package atec.btcheater.calculator.domain.repository

import atec.btcheater.calculator.domain.model.DifficultyEpoch
import atec.btcheater.calculator.domain.model.MarketData

interface MarketDataRepository {
    suspend fun getMarketData(): Result<MarketData>
    suspend fun getDifficultyHistory(): Result<List<DifficultyEpoch>>
}
