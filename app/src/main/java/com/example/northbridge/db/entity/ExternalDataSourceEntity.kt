package com.example.northbridge.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.northbridge.model.SourceType

@Entity(
    tableName = "external_data_sources",
    indices = [Index(value = ["name"], unique = true)]
)
data class ExternalDataSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "source_type") val sourceType: SourceType,
    @ColumnInfo(name = "website_url") val websiteUrl: String?,
    @ColumnInfo(name = "is_licensed") val isLicensed: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: String
)
