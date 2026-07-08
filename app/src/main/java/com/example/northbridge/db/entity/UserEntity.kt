package com.example.northbridge.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.northbridge.model.UserRole

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["company_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = CompanyEntity::class,
            parentColumns = ["id"],
            childColumns = ["company_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "company_id") val companyId: Long,
    @ColumnInfo(name = "full_name") val fullName: String,
    val email: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    val role: UserRole,
    val department: String?,
    val region: String?,
    @ColumnInfo(name = "calibration_score") val calibrationScore: Double?,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String
)
