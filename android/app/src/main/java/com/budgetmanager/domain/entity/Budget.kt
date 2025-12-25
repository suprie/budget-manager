package com.budgetmanager.domain.entity

import java.time.LocalDateTime

/**
 * Budget represents an envelope in the zero-sum budgeting system.
 * Money is allocated from a Pocket into Budget envelopes.
 */
data class Budget(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val pocketId: Long,
    val allocatedAmount: Double,
    val spentAmount: Double = 0.0,
    val period: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val remainingAmount: Double
        get() = allocatedAmount - spentAmount
}

data class BudgetSummary(
    val period: String,
    val totalAllocated: Double,
    val totalSpent: Double,
    val totalRemaining: Double,
    val unallocatedFunds: Double
)
