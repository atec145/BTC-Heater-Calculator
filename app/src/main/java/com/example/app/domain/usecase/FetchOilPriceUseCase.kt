package com.example.app.domain.usecase

import com.example.app.domain.repository.MinerConfigRepository
import javax.inject.Inject

class FetchOilPriceUseCase @Inject constructor(
    private val repository: MinerConfigRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Double {
        val currentPrice = repository.getOilPrice()
        val isManual = repository.isOilPriceManual()

        if (isManual && !forceRefresh) return currentPrice

        val fetchedAt = repository.getOilPriceFetchedAt()
        val cacheAgeMs = System.currentTimeMillis() - fetchedAt * 1000L
        val cacheValid = fetchedAt > 0 && cacheAgeMs < 24 * 3600 * 1000L

        if (cacheValid && !forceRefresh) return currentPrice

        val remotePrice = repository.fetchRemoteOilPrice()
        return if (remotePrice != null) {
            repository.saveOilPrice(remotePrice)
            repository.saveOilPriceFetchedAt(System.currentTimeMillis() / 1000)
            repository.setOilPriceManual(false)
            remotePrice
        } else {
            currentPrice
        }
    }
}
