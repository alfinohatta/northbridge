package com.example.northbridge.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.northbridge.model.EventCategory
import com.example.northbridge.model.EventStatus
import java.math.BigDecimal

@Entity(
    tableName = "tracked_events",
    indices = [
        Index(value = ["company_id"]),
        Index(value = ["status"]),
        Index(value = ["resolution_date"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = CompanyEntity::class,
            parentColumns = ["id"],
            childColumns = ["company_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["created_by"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class TrackedEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "company_id") val companyId: Long,
    val title: String,
    val description: String?,
    val category: EventCategory,
    @ColumnInfo(name = "resolution_date") val resolutionDate: String,
    @ColumnInfo(name = "exposure_amount") val exposureAmount: BigDecimal?,
    val currency: String?,
    val status: EventStatus = EventStatus.ACTIVE,
    @ColumnInfo(name = "created_by") val createdBy: Long,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String
)
