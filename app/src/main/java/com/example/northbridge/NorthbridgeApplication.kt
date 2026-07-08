package com.example.northbridge

import android.app.Application
import androidx.room.Room
import com.example.northbridge.db.AppDatabase
import com.example.northbridge.db.entity.*
import com.example.northbridge.model.*
import com.example.northbridge.repository.AuthRepository
import com.example.northbridge.repository.RiskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal

class NorthbridgeApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: AppDatabase
        private set

    lateinit var riskRepository: RiskRepository
        private set

    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "northbridge_db"
        ).build()

        riskRepository = RiskRepository(
            database.trackedEventDao(),
            database.divergenceFlagDao(),
            database.internalForecastDao(),
            database.auditLogDao(),
            database.hedgeRecommendationDao(),
            database.userDao(),
            database.externalMarketDataDao(),
            database.notificationDao(),
            database.companyDao(),
            database.externalDataSourceDao(),
            com.example.northbridge.api.NetworkClient.apiService
        )

        authRepository = com.example.northbridge.repository.AuthRepository(
            com.example.northbridge.api.NetworkClient.apiService,
            database.userDao(),
            database.deviceTokenDao()
        )

        applicationScope.launch {
            populateSampleData()
            // In a real app, register device here after login or on start
        }
    }

    private suspend fun populateSampleData() {
        val eventDao = database.trackedEventDao()
        val companyDao = database.companyDao()
        val userDao = database.userDao()
        val flagDao = database.divergenceFlagDao()
        val marketDao = database.externalMarketDataDao()
        val sourceDao = database.externalDataSourceDao()
        val hedgeDao = database.hedgeRecommendationDao()

        // check if data exists
        if (companyDao.getAllCompanies().first().isNotEmpty()) return

        // 0. External Data Sources
        sourceDao.insertSource(ExternalDataSourceEntity(
            id = 1,
            name = "Global Prediction Exchange",
            sourceType = SourceType.PREDICTION_MARKET,
            websiteUrl = "https://example-gpe.test",
            createdAt = "2026-07-07T12:00:00Z"
        ))
        sourceDao.insertSource(ExternalDataSourceEntity(
            id = 2,
            name = "Rate & Policy Futures Desk",
            sourceType = SourceType.DERIVATIVES_IMPLIED,
            websiteUrl = "https://example-rpfd.test",
            createdAt = "2026-07-07T12:00:00Z"
        ))
        sourceDao.insertSource(ExternalDataSourceEntity(
            id = 3,
            name = "SuperForecast Analyst Panel",
            sourceType = SourceType.FORECASTING_PANEL,
            websiteUrl = "https://example-sfap.test",
            createdAt = "2026-07-07T12:00:00Z"
        ))

        // 1. Meridian Consumer Goods (Scenario A)
        val meridianId = companyDao.insertCompany(CompanyEntity(
            name = "Meridian Consumer Goods Co.",
            industry = "Consumer Goods",
            countryCode = "US",
            headquarters = "Chicago, IL",
            createdAt = "2026-07-07T12:00:00Z",
            updatedAt = "2026-07-07T12:00:00Z"
        ))

        val marcusId = userDao.insertUser(UserEntity(
            companyId = meridianId,
            fullName = "Marcus Feld",
            email = "marcus.feld@meridiancg.com",
            passwordHash = "hash",
            role = UserRole.CRO,
            department = "Risk Management",
            region = "Global",
            calibrationScore = 0.1120,
            createdAt = "2026-07-07T12:00:00Z",
            updatedAt = "2026-07-07T12:00:00Z"
        ))

        val priyaId = userDao.insertUser(UserEntity(
            companyId = meridianId,
            fullName = "Priya Shah",
            email = "priya.shah@meridiancg.com",
            passwordHash = "hash",
            role = UserRole.FORECASTER,
            department = "Trade Compliance",
            region = "North America",
            calibrationScore = 0.0980,
            createdAt = "2026-07-07T12:00:00Z",
            updatedAt = "2026-07-07T12:00:00Z"
        ))

        userDao.insertUser(UserEntity(
            companyId = meridianId,
            fullName = "Elena Vargas",
            email = "elena.vargas@meridiancg.com",
            passwordHash = "hash",
            role = UserRole.CFO,
            department = "Finance",
            region = "Global",
            calibrationScore = null,
            createdAt = "2026-07-07T12:00:00Z",
            updatedAt = "2026-07-07T12:00:00Z"
        ))

        val eventId1 = eventDao.insertEvent(TrackedEventEntity(
            companyId = meridianId,
            title = "US Tariff on Imported Textiles Exceeds 15%",
            description = "Proposed tariff increase on imported textile raw materials used in Meridian's apparel line.",
            category = EventCategory.TARIFF,
            resolutionDate = "2026-09-30",
            exposureAmount = BigDecimal("18500000.00"),
            currency = "USD",
            status = EventStatus.ACTIVE,
            createdBy = marcusId,
            createdAt = "2026-07-07T12:00:00Z",
            updatedAt = "2026-07-07T12:00:00Z"
        ))

        database.internalForecastDao().insertForecast(InternalForecastEntity(
            eventId = eventId1, userId = marcusId, probability = 30.0, rationale = "initial", submittedAt = "2026-07-01T10:00:00Z"
        ))
        database.internalForecastDao().insertForecast(InternalForecastEntity(
            eventId = eventId1, userId = priyaId, probability = 35.0, rationale = "revision", submittedAt = "2026-07-05T10:00:00Z"
        ))

        marketDao.insertMarketData(ExternalMarketDataEntity(
            eventId = eventId1, sourceId = 1, probability = 45.0, sampleSize = 100, recordedAt = "2026-07-01T12:00:00Z"
        ))
        marketDao.insertMarketData(ExternalMarketDataEntity(
            eventId = eventId1, sourceId = 1, probability = 62.0, sampleSize = 500, recordedAt = "2026-07-07T12:00:00Z"
        ))

        val flagId1 = flagDao.insertFlag(DivergenceFlagEntity(
            eventId = eventId1,
            internalConsensusProbability = 35.00,
            externalConsensusProbability = 62.25,
            divergenceScore = 27.25,
            flagType = FlagType.BLIND_SPOT,
            status = FlagStatus.OPEN,
            summary = "External markets price the tariff meaningfully higher than internal trade-compliance estimates.",
            createdAt = "2026-07-07T12:10:00Z",
            updatedAt = "2026-07-07T12:10:00Z",
            reviewedBy = null,
            reviewedAt = null
        ))

        hedgeDao.insertRecommendation(HedgeRecommendationEntity(
            flagId = flagId1,
            recommendedAction = "Lock in a portion of textile input costs via a 6-month forward purchase agreement.",
            instrumentType = InstrumentType.FORWARD_CONTRACT,
            notionalAmount = BigDecimal("6000000.00"),
            currency = "USD",
            status = HedgeStatus.PROPOSED,
            executionPartner = "Crestline Commodities Partners",
            createdAt = "2026-07-07T12:20:00Z",
            updatedAt = "2026-07-07T12:20:00Z",
            approvedBy = null,
            approvedAt = null,
            executedAt = null
        ))

        // 2. Vantage Pharmaceuticals (Scenario B)
        val vantageId = companyDao.insertCompany(CompanyEntity(
            name = "Vantage Pharmaceuticals Inc.",
            industry = "Pharmaceuticals",
            countryCode = "DE",
            headquarters = "Frankfurt, Germany",
            createdAt = "2026-07-07T12:00:00Z",
            updatedAt = "2026-07-07T12:00:00Z"
        ))

        val sofiaId = userDao.insertUser(UserEntity(
            companyId = vantageId,
            fullName = "Sofia Bauer",
            email = "sofia.bauer@vantagepharma.de",
            passwordHash = "hash",
            role = UserRole.CRO,
            department = "Risk Management",
            region = "EMEA",
            calibrationScore = 0.1050,
            createdAt = "2026-07-07T12:00:00Z",
            updatedAt = "2026-07-07T12:00:00Z"
        ))

        val eventId2 = eventDao.insertEvent(TrackedEventEntity(
            companyId = vantageId,
            title = "FDA Approval Delay for Compound VX-221",
            description = "Risk that the FDA pushes the target approval date for Compound VX-221 beyond Q4 2026.",
            category = EventCategory.REGULATORY,
            resolutionDate = "2026-12-15",
            exposureAmount = BigDecimal("42000000.00"),
            currency = "EUR",
            status = EventStatus.ACTIVE,
            createdBy = sofiaId,
            createdAt = "2026-07-07T12:00:00Z",
            updatedAt = "2026-07-07T12:00:00Z"
        ))

        val flagId2 = flagDao.insertFlag(DivergenceFlagEntity(
            eventId = eventId2,
            internalConsensusProbability = 80.00,
            externalConsensusProbability = 50.00,
            divergenceScore = 30.00,
            flagType = FlagType.BLIND_SPOT,
            status = FlagStatus.UNDER_REVIEW,
            summary = "Internal team is significantly more confident in scheduled approval than external forecasters.",
            createdAt = "2026-07-07T12:10:00Z",
            updatedAt = "2026-07-07T12:10:00Z",
            reviewedBy = sofiaId,
            reviewedAt = "2026-07-07T12:15:00Z"
        ))

        // 3. Atlas Logistics (Scenario C)
        val atlasId = companyDao.insertCompany(CompanyEntity(
            name = "Atlas Logistics Group",
            industry = "Logistics",
            countryCode = "SG",
            headquarters = "Singapore",
            createdAt = "2026-07-07T12:00:00Z",
            updatedAt = "2026-07-07T12:00:00Z"
        ))

        val weiId = userDao.insertUser(UserEntity(
            companyId = atlasId,
            fullName = "Wei Chen",
            email = "wei.chen@atlaslogistics.sg",
            passwordHash = "hash",
            role = UserRole.CRO,
            department = "Risk Management",
            region = "APAC",
            calibrationScore = 0.0890,
            createdAt = "2026-07-07T12:00:00Z",
            updatedAt = "2026-07-07T12:00:00Z"
        ))

        val eventId3 = eventDao.insertEvent(TrackedEventEntity(
            companyId = atlasId,
            title = "Labor Strike at Port of Rotterdam",
            description = "Risk of a labor action disrupting container throughput at the Port of Rotterdam.",
            category = EventCategory.SUPPLY_CHAIN,
            resolutionDate = "2026-08-15",
            exposureAmount = BigDecimal("9750000.00"),
            currency = "USD",
            status = EventStatus.ACTIVE,
            createdBy = weiId,
            createdAt = "2026-07-07T12:00:00Z",
            updatedAt = "2026-07-07T12:00:00Z"
        ))

        flagDao.insertFlag(DivergenceFlagEntity(
            eventId = eventId3,
            internalConsensusProbability = 55.00,
            externalConsensusProbability = 34.00,
            divergenceScore = 21.00,
            flagType = FlagType.OPPORTUNITY,
            status = FlagStatus.OPEN,
            summary = "Internal port-operations view suggests materially higher strike risk than public data reflects.",
            createdAt = "2026-07-07T12:10:00Z",
            updatedAt = "2026-07-07T12:10:00Z",
            reviewedBy = null,
            reviewedAt = null
        ))

        database.notificationDao().insertNotification(NotificationEntity(
            userId = marcusId,
            title = "New Divergence Flag",
            message = "A new BLIND_SPOT flag has been raised for 'Labor Strike at Port of Rotterdam'. External markets are significantly more pessimistic.",
            eventId = eventId3,
            createdAt = "2026-07-07T12:15:00Z"
        ))
    }
}
