package com.example.northbridge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.northbridge.db.entity.UserEntity
import com.example.northbridge.model.EventStatus
import com.example.northbridge.model.ResolvedForecast
import com.example.northbridge.model.ResolvedForecastWithFlag
import com.example.northbridge.model.SessionManager
import com.example.northbridge.repository.RiskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CalibrationViewModel(
    private val repository: RiskRepository
) : ViewModel() {

    private val userId = SessionManager.currentUser.value?.id ?: -1L

    val user: StateFlow<UserEntity?> = repository.getUserFlow(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val forecastHistory: StateFlow<List<ResolvedForecast>> = repository.getResolvedForecastsByUser(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val historyWithFlags: StateFlow<List<ResolvedForecastWithFlag>> = repository.getResolvedForecastsWithFlagByUser(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val brierSkillScore: StateFlow<Double?> = historyWithFlags.map { history ->
        if (history.isEmpty()) return@map null

        val resolved = history.filter { 
            it.event.status == EventStatus.RESOLVED_YES || it.event.status == EventStatus.RESOLVED_NO 
        }
        if (resolved.isEmpty()) return@map null

        val brierScore = resolved.map { item ->
            val outcome = if (item.event.status == EventStatus.RESOLVED_YES) 1.0 else 0.0
            val p = item.forecast.probability / 100.0
            (p - outcome) * (p - outcome)
        }.average()

        val brierReference = resolved.map { item ->
            val outcome = if (item.event.status == EventStatus.RESOLVED_YES) 1.0 else 0.0
            // Market Benchmark: use external consensus if flag exists, otherwise 50/50
            val p = (item.flag?.externalConsensusProbability ?: 50.0) / 100.0
            (p - outcome) * (p - outcome)
        }.average()

        if (brierReference == 0.0) 0.0 else 1.0 - (brierScore / brierReference)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    class Factory(
        private val repository: RiskRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalibrationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CalibrationViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
