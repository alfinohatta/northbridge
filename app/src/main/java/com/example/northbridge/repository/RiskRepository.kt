package com.example.northbridge.repository

import com.example.northbridge.api.*
import com.example.northbridge.db.dao.*
import com.example.northbridge.db.entity.*
import com.example.northbridge.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlin.math.abs

class RiskRepository(
    private val eventDao: TrackedEventDao,
    private val flagDao: DivergenceFlagDao,
    private val forecastDao: InternalForecastDao,
    private val auditLogDao: AuditLogDao,
    private val hedgeDao: HedgeRecommendationDao,
    private val userDao: UserDao,
    private val marketDao: ExternalMarketDataDao,
    private val notificationDao: NotificationDao,
    private val companyDao: CompanyDao,
    private val externalDataSourceDao: ExternalDataSourceDao,
    private val apiService: NorthbridgeApiService
) {
    suspend fun syncWithBackend() {
        try {
            // 1. Sync Companies
            apiService.getCompanies().forEach { companyDao.insertCompany(it.toCompanyEntity()) }

            // 2. Sync Users
            apiService.getUsers().forEach { userDao.insertUser(it.toUserEntity()) }

            // 3. Sync Data Sources
            apiService.getDataSources().forEach { externalDataSourceDao.insertSource(it.toDataSourceEntity()) }

            // 4. Sync Events
            val remoteEvents = apiService.getEvents()
            remoteEvents.forEach { eventDao.insertEvent(it.toTrackedEventEntity()) }
            
            // 5. Sync Market Data
            apiService.getMarketData().forEach { marketDao.insertMarketData(it.toMarketDataEntity()) }

            // 6. Sync Flags
            val remoteFlags = apiService.getFlags()
            remoteFlags.forEach { flagDao.insertFlag(it.toFlagEntity()) }

            // 7. Sync Hedges
            apiService.getHedges().forEach { hedgeDao.insertRecommendation(it.toHedgeEntity()) }

            // 8. Sync Audit Logs
            apiService.getAuditLogs().forEach { auditLogDao.insertLog(it.toAuditEntity()) }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun CompanyDto.toCompanyEntity() = CompanyEntity(
        id = id, name = name, industry = industry, countryCode = countryCode,
        headquarters = headquarters, createdAt = createdAt, updatedAt = createdAt
    )

    private fun UserDto.toUserEntity() = UserEntity(
        id = id, companyId = companyId, fullName = fullName, email = email,
        passwordHash = "REDACTED", role = role, department = department, region = region,
        calibrationScore = calibrationScore, createdAt = createdAt, updatedAt = createdAt
    )

    private fun ExternalDataSourceDto.toDataSourceEntity() = ExternalDataSourceEntity(
        id = id, name = name, sourceType = sourceType, websiteUrl = websiteUrl,
        isLicensed = isLicensed, createdAt = createdAt
    )

    private fun ExternalMarketDataDto.toMarketDataEntity() = ExternalMarketDataEntity(
        id = id, eventId = eventId, sourceId = sourceId, probability = probability,
        sampleSize = sampleSize, recordedAt = recordedAt
    )

    private fun DivergenceFlagDto.toFlagEntity() = DivergenceFlagEntity(
        id = id, eventId = eventId, internalConsensusProbability = internalConsensusProbability,
        externalConsensusProbability = externalConsensusProbability, divergenceScore = divergenceScore,
        flagType = flagType, status = status, summary = summary, reviewedBy = null, reviewedAt = null,
        createdAt = createdAt, updatedAt = createdAt
    )

    private fun HedgeRecommendationDto.toHedgeEntity() = HedgeRecommendationEntity(
        id = id, flagId = flagId, recommendedAction = recommendedAction, instrumentType = instrumentType,
        notionalAmount = notionalAmount, currency = currency, status = status,
        executionPartner = executionPartner, createdAt = createdAt, updatedAt = createdAt,
        approvedBy = null, approvedAt = null, executedAt = null
    )

    private fun AuditLogDto.toAuditEntity() = AuditLogEntity(
        id = id, entityType = entityType, entityId = entityId, action = action,
        performedBy = performedBy, details = details, createdAt = createdAt
    )

    private fun TrackedEventDto.toTrackedEventEntity() = TrackedEventEntity(
        id = this.id,
        companyId = this.companyId,
        title = this.title,
        description = this.description,
        category = this.category,
        resolutionDate = this.resolutionDate,
        exposureAmount = this.exposureAmount,
        currency = this.currency,
        status = this.status,
        createdBy = this.createdBy,
        createdAt = this.createdAt,
        updatedAt = this.createdAt
    )

    fun getFlaggedEvents(): Flow<List<FlaggedEvent>> {
        return eventDao.getAllEvents().combine(flagDao.getAllFlags()) { events, flags ->
            events.map { event ->
                FlaggedEvent(
                    event = event,
                    flag = flags.find { it.eventId == event.id }
                )
            }
        }
    }

    suspend fun getEventById(id: Long): TrackedEventEntity? = eventDao.getEventById(id)

    fun getEventByIdFlow(id: Long): Flow<TrackedEventEntity?> = eventDao.getEventByIdFlow(id)

    suspend fun insertForecast(forecast: InternalForecastEntity): Long {
        val result = forecastDao.insertForecast(forecast)
        if (result > 0) {
            updateConsensusForEvent(forecast.eventId)
            
            // Sync to MySQL Backend
            try {
                apiService.submitForecast(InternalForecastDto(
                    eventId = forecast.eventId,
                    userId = forecast.userId,
                    probability = forecast.probability,
                    rationale = forecast.rationale,
                    submittedAt = forecast.submittedAt
                ))
            } catch (e: Exception) {
                e.printStackTrace()
                // In a production app, we'd queue this for retry
            }
        }
        return result
    }

    private suspend fun updateConsensusForEvent(eventId: Long) {
        val event = eventDao.getEventById(eventId) ?: return
        val forecasts = forecastDao.getForecastsByEvent(eventId).first()
        val marketDataList = marketDao.getMarketDataByEventSorted(eventId).first()
        val marketData = marketDataList.firstOrNull()

        if (forecasts.isEmpty()) return

        var totalWeight = 0.0
        var weightedSum = 0.0

        forecasts.forEach { forecast ->
            val user = userDao.getUserById(forecast.userId)
            val weight = if (user?.calibrationScore != null) {
                (1.0 - user.calibrationScore).coerceIn(0.1, 1.0)
            } else {
                0.5
            }
            weightedSum += forecast.probability * weight
            totalWeight += weight
        }

        val internalConsensus = weightedSum / totalWeight
        val externalConsensus = marketData?.probability ?: 50.0
        val sampleSize = marketData?.sampleSize ?: 1000

        val score = calculateRefinedDivergenceScore(
            internalConsensus, 
            externalConsensus, 
            sampleSize, 
            event.exposureAmount
        )
        val type = if (externalConsensus > internalConsensus) FlagType.BLIND_SPOT else FlagType.OPPORTUNITY
        val timestamp = System.currentTimeMillis().toString()

        val existingFlag = flagDao.getFlagByEvent(eventId)
        if (existingFlag != null) {
            flagDao.updateFlag(existingFlag.copy(
                internalConsensusProbability = internalConsensus,
                externalConsensusProbability = externalConsensus,
                divergenceScore = score,
                flagType = type,
                updatedAt = timestamp
            ))
        } else {
            flagDao.insertFlag(DivergenceFlagEntity(
                eventId = eventId,
                internalConsensusProbability = internalConsensus,
                externalConsensusProbability = externalConsensus,
                divergenceScore = score,
                flagType = type,
                status = FlagStatus.OPEN,
                summary = "Initial automated divergence detection.",
                reviewedBy = null,
                reviewedAt = null,
                createdAt = timestamp,
                updatedAt = timestamp
            ))
        }
    }

    private fun calculateRefinedDivergenceScore(
        internalProb: Double,
        externalProb: Double,
        externalSampleSize: Int,
        exposureAmount: java.math.BigDecimal?
    ): Double {
        val rawGap = abs(internalProb - externalProb)
        val sampleWeight = (kotlin.math.log10(externalSampleSize.toDouble()) / 4.0).coerceIn(0.5, 1.2)
        
        // Exposure weighting: normalize exposure around a base of $10M
        val exposureWeight = if (exposureAmount != null) {
            (exposureAmount.toDouble() / 10_000_000.0).coerceIn(0.8, 2.0)
        } else {
            1.0
        }
        
        return rawGap * sampleWeight * exposureWeight
    }

    fun getForecastsByUser(userId: Long): Flow<List<InternalForecastEntity>> =
        forecastDao.getForecastsByUser(userId)

    suspend fun insertEvent(event: TrackedEventEntity): Long = eventDao.insertEvent(event)

    suspend fun insertFlag(flag: DivergenceFlagEntity): Long = flagDao.insertFlag(flag)

    suspend fun getFlagById(id: Long): DivergenceFlagEntity? = flagDao.getFlagById(id)

    fun getAllFlags(): Flow<List<DivergenceFlagEntity>> = flagDao.getAllFlags()

    fun getFlagWithEventById(flagId: Long): Flow<FlaggedEvent?> {
        return flagDao.getFlagByIdFlow(flagId).combine(eventDao.getAllEvents()) { flag: DivergenceFlagEntity?, events: List<TrackedEventEntity> ->
            if (flag == null) null
            else FlaggedEvent(event = events.first { it.id == flag.eventId }, flag = flag)
        }
    }

    // Audit Log methods
    suspend fun logAction(log: AuditLogEntity): Long {
        val id = auditLogDao.insertLog(log)
        
        // Write-back to MySQL Backend
        try {
            apiService.postAuditLog(AuditLogDto(
                id = 0, // Backend will auto-generate
                entityType = log.entityType,
                entityId = log.entityId,
                action = log.action,
                performedBy = log.performedBy,
                details = log.details,
                createdAt = log.createdAt
            ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return id
    }

    fun getAllAuditLogs(): Flow<List<AuditLogEntity>> = auditLogDao.getAllLogs()

    fun getLogsForEntity(entityType: String, entityId: Long): Flow<List<AuditLogEntity>> =
        auditLogDao.getLogsForEntity(entityType, entityId)

    // Hedge methods
    fun getRecommendationsByFlag(flagId: Long): Flow<List<HedgeRecommendationEntity>> =
        hedgeDao.getRecommendationsByFlag(flagId)

    suspend fun updateHedgeRecommendation(recommendation: HedgeRecommendationEntity) {
        hedgeDao.updateRecommendation(recommendation)
        
        // Sync status to MySQL Backend
        try {
            apiService.updateHedgeStatus(
                id = recommendation.id,
                statusUpdate = mapOf(
                    "status" to recommendation.status.name,
                    "execution_partner" to (recommendation.executionPartner ?: "")
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getHedgeRecommendationById(id: Long) = hedgeDao.getRecommendationById(id)

    suspend fun insertHedgeRecommendation(recommendation: HedgeRecommendationEntity) =
        hedgeDao.insertRecommendation(recommendation)

    fun getAllHedgeRecommendations(): Flow<List<HedgeRecommendationEntity>> = hedgeDao.getAllRecommendations()

    // User/Calibration methods
    fun getUserFlow(userId: Long): Flow<UserEntity?> = userDao.getUserFlow(userId)

    fun getResolvedForecastsByUser(userId: Long): Flow<List<ResolvedForecast>> =
        forecastDao.getResolvedForecastsByUser(userId)

    fun getResolvedForecastsWithFlagByUser(userId: Long): Flow<List<ResolvedForecastWithFlag>> =
        forecastDao.getResolvedForecastsWithFlagByUser(userId)

    fun getForecastsByEvent(eventId: Long): Flow<List<InternalForecastEntity>> =
        forecastDao.getForecastsByEventSorted(eventId)

    fun getMarketDataByEvent(eventId: Long): Flow<List<ExternalMarketDataEntity>> =
        marketDao.getMarketDataByEventSorted(eventId)

    // Market Simulation methods
    suspend fun simulateMarketUpdate(eventId: Long, newProbability: Double) {
        val timestamp = System.currentTimeMillis().toString()
        
        // 1. Record new market data
        marketDao.insertMarketData(ExternalMarketDataEntity(
            eventId = eventId,
            sourceId = 1, // Global Prediction Exchange
            probability = newProbability,
            sampleSize = (500..2000).random(),
            recordedAt = timestamp
        ))

        // 2. Update the Divergence Flag
        val event = eventDao.getEventById(eventId)
        val existingFlag = flagDao.getFlagByEvent(eventId)
        if (existingFlag != null && event != null) {
            val score = calculateRefinedDivergenceScore(
                existingFlag.internalConsensusProbability,
                newProbability,
                (500..2000).last(), // Using the same sample size range as recorded above
                event.exposureAmount
            )
            val type = if (newProbability > existingFlag.internalConsensusProbability) {
                FlagType.BLIND_SPOT
            } else {
                FlagType.OPPORTUNITY
            }

            flagDao.updateFlag(existingFlag.copy(
                externalConsensusProbability = newProbability,
                divergenceScore = score,
                flagType = type,
                updatedAt = timestamp
            ))

            logAction(AuditLogEntity(
                entityType = "divergence_flags",
                entityId = existingFlag.id,
                action = AuditAction.UPDATE,
                details = "{\"new_external_prob\": $newProbability, \"new_score\": $score}",
                createdAt = timestamp,
                performedBy = null // System action
            ))
        }
    }

    suspend fun resolveEvent(eventId: Long, status: EventStatus) {
        val event = eventDao.getEventById(eventId) ?: return
        eventDao.updateEvent(event.copy(status = status))

        if (status == EventStatus.RESOLVED_YES || status == EventStatus.RESOLVED_NO) {
            // Update calibration for all users who contributed to this event
            val forecasts = forecastDao.getForecastsByEvent(eventId).first()
            forecasts.forEach { forecast ->
                updateUserCalibration(forecast.userId)
            }

            val timestamp = System.currentTimeMillis().toString()
            logAction(AuditLogEntity(
                entityType = "tracked_events",
                entityId = eventId,
                action = AuditAction.UPDATE,
                details = "{\"new_status\": \"$status\"}",
                createdAt = timestamp,
                performedBy = null // System action or current admin
            ))
        }
    }

    private suspend fun updateUserCalibration(userId: Long) {
        val history = forecastDao.getResolvedForecastsByUser(userId).first()
        val resolvedItems = history.filter {
            it.event.status == EventStatus.RESOLVED_YES || it.event.status == EventStatus.RESOLVED_NO
        }

        if (resolvedItems.isEmpty()) return

        val brierScore = resolvedItems.map { item ->
            val outcome = if (item.event.status == EventStatus.RESOLVED_YES) 1.0 else 0.0
            val p = item.forecast.probability / 100.0
            (p - outcome) * (p - outcome)
        }.average()

        val user = userDao.getUserById(userId)
        if (user != null) {
            userDao.updateUser(user.copy(calibrationScore = brierScore))
        }
    }

    // Notification methods
    fun getNotificationsByUser(userId: Long): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsByUser(userId)

    suspend fun insertNotification(notification: NotificationEntity): Long =
        notificationDao.insertNotification(notification)

    suspend fun markNotificationAsRead(id: Long) = notificationDao.markAsRead(id)
}
