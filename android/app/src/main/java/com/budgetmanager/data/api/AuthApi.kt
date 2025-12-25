package com.budgetmanager.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

data class AuthResponseDto(
    val token: String,
    val user: UserDto
)

data class UserDto(
    val id: Long,
    val email: String,
    val name: String,
    val created_at: String,
    val updated_at: String
)

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponseDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponseDto>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<UserDto>
}
