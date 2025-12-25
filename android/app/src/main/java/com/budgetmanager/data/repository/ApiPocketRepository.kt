package com.budgetmanager.data.repository

import com.budgetmanager.data.api.AddFundsRequest
import com.budgetmanager.data.api.ApiClient
import com.budgetmanager.data.api.CreatePocketRequest
import com.budgetmanager.data.api.PocketDto
import com.budgetmanager.data.api.UpdatePocketRequest
import com.budgetmanager.domain.entity.DomainError
import com.budgetmanager.domain.entity.Pocket
import com.budgetmanager.domain.repository.PocketRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ApiPocketRepository : PocketRepository {
    private val api = ApiClient.pocketApi
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private fun PocketDto.toDomain(): Pocket {
        return Pocket(
            id = id,
            name = name,
            description = description,
            balance = balance,
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

    override suspend fun getAll(): List<Pocket> {
        val response = api.getAllPockets()
        if (response.isSuccessful) {
            return response.body()?.map { it.toDomain() } ?: emptyList()
        }
        throw DomainError.ApiError("Failed to fetch pockets: ${response.code()}")
    }

    override suspend fun getById(id: Long): Pocket {
        val response = api.getPocket(id)
        if (response.isSuccessful) {
            return response.body()?.toDomain()
                ?: throw DomainError.NotFound
        }
        if (response.code() == 404) {
            throw DomainError.NotFound
        }
        throw DomainError.ApiError("Failed to fetch pocket: ${response.code()}")
    }

    override suspend fun add(pocket: Pocket): Pocket {
        val request = CreatePocketRequest(
            name = pocket.name,
            description = pocket.description,
            balance = pocket.balance
        )
        val response = api.createPocket(request)
        if (response.isSuccessful) {
            return response.body()?.toDomain()
                ?: throw DomainError.ApiError("Failed to parse response")
        }
        throw DomainError.ApiError("Failed to create pocket: ${response.code()}")
    }

    override suspend fun update(pocket: Pocket) {
        val request = UpdatePocketRequest(
            name = pocket.name,
            description = pocket.description,
            balance = pocket.balance
        )
        val response = api.updatePocket(pocket.id, request)
        if (!response.isSuccessful) {
            throw DomainError.ApiError("Failed to update pocket: ${response.code()}")
        }
    }

    override suspend fun delete(id: Long) {
        val response = api.deletePocket(id)
        if (!response.isSuccessful) {
            throw DomainError.ApiError("Failed to delete pocket: ${response.code()}")
        }
    }

    override suspend fun updateBalance(id: Long, amount: Double) {
        if (amount > 0) {
            val request = AddFundsRequest(amount = amount)
            val response = api.addFunds(id, request)
            if (!response.isSuccessful) {
                throw DomainError.ApiError("Failed to add funds: ${response.code()}")
            }
        } else {
            // For negative amounts (deductions), we need to get current balance and update
            val pocket = getById(id)
            val newBalance = pocket.balance + amount
            val request = UpdatePocketRequest(balance = newBalance)
            val response = api.updatePocket(id, request)
            if (!response.isSuccessful) {
                throw DomainError.ApiError("Failed to update balance: ${response.code()}")
            }
        }
    }
}
