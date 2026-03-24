package atec.btcheater.calculator.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import atec.btcheater.calculator.domain.model.MinerConfig
import atec.btcheater.calculator.domain.model.SaleMode
import atec.btcheater.calculator.domain.repository.MinerConfigRepository
import atec.btcheater.calculator.domain.usecase.FetchOilPriceUseCase
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
class SettingsViewModel @Inject constructor(
    private val repository: MinerConfigRepository,
    private val fetchOilPriceUseCase: FetchOilPriceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val saleMode = repository.getSaleMode()
            val efficiency = repository.getBoilerEfficiency()
            val netzaufschlag = repository.getNetzaufschlagCtKwh()
            val waermeeffizienz = repository.getMinerWaermeeffizienz()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    saleMode = saleMode,
                    hodlTargetInput = if (saleMode is SaleMode.Hodl) saleMode.targetPriceEur.toString() else "",
                    boilerEfficiencyInput = "%.0f".format(efficiency * 100),
                    netzaufschlagInput = "%.2f".format(netzaufschlag),
                    minerWaermeeffizienzInput = "%.0f".format(waermeeffizienz * 100)
                )
            }
        }

        viewModelScope.launch {
            loadOilPrice(forceRefresh = false)
        }

        viewModelScope.launch {
            repository.getMiners().collectLatest { miners ->
                _uiState.update { it.copy(miners = miners) }
            }
        }
    }

    private suspend fun loadOilPrice(forceRefresh: Boolean) {
        val price = fetchOilPriceUseCase(forceRefresh)
        val isManual = repository.isOilPriceManual()
        val fetchedAt = repository.getOilPriceFetchedAt()
        val dateStr = if (fetchedAt > 0) {
            SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN).format(Date(fetchedAt * 1000))
        } else ""

        _uiState.update {
            it.copy(
                oilPriceInput = "%.2f".format(price),
                oilPriceIsAuto = !isManual,
                oilPriceLastFetched = dateStr,
                oilPriceManuallyEdited = false
            )
        }
    }

    fun refreshOilPrice() {
        viewModelScope.launch { loadOilPrice(forceRefresh = true) }
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
        _uiState.update { it.copy(oilPriceInput = value, oilPriceManuallyEdited = true) }
    }

    fun onBoilerEfficiencyChanged(value: String) {
        _uiState.update { it.copy(boilerEfficiencyInput = value) }
    }

    fun onNetzaufschlagChanged(value: String) {
        _uiState.update { it.copy(netzaufschlagInput = value) }
    }

    fun onMinerWaermeeffizienzChanged(value: String) {
        _uiState.update { it.copy(minerWaermeeffizienzInput = value) }
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
            _uiState.value.oilPriceInput.toDoubleOrNull()?.let {
                repository.saveOilPrice(it)
                if (_uiState.value.oilPriceManuallyEdited) {
                    repository.setOilPriceManual(true)
                    _uiState.update { s -> s.copy(oilPriceIsAuto = false, oilPriceLastFetched = "") }
                }
            }
            _uiState.value.boilerEfficiencyInput.toDoubleOrNull()?.let {
                repository.saveBoilerEfficiency(it / 100.0)
            }
            _uiState.value.netzaufschlagInput.toDoubleOrNull()?.let {
                repository.saveNetzaufschlagCtKwh(it)
            }
            _uiState.value.minerWaermeeffizienzInput.toDoubleOrNull()?.let {
                repository.saveMinerWaermeeffizienz(it / 100.0)
            }
            _uiState.update { it.copy(isSaved = true, oilPriceManuallyEdited = false) }
        }
    }
}
