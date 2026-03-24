package atec.btcheater.calculator.di

import atec.btcheater.calculator.data.repository.MarketDataRepositoryImpl
import atec.btcheater.calculator.data.repository.MinerConfigRepositoryImpl
import atec.btcheater.calculator.domain.repository.MarketDataRepository
import atec.btcheater.calculator.domain.repository.MinerConfigRepository
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
