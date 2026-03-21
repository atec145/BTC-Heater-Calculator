package com.example.app.di

import com.example.app.data.repository.MarketDataRepositoryImpl
import com.example.app.data.repository.MinerConfigRepositoryImpl
import com.example.app.domain.repository.MarketDataRepository
import com.example.app.domain.repository.MinerConfigRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMarketDataRepository(impl: MarketDataRepositoryImpl): MarketDataRepository

    @Binds
    @Singleton
    abstract fun bindMinerConfigRepository(impl: MinerConfigRepositoryImpl): MinerConfigRepository
}
