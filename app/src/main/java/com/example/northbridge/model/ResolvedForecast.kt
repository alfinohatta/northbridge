package com.example.northbridge.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.northbridge.db.entity.InternalForecastEntity
import com.example.northbridge.db.entity.TrackedEventEntity

data class ResolvedForecast(
    @Embedded val forecast: InternalForecastEntity,
    @Relation(
        parentColumn = "event_id",
        entityColumn = "id"
    )
    val event: TrackedEventEntity
)
