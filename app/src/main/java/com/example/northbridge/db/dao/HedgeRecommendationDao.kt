package com.example.northbridge.db.dao

import androidx.room.*
import com.example.northbridge.db.entity.HedgeRecommendationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HedgeRecommendationDao {
    @Query("SELECT * FROM hedge_recommendations")
    fun getAllRecommendations(): Flow<List<HedgeRecommendationEntity>>

    @Query("SELECT * FROM hedge_recommendations WHERE flag_id = :flagId")
    fun getRecommendationsByFlag(flagId: Long): Flow<List<HedgeRecommendationEntity>>

    @Query("SELECT * FROM hedge_recommendations WHERE id = :id")
    suspend fun getRecommendationById(id: Long): HedgeRecommendationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: HedgeRecommendationEntity): Long

    @Update
    suspend fun updateRecommendation(recommendation: HedgeRecommendationEntity)

    @Delete
    suspend fun deleteRecommendation(recommendation: HedgeRecommendationEntity)
}
