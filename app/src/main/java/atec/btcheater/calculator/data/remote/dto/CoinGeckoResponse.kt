package atec.btcheater.calculator.data.remote.dto

import com.google.gson.annotations.SerializedName

// Response von /simple/price?ids=bitcoin&vs_currencies=eur
// z.B. {"bitcoin":{"eur":60000.0}}
typealias CoinGeckoResponse = Map<String, Map<String, Double>>
