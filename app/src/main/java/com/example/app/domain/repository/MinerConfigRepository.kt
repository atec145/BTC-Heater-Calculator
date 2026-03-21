package com.example.app.domain.repository

import com.example.app.domain.model.MinerConfig
import com.example.app.domain.model.SaleMode
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
}
