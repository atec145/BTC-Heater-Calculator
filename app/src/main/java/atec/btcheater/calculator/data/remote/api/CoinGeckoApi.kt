package atec.btcheater.calculator.data.remote.api

import atec.btcheater.calculator.data.remote.dto.CoinGeckoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {
    @GET("api/v3/simple/price")
    suspend fun getPrice(
        @Query("ids") ids: String = "bitcoin",
        @Query("vs_currencies") vsCurrencies: String = "eur"
    ): CoinGeckoResponse
}
