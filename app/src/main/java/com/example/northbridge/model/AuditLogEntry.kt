package com.example.northbridge.model

data class AuditLogEntry(
    val id: Long,
    val entityType: String,
    val entityId: Long,
    val action: AuditAction,
    val performedBy: Long?,
    val details: String?,
    val createdAt: String
)
