package com.example.northbridge.model

import com.example.northbridge.db.entity.DivergenceFlagEntity
import com.example.northbridge.db.entity.TrackedEventEntity

data class FlaggedEvent(
    val event: TrackedEventEntity,
    val flag: DivergenceFlagEntity?
)
