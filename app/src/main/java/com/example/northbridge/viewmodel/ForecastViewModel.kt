package com.example.northbridge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.northbridge.db.entity.AuditLogEntity
import com.example.northbridge.db.entity.InternalForecastEntity
import com.example.northbridge.db.entity.TrackedEventEntity
import com.example.northbridge.model.AuditAction
import com.example.northbridge.repository.RiskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForecastViewModel(
    private val repository: RiskRepository,
    private val eventId: Long,
    private val userId: Long
) : ViewModel() {

    private val _event = MutableStateFlow<TrackedEventEntity?>(null)
    val event: StateFlow<TrackedEventEntity?> = _event.asStateFlow()

    private val _submissionStatus = MutableStateFlow<Boolean?>(null)
    val submissionStatus: StateFlow<Boolean?> = _submissionStatus.asStateFlow()

    init {
        loadEvent()
    }

    private fun loadEvent() {
        viewModelScope.launch {
            _event.value = repository.getEventById(eventId)
        }
    }

    fun submitForecast(probability: Double, rationale: String?) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis().toString()
            val forecast = InternalForecastEntity(
                eventId = eventId,
                userId = userId,
                probability = probability,
                rationale = rationale,
                submittedAt = timestamp
            )
            val result = repository.insertForecast(forecast)
            
            if (result > 0) {
                repository.logAction(AuditLogEntity(
                    entityType = "internal_forecasts",
                    entityId = result,
                    action = AuditAction.CREATE,
                    performedBy = userId,
                    details = "{\"probability\": $probability}",
                    createdAt = timestamp
                ))
                _submissionStatus.value = true
            } else {
                _submissionStatus.value = false
            }
        }
    }

    class Factory(
        private val repository: RiskRepository,
        private val eventId: Long,
        private val userId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ForecastViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ForecastViewModel(repository, eventId, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
