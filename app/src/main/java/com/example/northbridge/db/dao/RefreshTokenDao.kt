package com.example.northbridge.db.dao

import androidx.room.*
import com.example.northbridge.db.entity.RefreshTokenEntity

@Dao
interface RefreshTokenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(token: RefreshTokenEntity): Long

    @Query("SELECT * FROM refresh_tokens WHERE user_id = :userId AND revoked = 0")
    suspend fun getActiveTokenForUser(userId: Long): RefreshTokenEntity?

    @Query("UPDATE refresh_tokens SET revoked = 1 WHERE user_id = :userId")
    suspend fun revokeTokensForUser(userId: Long)
}
