package com.budgetmanager.domain.interactor

import com.budgetmanager.domain.entity.DomainError
import com.budgetmanager.domain.entity.Expense
import com.budgetmanager.domain.repository.BudgetRepository
import com.budgetmanager.domain.repository.ExpenseRepository
import java.time.LocalDate

class ExpenseInteractor(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository
) {
    suspend fun getExpenses(): List<Expense> {
        return expenseRepository.getAll()
    }

    suspend fun getExpense(id: Long): Expense {
        return expenseRepository.getById(id)
    }

    suspend fun getExpensesByBudgetId(budgetId: Long): List<Expense> {
        return expenseRepository.getByBudgetId(budgetId)
    }

    suspend fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): List<Expense> {
        return expenseRepository.getByDateRange(startDate, endDate)
    }

    suspend fun createExpense(
        budgetId: Long,
        amount: Double,
        description: String,
        date: LocalDate = LocalDate.now()
    ): Expense {
        if (amount <= 0) {
            throw DomainError.InvalidInput("Amount must be greater than 0")
        }
        if (description.isBlank()) {
            throw DomainError.InvalidInput("Description cannot be empty")
        }

        val budget = budgetRepository.getById(budgetId)
        // Skip insufficient funds check for Uncategorized budget (allocatedAmount = 0)
        // This allows expenses to be added without pre-allocation
        if (budget.allocatedAmount > 0 && budget.remainingAmount < amount) {
            throw DomainError.InsufficientFunds
        }

        val expense = Expense(
            budgetId = budgetId,
            amount = amount,
            description = description,
            date = date
        )

        val createdExpense = expenseRepository.add(expense)
        budgetRepository.updateSpentAmount(budgetId, amount)

        return createdExpense
    }

    suspend fun updateExpense(
        id: Long,
        budgetId: Long? = null,
        amount: Double? = null,
        description: String? = null,
        date: LocalDate? = null
    ): Expense {
        val expense = expenseRepository.getById(id)
        val oldAmount = expense.amount
        val oldBudgetId = expense.budgetId

        val updatedExpense = expense.copy(
            budgetId = budgetId ?: expense.budgetId,
            amount = amount ?: expense.amount,
            description = description ?: expense.description,
            date = date ?: expense.date
        )

        if (updatedExpense.budgetId != oldBudgetId) {
            val newBudget = budgetRepository.getById(updatedExpense.budgetId)
            if (newBudget.remainingAmount < updatedExpense.amount) {
                throw DomainError.InsufficientFunds
            }

            budgetRepository.updateSpentAmount(oldBudgetId, -oldAmount)
            budgetRepository.updateSpentAmount(updatedExpense.budgetId, updatedExpense.amount)
        } else if (updatedExpense.amount != oldAmount) {
            val diff = updatedExpense.amount - oldAmount
            val budget = budgetRepository.getById(updatedExpense.budgetId)

            if (diff > 0 && budget.remainingAmount < diff) {
                throw DomainError.InsufficientFunds
            }

            budgetRepository.updateSpentAmount(updatedExpense.budgetId, diff)
        }

        expenseRepository.update(updatedExpense)
        return updatedExpense
    }

    suspend fun deleteExpense(id: Long) {
        val expense = expenseRepository.getById(id)
        budgetRepository.updateSpentAmount(expense.budgetId, -expense.amount)
        expenseRepository.delete(id)
    }
}
