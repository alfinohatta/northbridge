package com.example.northbridge.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.northbridge.db.dao.*
import com.example.northbridge.db.entity.*

@Database(
    entities = [
        CompanyEntity::class,
        UserEntity::class,
        DeviceTokenEntity::class,
        RefreshTokenEntity::class,
        TrackedEventEntity::class,
        InternalForecastEntity::class,
        ExternalDataSourceEntity::class,
        ExternalMarketDataEntity::class,
        DivergenceFlagEntity::class,
        HedgeRecommendationEntity::class,
        AuditLogEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun userDao(): UserDao
    abstract fun trackedEventDao(): TrackedEventDao
    abstract fun divergenceFlagDao(): DivergenceFlagDao
    abstract fun internalForecastDao(): InternalForecastDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun hedgeRecommendationDao(): HedgeRecommendationDao
    abstract fun externalMarketDataDao(): ExternalMarketDataDao
    abstract fun externalDataSourceDao(): ExternalDataSourceDao
    abstract fun deviceTokenDao(): DeviceTokenDao
    abstract fun refreshTokenDao(): RefreshTokenDao
    abstract fun notificationDao(): NotificationDao
}
