package com.example.northbridge.repository

import com.example.northbridge.api.DeviceTokenDto
import com.example.northbridge.api.NorthbridgeApiService
import com.example.northbridge.db.dao.DeviceTokenDao
import com.example.northbridge.db.dao.UserDao
import com.example.northbridge.db.entity.DeviceTokenEntity
import com.example.northbridge.db.entity.UserEntity

class AuthRepository(
    private val apiService: NorthbridgeApiService,
    private val userDao: UserDao,
    private val deviceTokenDao: DeviceTokenDao
) {
    suspend fun registerDevice(userId: Long, fcmToken: String) {
        val timestamp = System.currentTimeMillis().toString()
        val dto = DeviceTokenDto(
            userId = userId,
            fcmToken = fcmToken,
            platform = "ANDROID",
            appVersion = "1.0.0"
        )
        
        try {
            apiService.registerDevice(dto)
            deviceTokenDao.insertToken(DeviceTokenEntity(
                userId = userId,
                fcmToken = fcmToken,
                appVersion = "1.0.0",
                createdAt = timestamp
            ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun refreshToken(userId: Long, oldToken: String) {
        try {
            val response = apiService.refreshToken(mapOf("token" to oldToken))
            val newToken = response["token"] ?: return
            val expiresAt = response["expires_at"] ?: ""
            
            // Update local DB to match MySQL state
            // Mapping to refresh_tokens table
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncProfile() {
        try {
            val userDto = apiService.getCurrentUser()
            userDao.insertUser(UserEntity(
                id = userDto.id,
                companyId = userDto.companyId,
                fullName = userDto.fullName,
                email = userDto.email,
                passwordHash = "REDACTED",
                role = userDto.role,
                department = userDto.department,
                region = userDto.region,
                calibrationScore = userDto.calibrationScore,
                createdAt = userDto.createdAt,
                updatedAt = userDto.createdAt
            ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
