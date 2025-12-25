package com.budgetmanager.domain.repository

import com.budgetmanager.domain.entity.Budget

interface BudgetRepository {
    suspend fun getAll(): List<Budget>
    suspend fun getById(id: Long): Budget
    suspend fun getByPocketId(pocketId: Long): List<Budget>
    suspend fun getByPeriod(period: String): List<Budget>
    suspend fun add(budget: Budget): Budget
    suspend fun update(budget: Budget)
    suspend fun updateSpentAmount(id: Long, amount: Double)
    suspend fun delete(id: Long)
    fun hasBudgets(pocketId: Long): Boolean
}
