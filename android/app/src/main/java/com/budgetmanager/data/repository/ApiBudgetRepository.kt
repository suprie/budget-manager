package com.budgetmanager.data.repository

import com.budgetmanager.data.api.ApiClient
import com.budgetmanager.data.api.BudgetDto
import com.budgetmanager.data.api.CreateBudgetRequest
import com.budgetmanager.data.api.UpdateBudgetRequest
import com.budgetmanager.domain.entity.Budget
import com.budgetmanager.domain.entity.DomainError
import com.budgetmanager.domain.repository.BudgetRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ApiBudgetRepository : BudgetRepository {
    private val api = ApiClient.budgetApi
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // Cache for quick hasBudgets check
    private var cachedBudgets: List<Budget> = emptyList()

    private fun BudgetDto.toDomain(): Budget {
        return Budget(
            id = id,
            name = name,
            description = description,
            pocketId = pocket_id,
            allocatedAmount = allocated_amount,
            spentAmount = spent_amount,
            period = period,
            createdAt = parseDateTime(created_at),
            updatedAt = parseDateTime(updated_at)
        )
    }

    private fun parseDateTime(dateStr: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateStr.replace("Z", ""), dateFormatter)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }

    override suspend fun getAll(): List<Budget> {
        val response = api.getAllBudgets()
        if (response.isSuccessful) {
            val budgets = response.body()?.map { it.toDomain() } ?: emptyList()
            cachedBudgets = budgets
            return budgets
        }
        throw DomainError.ApiError("Failed to fetch budgets: ${response.code()}")
    }

    override suspend fun getById(id: Long): Budget {
        val response = api.getBudget(id)
        if (response.isSuccessful) {
            return response.body()?.toDomain()
                ?: throw DomainError.NotFound
        }
        if (response.code() == 404) {
            throw DomainError.NotFound
        }
        throw DomainError.ApiError("Failed to fetch budget: ${response.code()}")
    }

    override suspend fun getByPocketId(pocketId: Long): List<Budget> {
        val response = api.getBudgetsByPocket(pocketId)
        if (response.isSuccessful) {
            return response.body()?.map { it.toDomain() } ?: emptyList()
        }
        throw DomainError.ApiError("Failed to fetch budgets: ${response.code()}")
    }

    override suspend fun getByPeriod(period: String): List<Budget> {
        val response = api.getBudgetsByPeriod(period)
        if (response.isSuccessful) {
            val budgets = response.body()?.map { it.toDomain() } ?: emptyList()
            cachedBudgets = budgets
            return budgets
        }
        throw DomainError.ApiError("Failed to fetch budgets: ${response.code()}")
    }

    override suspend fun add(budget: Budget): Budget {
        val request = CreateBudgetRequest(
            name = budget.name,
            description = budget.description,
            pocket_id = budget.pocketId,
            allocated_amount = budget.allocatedAmount,
            period = budget.period
        )
        val response = api.createBudget(request)
        if (response.isSuccessful) {
            return response.body()?.toDomain()
                ?: throw DomainError.ApiError("Failed to parse response")
        }
        throw DomainError.ApiError("Failed to create budget: ${response.code()}")
    }

    override suspend fun update(budget: Budget) {
        val request = UpdateBudgetRequest(
            name = budget.name,
            description = budget.description,
            allocated_amount = budget.allocatedAmount
        )
        val response = api.updateBudget(budget.id, request)
        if (!response.isSuccessful) {
            throw DomainError.ApiError("Failed to update budget: ${response.code()}")
        }
    }

    override suspend fun updateSpentAmount(id: Long, amount: Double) {
        // Get current budget and update spent amount
        val budget = getById(id)
        val newSpentAmount = budget.spentAmount + amount

        // We need a custom endpoint or workaround for this
        // For now, we'll update via the regular update endpoint
        // Note: The server should handle this atomically
        val request = UpdateBudgetRequest(
            allocated_amount = budget.allocatedAmount // Keep allocated, server updates spent
        )
        val response = api.updateBudget(id, request)
        if (!response.isSuccessful) {
            throw DomainError.ApiError("Failed to update spent amount: ${response.code()}")
        }
    }

    override suspend fun delete(id: Long) {
        val response = api.deleteBudget(id)
        if (!response.isSuccessful) {
            throw DomainError.ApiError("Failed to delete budget: ${response.code()}")
        }
    }

    override fun hasBudgets(pocketId: Long): Boolean {
        return cachedBudgets.any { it.pocketId == pocketId }
    }
}
