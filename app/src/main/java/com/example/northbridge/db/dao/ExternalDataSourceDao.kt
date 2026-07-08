package com.example.northbridge.db.dao

import androidx.room.*
import com.example.northbridge.db.entity.ExternalDataSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExternalDataSourceDao {
    @Query("SELECT * FROM external_data_sources")
    fun getAllSources(): Flow<List<ExternalDataSourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: ExternalDataSourceEntity): Long
}
