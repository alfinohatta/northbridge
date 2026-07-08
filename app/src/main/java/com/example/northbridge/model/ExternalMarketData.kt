package com.example.northbridge.model

data class ExternalMarketData(
    val id: Long,
    val eventId: Long,
    val sourceId: Long,
    val probability: Double,
    val recordedAt: String
)
