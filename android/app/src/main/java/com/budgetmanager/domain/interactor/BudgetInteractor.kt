package com.budgetmanager.domain.interactor

import com.budgetmanager.domain.entity.Budget
import com.budgetmanager.domain.entity.BudgetSummary
import com.budgetmanager.domain.entity.DomainError
import com.budgetmanager.domain.repository.BudgetRepository
import com.budgetmanager.domain.repository.ExpenseRepository
import com.budgetmanager.domain.repository.PocketRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BudgetInteractor(
    private val budgetRepository: BudgetRepository,
    private val pocketRepository: PocketRepository,
    private val expenseRepository: ExpenseRepository
) {
    suspend fun getBudgets(): List<Budget> {
        return budgetRepository.getAll()
    }

    suspend fun getBudget(id: Long): Budget {
        return budgetRepository.getById(id)
    }

    suspend fun getBudgetsByPocketId(pocketId: Long): List<Budget> {
        return budgetRepository.getByPocketId(pocketId)
    }

    suspend fun getBudgetsByPeriod(period: String): List<Budget> {
        return budgetRepository.getByPeriod(period)
    }

    suspend fun createBudget(
        name: String,
        description: String = "",
        pocketId: Long,
        allocatedAmount: Double,
        period: String
    ): Budget {
        if (name.isBlank()) {
            throw DomainError.InvalidInput("Name cannot be empty")
        }
        if (period.isBlank()) {
            throw DomainError.InvalidInput("Period cannot be empty")
        }
        if (allocatedAmount < 0) {
            throw DomainError.InvalidInput("Allocated amount must be non-negative")
        }

        val pocket = pocketRepository.getById(pocketId)
        if (pocket.balance < allocatedAmount) {
            throw DomainError.InsufficientFunds
        }

        val budget = Budget(
            name = name,
            description = description,
            pocketId = pocketId,
            allocatedAmount = allocatedAmount,
            period = period
        )

        val createdBudget = budgetRepository.add(budget)
        pocketRepository.updateBalance(pocketId, -allocatedAmount)

        return createdBudget
    }

    suspend fun updateBudget(
        id: Long,
        name: String? = null,
        description: String? = null,
        allocatedAmount: Double? = null
    ): Budget {
        val budget = budgetRepository.getById(id)

        var updatedBudget = budget.copy(
            name = name ?: budget.name,
            description = description ?: budget.description
        )

        if (allocatedAmount != null) {
            val diff = allocatedAmount - budget.allocatedAmount

            if (diff > 0) {
                val pocket = pocketRepository.getById(budget.pocketId)
                if (pocket.balance < diff) {
                    throw DomainError.InsufficientFunds
                }
            }

            updatedBudget = updatedBudget.copy(allocatedAmount = allocatedAmount)
            pocketRepository.updateBalance(budget.pocketId, -diff)
        }

        budgetRepository.update(updatedBudget)
        return updatedBudget
    }

    suspend fun deleteBudget(id: Long) {
        if (expenseRepository.hasExpenses(id)) {
            throw DomainError.BudgetHasExpenses
        }

        val budget = budgetRepository.getById(id)
        val unspentAmount = budget.allocatedAmount - budget.spentAmount

        if (unspentAmount > 0) {
            pocketRepository.updateBalance(budget.pocketId, unspentAmount)
        }

        budgetRepository.delete(id)
    }

    suspend fun getRemainingBudget(id: Long): Double {
        val budget = budgetRepository.getById(id)
        return budget.remainingAmount
    }

    suspend fun getSummary(period: String): BudgetSummary {
        val budgets = budgetRepository.getByPeriod(period)
        val pockets = pocketRepository.getAll()

        val totalAllocated = budgets.sumOf { it.allocatedAmount }
        val totalSpent = budgets.sumOf { it.spentAmount }
        val unallocatedFunds = pockets.sumOf { it.balance }

        return BudgetSummary(
            period = period,
            totalAllocated = totalAllocated,
            totalSpent = totalSpent,
            totalRemaining = totalAllocated - totalSpent,
            unallocatedFunds = unallocatedFunds
        )
    }

    companion object {
        fun currentPeriod(): String {
            return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        }
    }
}
