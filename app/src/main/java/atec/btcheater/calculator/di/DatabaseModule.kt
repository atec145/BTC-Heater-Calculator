package atec.btcheater.calculator.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import atec.btcheater.calculator.data.local.AppDatabase
import atec.btcheater.calculator.data.local.dao.MinerConfigDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "btc_heater.db").build()

    @Provides
    fun provideMinerConfigDao(db: AppDatabase): MinerConfigDao = db.minerConfigDao()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("btc_heater_prefs", Context.MODE_PRIVATE)
}
