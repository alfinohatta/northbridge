package com.example.northbridge.db.dao

import androidx.room.*
import com.example.northbridge.db.entity.DeviceTokenEntity

@Dao
interface DeviceTokenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(token: DeviceTokenEntity): Long

    @Query("SELECT * FROM device_tokens WHERE user_id = :userId")
    suspend fun getTokenForUser(userId: Long): DeviceTokenEntity?
}
