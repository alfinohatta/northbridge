package com.example.northbridge.model

data class ExternalDataSource(
    val id: Long,
    val name: String,
    val sourceType: SourceType,
    val websiteUrl: String?,
    val isLicensed: Boolean
)
