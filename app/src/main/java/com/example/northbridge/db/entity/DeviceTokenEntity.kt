package com.example.northbridge.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "device_tokens",
    indices = [
        Index(value = ["fcm_token"], unique = true),
        Index(value = ["user_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DeviceTokenEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "fcm_token") val fcmToken: String,
    val platform: String = "ANDROID",
    @ColumnInfo(name = "app_version") val appVersion: String?,
    @ColumnInfo(name = "created_at") val createdAt: String
)
