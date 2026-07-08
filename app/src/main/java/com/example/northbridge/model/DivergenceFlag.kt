package com.example.northbridge.model

data class DivergenceFlag(
    val id: Long,
    val eventId: Long,
    val internalConsensusProbability: Double,
    val externalConsensusProbability: Double,
    val divergenceScore: Double,
    val flagType: FlagType,
    val status: FlagStatus,
    val createdAt: String
)
