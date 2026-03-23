package com.example.app.presentation.screens.difficulty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.domain.repository.MarketDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DifficultyViewModel @Inject constructor(
    private val marketDataRepository: MarketDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DifficultyUiState())
    val uiState: StateFlow<DifficultyUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            marketDataRepository.getDifficultyHistory()
                .onSuccess { epochs ->
                    _uiState.update { it.copy(isLoading = false, epochs = epochs) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Netzwerkfehler") }
                }
        }
    }
}
