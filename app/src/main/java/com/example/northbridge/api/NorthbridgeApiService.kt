package com.example.northbridge.api

import com.example.northbridge.model.*
import retrofit2.http.*
import java.math.BigDecimal

// --- MySQL-Mapped DTOs ---

data class CompanyDto(
    val id: Long,
    val name: String,
    val industry: String,
    val countryCode: String,
    val headquarters: String?,
    val createdAt: String
)

data class UserDto(
    val id: Long,
    val companyId: Long,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val department: String?,
    val region: String?,
    val calibrationScore: Double?,
    val createdAt: String
)

data class TrackedEventDto(
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

data class ExternalDataSourceDto(
    val id: Long,
    val name: String,
    val sourceType: SourceType,
    val websiteUrl: String?,
    val isLicensed: Boolean,
    val createdAt: String
)

data class ExternalMarketDataDto(
    val id: Long,
    val eventId: Long,
    val sourceId: Long,
    val probability: Double,
    val sampleSize: Int?,
    val recordedAt: String
)

data class DivergenceFlagDto(
    val id: Long,
    val eventId: Long,
    val internalConsensusProbability: Double,
    val externalConsensusProbability: Double,
    val divergenceScore: Double,
    val flagType: FlagType,
    val status: FlagStatus,
    val summary: String?,
    val createdAt: String
)

data class DeviceTokenDto(
    val userId: Long,
    val fcmToken: String,
    val platform: String,
    val appVersion: String?
)

data class HedgeRecommendationDto(
    val id: Long,
    val flagId: Long,
    val recommendedAction: String,
    val instrumentType: InstrumentType,
    val notionalAmount: BigDecimal,
    val currency: String,
    val status: HedgeStatus,
    val executionPartner: String?,
    val createdAt: String
)

data class AuditLogDto(
    val id: Long,
    val entityType: String,
    val entityId: Long,
    val action: AuditAction,
    val performedBy: Long?,
    val details: String?, // JSON Payload
    val createdAt: String
)

// --- API Endpoints ---

interface NorthbridgeApiService {

    @GET("sync/companies")
    suspend fun getCompanies(): List<CompanyDto>

    @GET("sync/users")
    suspend fun getUsers(): List<UserDto>

    @GET("sync/events")
    suspend fun getEvents(): List<TrackedEventDto>

    @GET("sync/sources")
    suspend fun getDataSources(): List<ExternalDataSourceDto>

    @GET("sync/market-data")
    suspend fun getMarketData(): List<ExternalMarketDataDto>

    @GET("sync/flags")
    suspend fun getFlags(): List<DivergenceFlagDto>

    @GET("sync/hedges")
    suspend fun getHedges(): List<HedgeRecommendationDto>

    @PATCH("hedges/recommendations/{id}")
    suspend fun updateHedgeStatus(
        @Path("id") id: Long,
        @Body statusUpdate: Map<String, String>
    ): HedgeRecommendationDto

    @GET("sync/audit")
    suspend fun getAuditLogs(): List<AuditLogDto>

    @POST("sync/audit")
    suspend fun postAuditLog(@Body log: AuditLogDto): AuditLogDto

    @GET("users/me")
    suspend fun getCurrentUser(): UserDto

    @POST("auth/refresh")
    suspend fun refreshToken(@Body body: Map<String, String>): Map<String, String>

    @POST("users/device")
    suspend fun registerDevice(@Body deviceToken: DeviceTokenDto): DeviceTokenDto

    @POST("forecasts")
    suspend fun submitForecast(@Body forecast: InternalForecastDto): InternalForecastDto
}

data class InternalForecastDto(
    val eventId: Long,
    val userId: Long,
    val probability: Double,
    val rationale: String?,
    val submittedAt: String
)
