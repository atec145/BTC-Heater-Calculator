package com.example.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.domain.model.SaleMode
import com.example.app.domain.repository.MarketDataRepository
import com.example.app.domain.repository.MinerConfigRepository
import com.example.app.domain.usecase.CalculateProfitabilityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val marketDataRepository: MarketDataRepository,
    private val minerConfigRepository: MinerConfigRepository,
    private val calculateProfitability: CalculateProfitabilityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeMiners()
    }

    private fun observeMiners() {
        viewModelScope.launch {
            minerConfigRepository.getMiners().collectLatest { miners ->
                _uiState.update { it.copy(activeMiners = miners) }
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val saleMode = minerConfigRepository.getSaleMode()
            val boilerEfficiency = minerConfigRepository.getBoilerEfficiency()

            marketDataRepository.getMarketData()
                .onSuccess { market ->
                    val miners = _uiState.value.activeMiners
                    val activeMiners = miners.filter { it.isActive }

                    val result = if (activeMiners.isEmpty()) null else {
                        calculateProfitability(miners, market, saleMode, boilerEfficiency)
                    }

                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val lastUpdated = timeFormat.format(Date(market.lastUpdated * 1000))

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            result = result,
                            hourlyPrices = market.hourlyPrices,
                            saleMode = saleMode,
                            btcPriceEur = market.btcPriceEur,
                            lastUpdated = lastUpdated
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Netzwerkfehler"
                        )
                    }
                }
        }
    }
}
