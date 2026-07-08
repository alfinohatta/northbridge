package com.example.northbridge.db.dao

import androidx.room.*
import com.example.northbridge.db.entity.InternalForecastEntity
import com.example.northbridge.model.ResolvedForecast
import com.example.northbridge.model.ResolvedForecastWithFlag
import kotlinx.coroutines.flow.Flow

@Dao
interface InternalForecastDao {
    @Query("SELECT * FROM internal_forecasts WHERE event_id = :eventId")
    fun getForecastsByEvent(eventId: Long): Flow<List<InternalForecastEntity>>

    @Query("SELECT * FROM internal_forecasts WHERE event_id = :eventId ORDER BY submitted_at ASC")
    fun getForecastsByEventSorted(eventId: Long): Flow<List<InternalForecastEntity>>

    @Transaction
    @Query("SELECT * FROM internal_forecasts WHERE user_id = :userId")
    fun getResolvedForecastsByUser(userId: Long): Flow<List<ResolvedForecast>>

    @Transaction
    @Query("SELECT * FROM internal_forecasts WHERE user_id = :userId")
    fun getResolvedForecastsWithFlagByUser(userId: Long): Flow<List<ResolvedForecastWithFlag>>

    @Query("SELECT * FROM internal_forecasts WHERE user_id = :userId")
    fun getForecastsByUser(userId: Long): Flow<List<InternalForecastEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: InternalForecastEntity): Long

    @Update
    suspend fun updateForecast(forecast: InternalForecastEntity)

    @Delete
    suspend fun deleteForecast(forecast: InternalForecastEntity)
}
