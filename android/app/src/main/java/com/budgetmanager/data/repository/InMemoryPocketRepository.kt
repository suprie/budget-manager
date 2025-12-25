package com.budgetmanager.data.repository

import com.budgetmanager.domain.entity.DomainError
import com.budgetmanager.domain.entity.Pocket
import com.budgetmanager.domain.repository.PocketRepository
import java.time.LocalDateTime

class InMemoryPocketRepository : PocketRepository {
    private val pockets = mutableListOf<Pocket>()
    private var nextId: Long = 1

    override suspend fun getAll(): List<Pocket> {
        return pockets.sortedBy { it.name }
    }

    override suspend fun getById(id: Long): Pocket {
        return pockets.find { it.id == id } ?: throw DomainError.NotFound
    }

    override suspend fun add(pocket: Pocket): Pocket {
        val newPocket = pocket.copy(id = nextId++)
        pockets.add(newPocket)
        return newPocket
    }

    override suspend fun update(pocket: Pocket) {
        val index = pockets.indexOfFirst { it.id == pocket.id }
        if (index == -1) throw DomainError.NotFound

        pockets[index] = pocket.copy(updatedAt = LocalDateTime.now())
    }

    override suspend fun delete(id: Long) {
        if (!pockets.removeIf { it.id == id }) {
            throw DomainError.NotFound
        }
    }

    override suspend fun updateBalance(id: Long, amount: Double) {
        val index = pockets.indexOfFirst { it.id == id }
        if (index == -1) throw DomainError.NotFound

        val pocket = pockets[index]
        pockets[index] = pocket.copy(
            balance = pocket.balance + amount,
            updatedAt = LocalDateTime.now()
        )
    }
}
