package com.example.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.domain.model.MinerConfig
import com.example.app.domain.model.SaleMode
import com.example.app.domain.repository.MinerConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: MinerConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val saleMode = repository.getSaleMode()
            val oilPrice = repository.getOilPrice()
            val efficiency = repository.getBoilerEfficiency()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    saleMode = saleMode,
                    hodlTargetInput = if (saleMode is SaleMode.Hodl) saleMode.targetPriceEur.toString() else "",
                    oilPriceInput = "%.2f".format(oilPrice),
                    boilerEfficiencyInput = "%.0f".format(efficiency * 100)
                )
            }
        }

        viewModelScope.launch {
            repository.getMiners().collectLatest { miners ->
                _uiState.update { it.copy(miners = miners) }
            }
        }
    }

    fun onSaleModeChanged(isHodl: Boolean) {
        val mode = if (isHodl) {
            val target = _uiState.value.hodlTargetInput.toDoubleOrNull() ?: 0.0
            SaleMode.Hodl(target)
        } else SaleMode.Instant
        _uiState.update { it.copy(saleMode = mode) }
    }

    fun onHodlTargetChanged(value: String) {
        _uiState.update {
            it.copy(
                hodlTargetInput = value,
                saleMode = SaleMode.Hodl(value.toDoubleOrNull() ?: 0.0)
            )
        }
    }

    fun onOilPriceChanged(value: String) {
        _uiState.update { it.copy(oilPriceInput = value) }
    }

    fun onBoilerEfficiencyChanged(value: String) {
        _uiState.update { it.copy(boilerEfficiencyInput = value) }
    }

    fun showAddMinerDialog() {
        _uiState.update { it.copy(showMinerDialog = true, editingMiner = null) }
    }

    fun showEditMinerDialog(miner: MinerConfig) {
        _uiState.update { it.copy(showMinerDialog = true, editingMiner = miner) }
    }

    fun dismissMinerDialog() {
        _uiState.update { it.copy(showMinerDialog = false, editingMiner = null) }
    }

    fun saveMiner(label: String, hashrateThs: Double, watt: Double, isActive: Boolean) {
        viewModelScope.launch {
            val existing = _uiState.value.editingMiner
            val miner = MinerConfig(
                id = existing?.id ?: 0,
                label = label,
                hashrateThs = hashrateThs,
                watt = watt,
                isActive = isActive
            )
            repository.saveMiner(miner)
            dismissMinerDialog()
        }
    }

    fun deleteMiner(id: Int) {
        viewModelScope.launch { repository.deleteMiner(id) }
    }

    fun toggleMinerActive(miner: MinerConfig) {
        viewModelScope.launch {
            repository.saveMiner(miner.copy(isActive = !miner.isActive))
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            repository.saveSaleMode(_uiState.value.saleMode)
            _uiState.value.oilPriceInput.toDoubleOrNull()?.let { repository.saveOilPrice(it) }
            _uiState.value.boilerEfficiencyInput.toDoubleOrNull()?.let {
                repository.saveBoilerEfficiency(it / 100.0)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
