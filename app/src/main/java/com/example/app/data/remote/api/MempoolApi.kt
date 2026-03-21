package com.example.app.data.remote.api

import retrofit2.http.GET

interface BlockchainInfoApi {
    @GET("q/getdifficulty")
    suspend fun getDifficulty(): Double
}
