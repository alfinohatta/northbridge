package com.example.northbridge.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.northbridge.db.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_log ORDER BY created_at DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_log WHERE entity_type = :entityType AND entity_id = :entityId ORDER BY created_at DESC")
    fun getLogsForEntity(entityType: String, entityId: Long): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity): Long
}
