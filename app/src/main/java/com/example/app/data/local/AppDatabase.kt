package com.example.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.app.data.local.dao.MinerConfigDao
import com.example.app.data.local.entity.MinerConfigEntity

@Database(entities = [MinerConfigEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun minerConfigDao(): MinerConfigDao
}
