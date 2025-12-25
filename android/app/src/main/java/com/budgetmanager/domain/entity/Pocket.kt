package com.budgetmanager.domain.entity

import java.time.LocalDateTime

/**
 * Pocket represents a source of money (e.g., bank account, cash, e-wallet)
 */
data class Pocket(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val balance: Double = 0.0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
