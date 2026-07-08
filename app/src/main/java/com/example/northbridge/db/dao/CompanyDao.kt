package com.example.northbridge.db.dao

import androidx.room.*
import com.example.northbridge.db.entity.CompanyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies")
    fun getAllCompanies(): Flow<List<CompanyEntity>>

    @Query("SELECT * FROM companies WHERE id = :id")
    suspend fun getCompanyById(id: Long): CompanyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CompanyEntity): Long

    @Update
    suspend fun updateCompany(company: CompanyEntity)

    @Delete
    suspend fun deleteCompany(company: CompanyEntity)
}
