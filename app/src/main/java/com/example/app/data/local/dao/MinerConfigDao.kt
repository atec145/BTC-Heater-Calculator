package com.example.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.app.data.local.entity.MinerConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MinerConfigDao {
    @Query("SELECT * FROM miner_configs")
    fun getAll(): Flow<List<MinerConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(miner: MinerConfigEntity)

    @Query("DELETE FROM miner_configs WHERE id = :id")
    suspend fun deleteById(id: Int)
}
