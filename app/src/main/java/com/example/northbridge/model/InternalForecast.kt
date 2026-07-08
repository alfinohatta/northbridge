package com.example.northbridge.model

data class InternalForecast(
    val id: Long,
    val eventId: Long,
    val userId: Long,
    val probability: Double,
    val rationale: String?,
    val submittedAt: String
)
