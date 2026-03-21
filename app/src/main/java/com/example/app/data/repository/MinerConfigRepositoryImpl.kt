package com.example.app.data.repository

import android.content.SharedPreferences
import com.example.app.data.local.dao.MinerConfigDao
import com.example.app.data.local.entity.MinerConfigEntity
import com.example.app.domain.model.MinerConfig
import com.example.app.domain.model.SaleMode
import com.example.app.domain.repository.MinerConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MinerConfigRepositoryImpl @Inject constructor(
    private val dao: MinerConfigDao,
    private val prefs: SharedPreferences
) : MinerConfigRepository {

    override fun getMiners(): Flow<List<MinerConfig>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveMiner(miner: MinerConfig) =
        dao.insertOrUpdate(miner.toEntity())

    override suspend fun deleteMiner(id: Int) = dao.deleteById(id)

    override suspend fun getSaleMode(): SaleMode {
        val isHodl = prefs.getBoolean(KEY_IS_HODL, false)
        return if (isHodl) {
            val target = prefs.getFloat(KEY_HODL_TARGET, 0f).toDouble()
            SaleMode.Hodl(target)
        } else {
            SaleMode.Instant
        }
    }

    override suspend fun saveSaleMode(mode: SaleMode) {
        prefs.edit().apply {
            when (mode) {
                is SaleMode.Instant -> {
                    putBoolean(KEY_IS_HODL, false)
                }
                is SaleMode.Hodl -> {
                    putBoolean(KEY_IS_HODL, true)
                    putFloat(KEY_HODL_TARGET, mode.targetPriceEur.toFloat())
                }
            }
        }.apply()
    }

    override suspend fun getOilPrice(): Double =
        prefs.getFloat(KEY_OIL_PRICE, 1.10f).toDouble()

    override suspend fun saveOilPrice(price: Double) {
        prefs.edit().putFloat(KEY_OIL_PRICE, price.toFloat()).apply()
    }

    override suspend fun getBoilerEfficiency(): Double =
        prefs.getFloat(KEY_BOILER_EFFICIENCY, 0.85f).toDouble()

    override suspend fun saveBoilerEfficiency(efficiency: Double) {
        prefs.edit().putFloat(KEY_BOILER_EFFICIENCY, efficiency.toFloat()).apply()
    }

    private fun MinerConfigEntity.toDomain() = MinerConfig(
        id = id, label = label, hashrateThs = hashrateThs, watt = watt, isActive = isActive
    )

    private fun MinerConfig.toEntity() = MinerConfigEntity(
        id = id, label = label, hashrateThs = hashrateThs, watt = watt, isActive = isActive
    )

    companion object {
        private const val KEY_IS_HODL = "is_hodl"
        private const val KEY_HODL_TARGET = "hodl_target"
        private const val KEY_OIL_PRICE = "oil_price"
        private const val KEY_BOILER_EFFICIENCY = "boiler_efficiency"
    }
}
