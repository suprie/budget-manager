package com.budgetmanager.domain.interactor

import com.budgetmanager.domain.entity.DomainError
import com.budgetmanager.domain.entity.Pocket
import com.budgetmanager.domain.repository.BudgetRepository
import com.budgetmanager.domain.repository.PocketRepository

class PocketInteractor(
    private val pocketRepository: PocketRepository,
    private val budgetRepository: BudgetRepository
) {
    suspend fun getPockets(): List<Pocket> {
        return pocketRepository.getAll()
    }

    suspend fun getPocket(id: Long): Pocket {
        return pocketRepository.getById(id)
    }

    suspend fun createPocket(
        name: String,
        description: String = "",
        balance: Double = 0.0
    ): Pocket {
        if (name.isBlank()) {
            throw DomainError.InvalidInput("Name cannot be empty")
        }

        val pocket = Pocket(
            name = name,
            description = description,
            balance = balance
        )

        return pocketRepository.add(pocket)
    }

    suspend fun updatePocket(
        id: Long,
        name: String? = null,
        description: String? = null,
        balance: Double? = null
    ): Pocket {
        val pocket = pocketRepository.getById(id)

        val updatedPocket = pocket.copy(
            name = name ?: pocket.name,
            description = description ?: pocket.description,
            balance = balance ?: pocket.balance
        )

        pocketRepository.update(updatedPocket)
        return updatedPocket
    }

    suspend fun deletePocket(id: Long) {
        if (budgetRepository.hasBudgets(id)) {
            throw DomainError.PocketHasBudgets
        }
        pocketRepository.delete(id)
    }

    suspend fun addFunds(id: Long, amount: Double): Pocket {
        if (amount <= 0) {
            throw DomainError.InvalidInput("Amount must be greater than 0")
        }
        pocketRepository.updateBalance(id, amount)
        return pocketRepository.getById(id)
    }
}
