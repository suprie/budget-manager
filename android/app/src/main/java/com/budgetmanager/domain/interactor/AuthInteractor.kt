package com.budgetmanager.domain.interactor

import com.budgetmanager.domain.entity.AuthResponse
import com.budgetmanager.domain.entity.User
import com.budgetmanager.domain.repository.AuthRepository

class AuthInteractor(
    private val authRepository: AuthRepository
) {
    suspend fun register(email: String, password: String, name: String): Result<AuthResponse> {
        if (email.isBlank()) {
            return Result.failure(Exception("Email is required"))
        }
        if (!email.contains("@")) {
            return Result.failure(Exception("Invalid email format"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }
        if (name.isBlank()) {
            return Result.failure(Exception("Name is required"))
        }

        return authRepository.register(email.trim(), password, name.trim())
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        if (email.isBlank()) {
            return Result.failure(Exception("Email is required"))
        }
        if (password.isBlank()) {
            return Result.failure(Exception("Password is required"))
        }

        return authRepository.login(email.trim(), password)
    }

    suspend fun logout() {
        authRepository.clearToken()
        authRepository.clearCurrentUser()
    }

    suspend fun isLoggedIn(): Boolean {
        return authRepository.getStoredToken() != null
    }

    suspend fun getCurrentUser(): User? {
        return authRepository.getCurrentUser()
    }

    suspend fun getToken(): String? {
        return authRepository.getStoredToken()
    }
}
