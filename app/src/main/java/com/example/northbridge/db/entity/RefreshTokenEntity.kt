package com.example.northbridge.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "refresh_tokens",
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
data class RefreshTokenEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "token_hash") val tokenHash: String,
    @ColumnInfo(name = "expires_at") val expiresAt: String,
    val revoked: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: String
)
