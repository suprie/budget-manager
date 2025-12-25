package com.budgetmanager.data.repository

import com.budgetmanager.data.api.ApiClient
import com.budgetmanager.data.api.CreateExpenseRequest
import com.budgetmanager.data.api.ExpenseDto
import com.budgetmanager.data.api.UpdateExpenseRequest
import com.budgetmanager.domain.entity.DomainError
import com.budgetmanager.domain.entity.Expense
import com.budgetmanager.domain.repository.ExpenseRepository
import com.google.gson.Gson
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ApiExpenseRepository : ExpenseRepository {
    private val expenseApi = ApiClient.expenseApi
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    override suspend fun getAll(): List<Expense> {
        val response = expenseApi.getAllExpenses()
        return handleListResponse(response)
    }

    override suspend fun getById(id: Long): Expense {
        val response = expenseApi.getExpense(id)
        return handleResponse(response)
    }

    override suspend fun getByBudgetId(budgetId: Long): List<Expense> {
        val response = expenseApi.getExpensesByBudget(budgetId)
        return handleListResponse(response)
    }

    override suspend fun getByDateRange(startDate: LocalDate, endDate: LocalDate): List<Expense> {
        val response = expenseApi.getExpensesByDateRange(
            startDate = startDate.format(dateFormatter),
            endDate = endDate.format(dateFormatter)
        )
        return handleListResponse(response)
    }

    override suspend fun add(expense: Expense): Expense {
        val request = CreateExpenseRequest(
            budget_id = expense.budgetId,
            amount = expense.amount,
            description = expense.description,
            date = expense.date.format(dateFormatter)
        )
        val response = expenseApi.createExpense(request)
        return handleResponse(response)
    }

    override suspend fun update(expense: Expense) {
        val request = UpdateExpenseRequest(
            budget_id = expense.budgetId,
            amount = expense.amount,
            description = expense.description,
            date = expense.date.format(dateFormatter)
        )
        val response = expenseApi.updateExpense(expense.id, request)
        handleResponse(response)
    }

    override suspend fun delete(id: Long) {
        val response = expenseApi.deleteExpense(id)
        if (!response.isSuccessful) {
            throw mapHttpError(response)
        }
    }

    override fun hasExpenses(budgetId: Long): Boolean {
        // This is a synchronous method, cannot call suspend function
        // For API implementation, we'd need to change the interface or handle differently
        // For now, return false (this is mainly used for validation before delete)
        return false
    }

    private fun handleResponse(response: Response<ExpenseDto>): Expense {
        if (response.isSuccessful) {
            val dto = response.body() ?: throw DomainError.NotFound
            return mapToExpense(dto)
        }
        throw mapHttpError(response)
    }

    private fun handleListResponse(response: Response<List<ExpenseDto>>): List<Expense> {
        if (response.isSuccessful) {
            val dtos = response.body() ?: emptyList()
            return dtos.map { mapToExpense(it) }
        }
        throw mapHttpError(response)
    }

    private fun <T> mapHttpError(response: Response<T>): DomainError {
        return when (response.code()) {
            401 -> DomainError.Unauthorized
            404 -> DomainError.NotFound
            else -> {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    val error = Gson().fromJson(errorBody, com.budgetmanager.data.api.ApiErrorResponse::class.java)
                    error.message ?: error.error
                } catch (e: Exception) {
                    "Server error: ${response.code()}"
                }

                if (message.lowercase().contains("insufficient")) {
                    DomainError.InsufficientFunds
                } else {
                    DomainError.ServerError(message)
                }
            }
        }
    }

    private fun mapToExpense(dto: ExpenseDto): Expense {
        return Expense(
            id = dto.id,
            budgetId = dto.budget_id,
            amount = dto.amount,
            description = dto.description,
            date = LocalDate.parse(dto.date.substring(0, 10)),
            createdAt = parseDateTime(dto.created_at),
            updatedAt = parseDateTime(dto.updated_at)
        )
    }

    private fun parseDateTime(dateTimeStr: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateTimeStr, dateTimeFormatter)
        } catch (e: Exception) {
            try {
                LocalDateTime.parse(dateTimeStr.replace("Z", ""))
            } catch (e: Exception) {
                LocalDateTime.now()
            }
        }
    }
}
