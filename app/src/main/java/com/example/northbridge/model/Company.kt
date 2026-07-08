package com.example.northbridge.model

data class Company(
    val id: Long,
    val name: String,
    val industry: String,
    val country: String,
    val headquarters: String?,
    val createdAt: String
)
