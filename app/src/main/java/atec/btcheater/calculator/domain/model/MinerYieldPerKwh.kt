package atec.btcheater.calculator.domain.model

data class MinerYieldPerKwh(
    val minerId: Int,
    val label: String,
    val satsPerKwh: Long,
    val eurPerKwh: Double
)
