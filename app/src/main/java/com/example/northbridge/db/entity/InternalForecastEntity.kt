package com.example.northbridge.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "internal_forecasts",
    indices = [
        Index(value = ["event_id", "user_id"], unique = true),
        Index(value = ["event_id"]),
        Index(value = ["user_id"])
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
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class InternalForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "event_id") val eventId: Long,
    @ColumnInfo(name = "user_id") val userId: Long,
    val probability: Double,
    val rationale: String?,
    @ColumnInfo(name = "submitted_at") val submittedAt: String
)
