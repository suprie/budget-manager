package com.budgetmanager.domain.repository

import com.budgetmanager.domain.entity.Expense
import java.time.LocalDate

interface ExpenseRepository {
    suspend fun getAll(): List<Expense>
    suspend fun getById(id: Long): Expense
    suspend fun getByBudgetId(budgetId: Long): List<Expense>
    suspend fun getByDateRange(startDate: LocalDate, endDate: LocalDate): List<Expense>
    suspend fun add(expense: Expense): Expense
    suspend fun update(expense: Expense)
    suspend fun delete(id: Long)
    fun hasExpenses(budgetId: Long): Boolean
}
