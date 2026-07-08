package com.example.northbridge.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "external_market_data",
    indices = [
        Index(value = ["event_id"]),
        Index(value = ["source_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = TrackedEventEntity::class,
            parentColumns = ["id"],
            childColumns = ["event_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExternalDataSourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["source_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class ExternalMarketDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "event_id") val eventId: Long,
    @ColumnInfo(name = "source_id") val sourceId: Long,
    val probability: Double,
    @ColumnInfo(name = "sample_size") val sampleSize: Int?,
    @ColumnInfo(name = "recorded_at") val recordedAt: String
)
