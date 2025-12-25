package com.budgetmanager.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.budgetmanager.data.api.ApiClient
import com.budgetmanager.data.api.LoginRequest
import com.budgetmanager.data.api.RegisterRequest
import com.budgetmanager.data.api.UserDto
import com.budgetmanager.domain.entity.AuthResponse
import com.budgetmanager.domain.entity.User
import com.budgetmanager.domain.repository.AuthRepository
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ApiAuthRepository(context: Context) : AuthRepository {
    private val api = ApiClient.authApi
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER = "current_user"
    }

    private fun UserDto.toDomain(): User {
        return User(
            id = id,
            email = email,
            name = name,
            createdAt = parseDateTime(created_at),
            updatedAt = parseDateTime(updated_at)
        )
    }

    private fun parseDateTime(dateStr: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateStr.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<AuthResponse> {
        return try {
            val request = RegisterRequest(email = email, password = password, name = name)
            val response = api.register(request)

            if (response.isSuccessful) {
                val body = response.body()!!
                val user = body.user.toDomain()
                val authResponse = AuthResponse(token = body.token, user = user)

                // Save token and set it for future API calls
                saveToken(body.token)
                saveCurrentUser(user)
                ApiClient.setAuthToken(body.token)

                Result.success(authResponse)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Registration failed"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val request = LoginRequest(email = email, password = password)
            val response = api.login(request)

            if (response.isSuccessful) {
                val body = response.body()!!
                val user = body.user.toDomain()
                val authResponse = AuthResponse(token = body.token, user = user)

                // Save token and set it for future API calls
                saveToken(body.token)
                saveCurrentUser(user)
                ApiClient.setAuthToken(body.token)

                Result.success(authResponse)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Login failed"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStoredToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    override suspend fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
        ApiClient.setAuthToken(token)
    }

    override suspend fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
        ApiClient.setAuthToken(null)
    }

    override suspend fun getCurrentUser(): User? {
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveCurrentUser(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit().putString(KEY_USER, userJson).apply()
    }

    override suspend fun clearCurrentUser() {
        prefs.edit().remove(KEY_USER).apply()
    }
}
