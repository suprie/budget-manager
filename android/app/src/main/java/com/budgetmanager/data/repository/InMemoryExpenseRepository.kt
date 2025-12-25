package com.budgetmanager.data.repository

import com.budgetmanager.domain.entity.DomainError
import com.budgetmanager.domain.entity.Expense
import com.budgetmanager.domain.repository.ExpenseRepository
import java.time.LocalDate
import java.time.LocalDateTime

class InMemoryExpenseRepository : ExpenseRepository {
    private val expenses = mutableListOf<Expense>()
    private var nextId: Long = 1

    override suspend fun getAll(): List<Expense> {
        return expenses.sortedByDescending { it.date }
    }

    override suspend fun getById(id: Long): Expense {
        return expenses.find { it.id == id } ?: throw DomainError.NotFound
    }

    override suspend fun getByBudgetId(budgetId: Long): List<Expense> {
        return expenses
            .filter { it.budgetId == budgetId }
            .sortedByDescending { it.date }
    }

    override suspend fun getByDateRange(startDate: LocalDate, endDate: LocalDate): List<Expense> {
        return expenses
            .filter { it.date in startDate..endDate }
            .sortedByDescending { it.date }
    }

    override suspend fun add(expense: Expense): Expense {
        val newExpense = expense.copy(id = nextId++)
        expenses.add(newExpense)
        return newExpense
    }

    override suspend fun update(expense: Expense) {
        val index = expenses.indexOfFirst { it.id == expense.id }
        if (index == -1) throw DomainError.NotFound

        expenses[index] = expense.copy(updatedAt = LocalDateTime.now())
    }

    override suspend fun delete(id: Long) {
        if (!expenses.removeIf { it.id == id }) {
            throw DomainError.NotFound
        }
    }

    override fun hasExpenses(budgetId: Long): Boolean {
        return expenses.any { it.budgetId == budgetId }
    }
}
