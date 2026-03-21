package com.example.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AwattarResponse(
    @SerializedName("data") val data: List<AwattarDataPoint>
)

data class AwattarDataPoint(
    @SerializedName("start_timestamp") val startTimestamp: Long, // ms
    @SerializedName("end_timestamp") val endTimestamp: Long,     // ms
    @SerializedName("marketprice") val marketpriceEurMwh: Double,
    @SerializedName("unit") val unit: String
)
