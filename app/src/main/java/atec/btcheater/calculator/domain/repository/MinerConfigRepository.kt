package atec.btcheater.calculator.domain.repository

import atec.btcheater.calculator.domain.model.MinerConfig
import atec.btcheater.calculator.domain.model.SaleMode
import kotlinx.coroutines.flow.Flow

interface MinerConfigRepository {
    fun getMiners(): Flow<List<MinerConfig>>
    suspend fun saveMiner(miner: MinerConfig)
    suspend fun deleteMiner(id: Int)
    suspend fun getSaleMode(): SaleMode
    suspend fun saveSaleMode(mode: SaleMode)
    suspend fun getOilPrice(): Double
    suspend fun saveOilPrice(price: Double)
    suspend fun getBoilerEfficiency(): Double
    suspend fun saveBoilerEfficiency(efficiency: Double)
    suspend fun getNetzaufschlagCtKwh(): Double
    suspend fun saveNetzaufschlagCtKwh(ct: Double)
    suspend fun getMinerWaermeeffizienz(): Double
    suspend fun saveMinerWaermeeffizienz(efficiency: Double)
    suspend fun fetchRemoteOilPrice(): Double?
    suspend fun getOilPriceFetchedAt(): Long
    suspend fun saveOilPriceFetchedAt(epochSeconds: Long)
    suspend fun isOilPriceManual(): Boolean
    suspend fun setOilPriceManual(manual: Boolean)
}
