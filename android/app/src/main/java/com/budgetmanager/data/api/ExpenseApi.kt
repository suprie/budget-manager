package com.budgetmanager.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class ExpenseDto(
    val id: Long,
    val budget_id: Long,
    val amount: Double,
    val description: String,
    val date: String,
    val created_at: String,
    val updated_at: String
)

data class CreateExpenseRequest(
    val budget_id: Long,
    val amount: Double,
    val description: String,
    val date: String
)

data class UpdateExpenseRequest(
    val budget_id: Long? = null,
    val amount: Double? = null,
    val description: String? = null,
    val date: String? = null
)

data class ApiErrorResponse(
    val error: String,
    val message: String?
)

interface ExpenseApi {
    @POST("api/expenses")
    suspend fun createExpense(@Body request: CreateExpenseRequest): Response<ExpenseDto>

    @GET("api/expenses")
    suspend fun getAllExpenses(): Response<List<ExpenseDto>>

    @GET("api/expenses/{id}")
    suspend fun getExpense(@Path("id") id: Long): Response<ExpenseDto>

    @PUT("api/expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: Long,
        @Body request: UpdateExpenseRequest
    ): Response<ExpenseDto>

    @DELETE("api/expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: Long): Response<Unit>

    @GET("api/budgets/{budgetId}/expenses")
    suspend fun getExpensesByBudget(@Path("budgetId") budgetId: Long): Response<List<ExpenseDto>>

    @GET("api/expenses/by-date-range")
    suspend fun getExpensesByDateRange(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Response<List<ExpenseDto>>
}
