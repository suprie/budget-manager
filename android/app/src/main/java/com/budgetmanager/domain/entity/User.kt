package com.budgetmanager.domain.entity

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: Long = 0,
    val email: String,
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class AuthResponse(
    val token: String,
    val user: User
)
