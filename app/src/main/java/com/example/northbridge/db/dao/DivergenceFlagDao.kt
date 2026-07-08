package com.example.northbridge.db.dao

import androidx.room.*
import com.example.northbridge.db.entity.DivergenceFlagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DivergenceFlagDao {
    @Query("SELECT * FROM divergence_flags")
    fun getAllFlags(): Flow<List<DivergenceFlagEntity>>

    @Query("SELECT * FROM divergence_flags WHERE id = :id")
    suspend fun getFlagById(id: Long): DivergenceFlagEntity?

    @Query("SELECT * FROM divergence_flags WHERE id = :id")
    fun getFlagByIdFlow(id: Long): Flow<DivergenceFlagEntity?>

    @Query("SELECT * FROM divergence_flags WHERE event_id = :eventId")
    suspend fun getFlagByEvent(eventId: Long): DivergenceFlagEntity?

    @Query("SELECT * FROM divergence_flags WHERE event_id = :eventId")
    fun getFlagsByEvent(eventId: Long): Flow<List<DivergenceFlagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlag(flag: DivergenceFlagEntity): Long

    @Update
    suspend fun updateFlag(flag: DivergenceFlagEntity)

    @Delete
    suspend fun deleteFlag(flag: DivergenceFlagEntity)
}
