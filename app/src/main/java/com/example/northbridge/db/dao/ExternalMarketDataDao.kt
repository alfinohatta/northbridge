package com.example.northbridge.db.dao

import androidx.room.*
import com.example.northbridge.db.entity.ExternalMarketDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExternalMarketDataDao {
    @Query("SELECT * FROM external_market_data WHERE event_id = :eventId")
    fun getMarketDataByEvent(eventId: Long): Flow<List<ExternalMarketDataEntity>>

    @Query("SELECT * FROM external_market_data WHERE event_id = :eventId ORDER BY recorded_at ASC")
    fun getMarketDataByEventSorted(eventId: Long): Flow<List<ExternalMarketDataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketData(data: ExternalMarketDataEntity): Long

    @Query("DELETE FROM external_market_data WHERE event_id = :eventId")
    suspend fun deleteMarketDataForEvent(eventId: Long)
}
