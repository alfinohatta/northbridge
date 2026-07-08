package com.example.northbridge.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["user_id"])],
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
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long,
    val title: String,
    val message: String,
    @ColumnInfo(name = "event_id") val eventId: Long?,
    @ColumnInfo(name = "is_read") val isRead: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: String
)
