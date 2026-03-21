package com.example.app.presentation.screens.settings

import com.example.app.domain.model.MinerConfig
import com.example.app.domain.model.SaleMode

data class SettingsUiState(
    val isLoading: Boolean = true,
    val miners: List<MinerConfig> = emptyList(),
    val saleMode: SaleMode = SaleMode.Instant,
    val hodlTargetInput: String = "",
    val oilPriceInput: String = "1.10",
    val boilerEfficiencyInput: String = "85",
    val isSaved: Boolean = false,
    // Dialog-Zustand für Miner hinzufügen/bearbeiten
    val editingMiner: MinerConfig? = null,
    val showMinerDialog: Boolean = false
)
