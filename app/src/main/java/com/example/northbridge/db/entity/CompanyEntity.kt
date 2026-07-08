package com.example.northbridge.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "companies",
    indices = [Index(value = ["name"], unique = true)]
)
data class CompanyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val industry: String,
    @ColumnInfo(name = "country_code") val countryCode: String,
    val headquarters: String?,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String
)
