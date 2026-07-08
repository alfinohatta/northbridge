package com.example.northbridge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.northbridge.db.entity.ExternalMarketDataEntity
import com.example.northbridge.db.entity.InternalForecastEntity
import com.example.northbridge.db.entity.TrackedEventEntity
import com.example.northbridge.repository.RiskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventTrendViewModel(
    private val repository: RiskRepository,
    private val eventId: Long
) : ViewModel() {

    val event: StateFlow<TrackedEventEntity?> = repository.getEventByIdFlow(eventId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val internalForecasts: StateFlow<List<InternalForecastEntity>> = repository.getForecastsByEvent(eventId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val externalMarketData: StateFlow<List<ExternalMarketDataEntity>> = repository.getMarketDataByEvent(eventId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    class Factory(
        private val repository: RiskRepository,
        private val eventId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventTrendViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EventTrendViewModel(repository, eventId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
