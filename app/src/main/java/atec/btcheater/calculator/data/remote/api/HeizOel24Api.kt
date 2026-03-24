package atec.btcheater.calculator.data.remote.api

import atec.btcheater.calculator.data.remote.dto.HeizOel24Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HeizOel24Api {
    @GET("api/chartapi/GetAveragePriceHistory")
    suspend fun getPriceHistory(
        @Query("countryId") countryId: Int,
        @Query("minDate") minDate: String,
        @Query("maxDate") maxDate: String
    ): HeizOel24Response
}
