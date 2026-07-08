package com.example.northbridge.model

import java.math.BigDecimal

data class TrackedEvent(
    val id: Long,
    val companyId: Long,
    val title: String,
    val description: String?,
    val category: EventCategory,
    val resolutionDate: String,
    val exposureAmount: BigDecimal?,
    val currency: String?,
    val status: EventStatus,
    val createdBy: Long,
    val createdAt: String
)
