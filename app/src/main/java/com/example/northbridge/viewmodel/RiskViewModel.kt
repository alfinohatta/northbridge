package com.example.northbridge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.northbridge.db.entity.AuditLogEntity
import com.example.northbridge.model.FlaggedEvent
import com.example.northbridge.model.SessionManager
import com.example.northbridge.repository.RiskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RiskViewModel(private val repository: RiskRepository) : ViewModel() {

    val currentUser = SessionManager.currentUser

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val flaggedEvents: StateFlow<List<FlaggedEvent>> = repository.getFlaggedEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.getAllAuditLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun simulateMarketSignals() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.syncWithBackend()
            _isRefreshing.value = false
        }
    }

    fun resolveEventSim(eventId: Long, outcome: com.example.northbridge.model.EventStatus) {
        viewModelScope.launch {
            repository.resolveEvent(eventId, outcome)
        }
    }

    class Factory(private val repository: RiskRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RiskViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RiskViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
