package com.example.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HeizOel24PricePoint(
    @SerializedName("Price") val price: Double = 0.0,
    @SerializedName("Date") val date: String = ""
)
