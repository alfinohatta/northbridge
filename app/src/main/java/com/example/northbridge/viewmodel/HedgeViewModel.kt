package com.example.northbridge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.northbridge.db.entity.AuditLogEntity
import com.example.northbridge.db.entity.HedgeRecommendationEntity
import com.example.northbridge.model.AuditAction
import com.example.northbridge.model.HedgeStatus
import com.example.northbridge.repository.RiskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HedgeViewModel(
    private val repository: RiskRepository,
    private val flagId: Long,
    private val userId: Long
) : ViewModel() {

    val recommendations: StateFlow<List<HedgeRecommendationEntity>> =
        repository.getRecommendationsByFlag(flagId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun updateStatus(recommendation: HedgeRecommendationEntity, newStatus: HedgeStatus) {
        viewModelScope.launch {
            val updated = recommendation.copy(
                status = newStatus,
                approvedBy = if (newStatus == HedgeStatus.APPROVED) userId else recommendation.approvedBy,
                approvedAt = if (newStatus == HedgeStatus.APPROVED) System.currentTimeMillis().toString() else recommendation.approvedAt,
                updatedAt = System.currentTimeMillis().toString()
            )
            repository.updateHedgeRecommendation(updated)

            repository.logAction(AuditLogEntity(
                entityType = "hedge_recommendations",
                entityId = recommendation.id,
                action = when (newStatus) {
                    HedgeStatus.APPROVED -> AuditAction.APPROVE
                    HedgeStatus.REJECTED -> AuditAction.REJECT
                    HedgeStatus.EXECUTED -> AuditAction.EXECUTE
                    else -> AuditAction.UPDATE
                },
                performedBy = userId,
                details = "{\"previous_status\": \"${recommendation.status}\", \"new_status\": \"$newStatus\"}",
                createdAt = System.currentTimeMillis().toString()
            ))
        }
    }

    fun executeDetailed(recommendation: HedgeRecommendationEntity, partner: String, reference: String) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis().toString()
            val updated = recommendation.copy(
                status = HedgeStatus.EXECUTED,
                executionPartner = partner,
                executedAt = timestamp,
                updatedAt = timestamp
            )
            repository.updateHedgeRecommendation(updated)

            repository.logAction(AuditLogEntity(
                entityType = "hedge_recommendations",
                entityId = recommendation.id,
                action = AuditAction.EXECUTE,
                performedBy = userId,
                details = "{\"partner\": \"$partner\", \"reference\": \"$reference\"}",
                createdAt = timestamp
            ))
        }
    }

    class Factory(
        private val repository: RiskRepository,
        private val flagId: Long,
        private val userId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HedgeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HedgeViewModel(repository, flagId, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
