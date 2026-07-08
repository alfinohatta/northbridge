package com.example.northbridge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.northbridge.db.entity.NotificationEntity
import com.example.northbridge.model.SessionManager
import com.example.northbridge.repository.RiskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: RiskRepository
) : ViewModel() {

    private val userId = SessionManager.currentUser.value?.id ?: -1L

    val notifications: StateFlow<List<NotificationEntity>> = repository.getNotificationsByUser(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun simulateNewPrompt() {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis().toString()
            repository.insertNotification(NotificationEntity(
                userId = userId,
                title = "Expert Input Required",
                message = "New volatility detected in the US Textile Tariff. Please provide an updated probability estimate.",
                eventId = 1L,
                createdAt = timestamp
            ))
        }
    }

    class Factory(
        private val repository: RiskRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NotificationViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
