package com.example.app.domain.model

data class MinerConfig(
    val id: Int,
    val label: String,
    val hashrateThs: Double,
    val watt: Double,
    val isActive: Boolean
)
