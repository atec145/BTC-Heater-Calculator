package atec.btcheater.calculator.domain.model

data class DifficultyEpoch(
    val timestamp: Long,       // Unix seconds
    val blockHeight: Int,
    val difficulty: Double
)
