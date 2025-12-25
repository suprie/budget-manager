package com.budgetmanager.di

import com.budgetmanager.data.repository.ApiExpenseRepository
import com.budgetmanager.data.repository.InMemoryAuthRepository
import com.budgetmanager.data.repository.InMemoryBudgetRepository
import com.budgetmanager.data.repository.InMemoryExpenseRepository
import com.budgetmanager.data.repository.InMemoryPocketRepository
import com.budgetmanager.domain.interactor.AuthInteractor
import com.budgetmanager.domain.interactor.BudgetInteractor
import com.budgetmanager.domain.interactor.ExpenseInteractor
import com.budgetmanager.domain.interactor.PocketInteractor
import com.budgetmanager.domain.repository.ExpenseRepository

/**
 * Simple manual dependency injection container.
 * In a larger app, consider using Hilt or Koin.
 */
object AppContainer {
    // Set to true to use API backend, false for in-memory storage
    private const val USE_API_BACKEND = false

    // Repositories (singletons)
    private val authRepository = InMemoryAuthRepository()
    private val pocketRepository = InMemoryPocketRepository()
    private val budgetRepository = InMemoryBudgetRepository()
    private val inMemoryExpenseRepository = InMemoryExpenseRepository()
    private val apiExpenseRepository: ExpenseRepository? = if (USE_API_BACKEND) ApiExpenseRepository() else null

    // Use API repository for expenses when backend is enabled
    private val expenseRepository: ExpenseRepository = if (USE_API_BACKEND) {
        apiExpenseRepository ?: inMemoryExpenseRepository
    } else {
        inMemoryExpenseRepository
    }

    // Interactors
    val authInteractor = AuthInteractor(authRepository)
    val pocketInteractor = PocketInteractor(pocketRepository, budgetRepository)
    val budgetInteractor = BudgetInteractor(budgetRepository, pocketRepository, expenseRepository)
    val expenseInteractor = ExpenseInteractor(expenseRepository, budgetRepository)
}
