package com.example.northbridge.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.northbridge.db.entity.DivergenceFlagEntity
import com.example.northbridge.db.entity.InternalForecastEntity
import com.example.northbridge.db.entity.TrackedEventEntity

data class ResolvedForecastWithFlag(
    @Embedded val forecast: InternalForecastEntity,
    @Relation(
        parentColumn = "event_id",
        entityColumn = "id"
    )
    val event: TrackedEventEntity,
    @Relation(
        parentColumn = "event_id",
        entityColumn = "event_id"
    )
    val flag: DivergenceFlagEntity?
)
