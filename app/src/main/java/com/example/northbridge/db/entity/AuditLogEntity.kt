package com.example.northbridge.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.northbridge.model.AuditAction

@Entity(
    tableName = "audit_log",
    indices = [
        Index(value = ["entity_type", "entity_id"]),
        Index(value = ["performed_by"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["performed_by"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "entity_type") val entityType: String,
    @ColumnInfo(name = "entity_id") val entityId: Long,
    val action: AuditAction,
    @ColumnInfo(name = "performed_by") val performedBy: Long?,
    val details: String?, // Stores JSON string
    @ColumnInfo(name = "created_at") val createdAt: String
)
