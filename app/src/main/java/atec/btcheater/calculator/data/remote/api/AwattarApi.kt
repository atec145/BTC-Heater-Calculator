package atec.btcheater.calculator.data.remote.api

import atec.btcheater.calculator.data.remote.dto.AwattarResponse
import retrofit2.http.GET

interface AwattarApi {
    @GET("v1/marketdata")
    suspend fun getMarketData(): AwattarResponse
}
