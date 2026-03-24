package atec.btcheater.calculator.presentation.screens.settings

import atec.btcheater.calculator.domain.model.MinerConfig
import atec.btcheater.calculator.domain.model.SaleMode

data class SettingsUiState(
    val isLoading: Boolean = true,
    val miners: List<MinerConfig> = emptyList(),
    val saleMode: SaleMode = SaleMode.Instant,
    val hodlTargetInput: String = "",
    val oilPriceInput: String = "1.10",
    val oilPriceIsAuto: Boolean = false,
    val oilPriceLastFetched: String = "",  // "DD.MM.YYYY" oder ""
    val oilPriceManuallyEdited: Boolean = false,
    val boilerEfficiencyInput: String = "85",
    val netzaufschlagInput: String = "9.85",
    val minerWaermeeffizienzInput: String = "85",
    val isSaved: Boolean = false,
    // Dialog-Zustand für Miner hinzufügen/bearbeiten
    val editingMiner: MinerConfig? = null,
    val showMinerDialog: Boolean = false
)
