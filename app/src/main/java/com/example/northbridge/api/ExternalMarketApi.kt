package com.example.northbridge.api

import retrofit2.http.GET
import retrofit2.http.Path

data class MarketSignalResponse(
    val eventId: Long,
    val probability: Double,
    val sampleSize: Int?,
    val sourceName: String,
    val timestamp: String
)

interface ExternalMarketApi {
    @GET("markets/signals/{eventId}")
    suspend fun getMarketSignal(@Path("eventId") eventId: Long): MarketSignalResponse
}
