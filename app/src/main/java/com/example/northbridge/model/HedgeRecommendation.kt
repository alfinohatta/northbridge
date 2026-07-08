package com.example.northbridge.model

import java.math.BigDecimal

data class HedgeRecommendation(
    val id: Long,
    val flagId: Long,
    val recommendedAction: String,
    val instrumentType: InstrumentType,
    val notionalAmount: BigDecimal,
    val currency: String,
    val status: HedgeStatus,
    val approvedBy: Long?,
    val approvedAt: String?
)
