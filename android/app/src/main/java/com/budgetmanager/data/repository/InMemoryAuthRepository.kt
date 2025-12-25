package com.budgetmanager.data.repository

import com.budgetmanager.domain.entity.AuthResponse
import com.budgetmanager.domain.entity.User
import com.budgetmanager.domain.repository.AuthRepository
import java.time.LocalDateTime

class InMemoryAuthRepository : AuthRepository {
    private val users = mutableMapOf<String, Pair<User, String>>() // email -> (user, passwordHash)
    private var storedToken: String? = null
    private var currentUser: User? = null
    private var nextId: Long = 1

    override suspend fun register(email: String, password: String, name: String): Result<AuthResponse> {
        if (users.containsKey(email)) {
            return Result.failure(Exception("Email already exists"))
        }

        val user = User(
            id = nextId++,
            email = email,
            name = name,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        users[email] = Pair(user, password.hashCode().toString())

        val token = "mock_token_${user.id}_${System.currentTimeMillis()}"
        storedToken = token
        currentUser = user

        return Result.success(AuthResponse(token = token, user = user))
    }

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        val userPair = users[email]
            ?: return Result.failure(Exception("Invalid email or password"))

        val (user, storedPasswordHash) = userPair
        if (storedPasswordHash != password.hashCode().toString()) {
            return Result.failure(Exception("Invalid email or password"))
        }

        val token = "mock_token_${user.id}_${System.currentTimeMillis()}"
        storedToken = token
        currentUser = user

        return Result.success(AuthResponse(token = token, user = user))
    }

    override suspend fun getStoredToken(): String? = storedToken

    override suspend fun saveToken(token: String) {
        storedToken = token
    }

    override suspend fun clearToken() {
        storedToken = null
    }

    override suspend fun getCurrentUser(): User? = currentUser

    override suspend fun saveCurrentUser(user: User) {
        currentUser = user
    }

    override suspend fun clearCurrentUser() {
        currentUser = null
    }
}
