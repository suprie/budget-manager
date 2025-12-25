package com.budgetmanager.di

import android.content.Context
import com.budgetmanager.data.repository.ApiAuthRepository
import com.budgetmanager.data.repository.ApiBudgetRepository
import com.budgetmanager.data.repository.ApiExpenseRepository
import com.budgetmanager.data.repository.ApiPocketRepository
import com.budgetmanager.data.repository.InMemoryAuthRepository
import com.budgetmanager.data.repository.InMemoryBudgetRepository
import com.budgetmanager.data.repository.InMemoryExpenseRepository
import com.budgetmanager.data.repository.InMemoryPocketRepository
import com.budgetmanager.domain.interactor.AuthInteractor
import com.budgetmanager.domain.interactor.BudgetInteractor
import com.budgetmanager.domain.interactor.ExpenseInteractor
import com.budgetmanager.domain.interactor.PocketInteractor
import com.budgetmanager.domain.repository.AuthRepository
import com.budgetmanager.domain.repository.BudgetRepository
import com.budgetmanager.domain.repository.ExpenseRepository
import com.budgetmanager.domain.repository.PocketRepository

/**
 * Simple manual dependency injection container.
 * In a larger app, consider using Hilt or Koin.
 */
object AppContainer {
    // Set to true to use API backend, false for in-memory storage
    // When true, all data is persisted via the Go backend API
    const val USE_API_BACKEND = true

    private var initialized = false
    private lateinit var authRepository: AuthRepository

    // Pocket repository
    private val pocketRepository: PocketRepository = if (USE_API_BACKEND) {
        ApiPocketRepository()
    } else {
        InMemoryPocketRepository()
    }

    // Budget repository
    private val budgetRepository: BudgetRepository = if (USE_API_BACKEND) {
        ApiBudgetRepository()
    } else {
        InMemoryBudgetRepository()
    }

    // Expense repository
    private val expenseRepository: ExpenseRepository = if (USE_API_BACKEND) {
        ApiExpenseRepository()
    } else {
        InMemoryExpenseRepository()
    }

    // Initialize with context (call from MainActivity)
    fun init(context: Context) {
        if (!initialized) {
            authRepository = if (USE_API_BACKEND) {
                ApiAuthRepository(context.applicationContext)
            } else {
                InMemoryAuthRepository()
            }
            initialized = true
        }
    }

    // Interactors
    val authInteractor: AuthInteractor
        get() {
            check(initialized) { "AppContainer not initialized. Call init(context) first." }
            return AuthInteractor(authRepository)
        }

    val pocketInteractor = PocketInteractor(pocketRepository, budgetRepository)
    val budgetInteractor = BudgetInteractor(budgetRepository, pocketRepository, expenseRepository)
    val expenseInteractor = ExpenseInteractor(expenseRepository, budgetRepository)
}
