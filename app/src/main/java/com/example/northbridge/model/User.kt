package com.example.northbridge.model

data class User(
    val id: Long,
    val companyId: Long,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val department: String?,
    val region: String?,
    val calibrationScore: Double?,
    val isActive: Boolean = true,
    val createdAt: String
)
