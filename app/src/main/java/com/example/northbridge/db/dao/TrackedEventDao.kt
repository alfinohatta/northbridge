package com.example.northbridge.db.dao

import androidx.room.*
import com.example.northbridge.db.entity.TrackedEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedEventDao {
    @Query("SELECT * FROM tracked_events")
    fun getAllEvents(): Flow<List<TrackedEventEntity>>

    @Query("SELECT * FROM tracked_events WHERE id = :id")
    suspend fun getEventById(id: Long): TrackedEventEntity?

    @Query("SELECT * FROM tracked_events WHERE id = :id")
    fun getEventByIdFlow(id: Long): Flow<TrackedEventEntity?>

    @Query("SELECT * FROM tracked_events WHERE company_id = :companyId")
    fun getEventsByCompany(companyId: Long): Flow<List<TrackedEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: TrackedEventEntity): Long

    @Update
    suspend fun updateEvent(event: TrackedEventEntity)

    @Delete
    suspend fun deleteEvent(event: TrackedEventEntity)
}
