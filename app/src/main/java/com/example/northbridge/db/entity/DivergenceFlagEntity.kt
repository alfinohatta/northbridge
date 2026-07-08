package com.example.northbridge.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.northbridge.model.FlagStatus
import com.example.northbridge.model.FlagType

@Entity(
    tableName = "divergence_flags",
    indices = [
        Index(value = ["event_id"]),
        Index(value = ["status"])
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
            childColumns = ["reviewed_by"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DivergenceFlagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "event_id") val eventId: Long,
    @ColumnInfo(name = "internal_consensus_probability") val internalConsensusProbability: Double,
    @ColumnInfo(name = "external_consensus_probability") val externalConsensusProbability: Double,
    @ColumnInfo(name = "divergence_score") val divergenceScore: Double,
    @ColumnInfo(name = "flag_type") val flagType: FlagType,
    val status: FlagStatus = FlagStatus.OPEN,
    val summary: String?,
    @ColumnInfo(name = "reviewed_by") val reviewedBy: Long?,
    @ColumnInfo(name = "reviewed_at") val reviewedAt: String?,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String
)
