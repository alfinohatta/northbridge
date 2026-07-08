package com.example.northbridge.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.northbridge.model.HedgeStatus
import com.example.northbridge.model.InstrumentType
import java.math.BigDecimal

@Entity(
    tableName = "hedge_recommendations",
    indices = [
        Index(value = ["flag_id"]),
        Index(value = ["status"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = DivergenceFlagEntity::class,
            parentColumns = ["id"],
            childColumns = ["flag_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["approved_by"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class HedgeRecommendationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "flag_id") val flagId: Long,
    @ColumnInfo(name = "recommended_action") val recommendedAction: String,
    @ColumnInfo(name = "instrument_type") val instrumentType: InstrumentType,
    @ColumnInfo(name = "notional_amount") val notionalAmount: BigDecimal,
    val currency: String,
    val status: HedgeStatus = HedgeStatus.PROPOSED,
    @ColumnInfo(name = "execution_partner") val executionPartner: String?,
    @ColumnInfo(name = "approved_by") val approvedBy: Long?,
    @ColumnInfo(name = "approved_at") val approvedAt: String?,
    @ColumnInfo(name = "executed_at") val executedAt: String?,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String
)
