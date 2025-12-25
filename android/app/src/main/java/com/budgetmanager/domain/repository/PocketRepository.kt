package com.budgetmanager.domain.repository

import com.budgetmanager.domain.entity.Pocket

interface PocketRepository {
    suspend fun getAll(): List<Pocket>
    suspend fun getById(id: Long): Pocket
    suspend fun add(pocket: Pocket): Pocket
    suspend fun update(pocket: Pocket)
    suspend fun delete(id: Long)
    suspend fun updateBalance(id: Long, amount: Double)
}
