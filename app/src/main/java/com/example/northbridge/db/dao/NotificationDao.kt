package com.example.northbridge.db.dao

import androidx.room.*
import com.example.northbridge.db.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY created_at DESC")
    fun getNotificationsByUser(userId: Long): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET is_read = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)
}
