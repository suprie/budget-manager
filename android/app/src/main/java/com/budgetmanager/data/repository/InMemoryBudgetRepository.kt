package com.budgetmanager.data.repository

import com.budgetmanager.domain.entity.Budget
import com.budgetmanager.domain.entity.DomainError
import com.budgetmanager.domain.repository.BudgetRepository
import java.time.LocalDateTime

class InMemoryBudgetRepository : BudgetRepository {
    private val budgets = mutableListOf<Budget>()
    private var nextId: Long = 1

    override suspend fun getAll(): List<Budget> {
        return budgets.sortedByDescending { it.period }
    }

    override suspend fun getById(id: Long): Budget {
        return budgets.find { it.id == id } ?: throw DomainError.NotFound
    }

    override suspend fun getByPocketId(pocketId: Long): List<Budget> {
        return budgets
            .filter { it.pocketId == pocketId }
            .sortedByDescending { it.period }
    }

    override suspend fun getByPeriod(period: String): List<Budget> {
        return budgets
            .filter { it.period == period }
            .sortedBy { it.name }
    }

    override suspend fun add(budget: Budget): Budget {
        val newBudget = budget.copy(id = nextId++)
        budgets.add(newBudget)
        return newBudget
    }

    override suspend fun update(budget: Budget) {
        val index = budgets.indexOfFirst { it.id == budget.id }
        if (index == -1) throw DomainError.NotFound

        budgets[index] = budget.copy(updatedAt = LocalDateTime.now())
    }

    override suspend fun updateSpentAmount(id: Long, amount: Double) {
        val index = budgets.indexOfFirst { it.id == id }
        if (index == -1) throw DomainError.NotFound

        val budget = budgets[index]
        budgets[index] = budget.copy(
            spentAmount = budget.spentAmount + amount,
            updatedAt = LocalDateTime.now()
        )
    }

    override suspend fun delete(id: Long) {
        if (!budgets.removeIf { it.id == id }) {
            throw DomainError.NotFound
        }
    }

    override fun hasBudgets(pocketId: Long): Boolean {
        return budgets.any { it.pocketId == pocketId }
    }
}
