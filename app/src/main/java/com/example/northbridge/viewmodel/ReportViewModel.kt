package com.example.northbridge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.northbridge.db.entity.DivergenceFlagEntity
import com.example.northbridge.db.entity.HedgeRecommendationEntity
import com.example.northbridge.model.FlaggedEvent
import com.example.northbridge.model.HedgeStatus
import com.example.northbridge.repository.RiskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal

data class ReportMetrics(
    val hedgeCoverage: Int,
    val valueProtected: BigDecimal,
    val activeFlagsCount: Int,
    val topRisks: List<FlaggedEvent>
)

class ReportViewModel(private val repository: RiskRepository) : ViewModel() {

    val reportMetrics: StateFlow<ReportMetrics?> = combine(
        repository.getFlaggedEvents(),
        repository.getAllHedgeRecommendations()
    ) { events, hedges ->
        val executedHedges = hedges.filter { it.status == HedgeStatus.EXECUTED }
        val coverage = if (hedges.isNotEmpty()) (executedHedges.size * 100 / hedges.size) else 0
        val totalProtected = executedHedges.sumOf { it.notionalAmount }
        
        val topRisks = events.filter { it.flag != null && it.flag.divergenceScore > 15 }
            .sortedByDescending { it.flag?.divergenceScore }

        ReportMetrics(
            hedgeCoverage = coverage,
            valueProtected = totalProtected,
            activeFlagsCount = events.count { it.flag != null },
            topRisks = topRisks
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    class Factory(private val repository: RiskRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportViewModel(repository) as T
        }
    }
}
