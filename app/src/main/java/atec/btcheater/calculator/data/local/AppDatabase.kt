package atec.btcheater.calculator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import atec.btcheater.calculator.data.local.dao.MinerConfigDao
import atec.btcheater.calculator.data.local.entity.MinerConfigEntity

@Database(entities = [MinerConfigEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun minerConfigDao(): MinerConfigDao
}
