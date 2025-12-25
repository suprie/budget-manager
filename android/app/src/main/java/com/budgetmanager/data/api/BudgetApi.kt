package com.budgetmanager.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class BudgetDto(
    val id: Long,
    val name: String,
    val description: String,
    val pocket_id: Long,
    val allocated_amount: Double,
    val spent_amount: Double,
    val period: String,
    val created_at: String,
    val updated_at: String
)

data class BudgetSummaryDto(
    val period: String,
    val total_allocated: Double,
    val total_spent: Double,
    val total_remaining: Double,
    val unallocated_funds: Double
)

data class CreateBudgetRequest(
    val name: String,
    val description: String = "",
    val pocket_id: Long,
    val allocated_amount: Double,
    val period: String
)

data class UpdateBudgetRequest(
    val name: String? = null,
    val description: String? = null,
    val allocated_amount: Double? = null
)

interface BudgetApi {
    @POST("api/budgets")
    suspend fun createBudget(@Body request: CreateBudgetRequest): Response<BudgetDto>

    @GET("api/budgets")
    suspend fun getAllBudgets(): Response<List<BudgetDto>>

    @GET("api/budgets/{id}")
    suspend fun getBudget(@Path("id") id: Long): Response<BudgetDto>

    @PUT("api/budgets/{id}")
    suspend fun updateBudget(
        @Path("id") id: Long,
        @Body request: UpdateBudgetRequest
    ): Response<BudgetDto>

    @DELETE("api/budgets/{id}")
    suspend fun deleteBudget(@Path("id") id: Long): Response<Unit>

    @GET("api/budgets/by-period")
    suspend fun getBudgetsByPeriod(@Query("period") period: String): Response<List<BudgetDto>>

    @GET("api/budgets/summary")
    suspend fun getSummary(@Query("period") period: String): Response<BudgetSummaryDto>

    @GET("api/pockets/{pocketId}/budgets")
    suspend fun getBudgetsByPocket(@Path("pocketId") pocketId: Long): Response<List<BudgetDto>>

    @GET("api/budgets/{id}/remaining")
    suspend fun getRemainingBudget(@Path("id") id: Long): Response<Double>
}
