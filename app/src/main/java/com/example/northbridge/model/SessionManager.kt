package com.example.northbridge.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SessionManager {
    // Session is null by default for security; initialize via login flow
    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser

    fun switchUser(user: UserSession?) {
        _currentUser.value = user
    }

    data class UserSession(
        val id: Long,
        val role: UserRole,
        val name: String
    )
}
