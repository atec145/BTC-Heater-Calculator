package com.example.app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.app.data.local.AppDatabase
import com.example.app.data.local.dao.MinerConfigDao
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
