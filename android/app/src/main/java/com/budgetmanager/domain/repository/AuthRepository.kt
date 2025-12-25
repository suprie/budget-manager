package com.budgetmanager.domain.repository

import com.budgetmanager.domain.entity.AuthResponse
import com.budgetmanager.domain.entity.User

interface AuthRepository {
    suspend fun register(email: String, password: String, name: String): Result<AuthResponse>
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun getStoredToken(): String?
    suspend fun saveToken(token: String)
    suspend fun clearToken()
    suspend fun getCurrentUser(): User?
    suspend fun saveCurrentUser(user: User)
    suspend fun clearCurrentUser()
}
