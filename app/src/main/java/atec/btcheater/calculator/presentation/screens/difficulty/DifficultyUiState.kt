package atec.btcheater.calculator.presentation.screens.difficulty

import atec.btcheater.calculator.domain.model.DifficultyEpoch

data class DifficultyUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val epochs: List<DifficultyEpoch> = emptyList()
)
