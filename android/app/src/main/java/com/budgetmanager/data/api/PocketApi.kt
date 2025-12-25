package com.budgetmanager.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class PocketDto(
    val id: Long,
    val name: String,
    val description: String,
    val balance: Double,
    val created_at: String,
    val updated_at: String
)

data class CreatePocketRequest(
    val name: String,
    val description: String = "",
    val balance: Double = 0.0
)

data class UpdatePocketRequest(
    val name: String? = null,
    val description: String? = null,
    val balance: Double? = null
)

data class AddFundsRequest(
    val amount: Double
)

interface PocketApi {
    @POST("api/pockets")
    suspend fun createPocket(@Body request: CreatePocketRequest): Response<PocketDto>

    @GET("api/pockets")
    suspend fun getAllPockets(): Response<List<PocketDto>>

    @GET("api/pockets/{id}")
    suspend fun getPocket(@Path("id") id: Long): Response<PocketDto>

    @PUT("api/pockets/{id}")
    suspend fun updatePocket(
        @Path("id") id: Long,
        @Body request: UpdatePocketRequest
    ): Response<PocketDto>

    @DELETE("api/pockets/{id}")
    suspend fun deletePocket(@Path("id") id: Long): Response<Unit>

    @POST("api/pockets/{id}/add-funds")
    suspend fun addFunds(
        @Path("id") id: Long,
        @Body request: AddFundsRequest
    ): Response<PocketDto>
}
