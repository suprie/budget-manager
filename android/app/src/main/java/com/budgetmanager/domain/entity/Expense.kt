package com.budgetmanager.domain.entity

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Expense represents a spending transaction against a budget envelope
 */
data class Expense(
    val id: Long = 0,
    val budgetId: Long,
    val amount: Double,
    val description: String,
    val date: LocalDate = LocalDate.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
