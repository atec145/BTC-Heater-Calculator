package com.example.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "miner_configs")
data class MinerConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val hashrateThs: Double,
    val watt: Double,
    val isActive: Boolean
)
