package com.example.app.presentation.screens.difficulty

import com.example.app.domain.model.DifficultyEpoch

data class DifficultyUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val epochs: List<DifficultyEpoch> = emptyList()
)
