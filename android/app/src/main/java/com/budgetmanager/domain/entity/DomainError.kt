package com.budgetmanager.domain.entity

sealed class DomainError : Exception() {
    data object NotFound : DomainError()
    data class InvalidInput(override val message: String) : DomainError()
    data object InsufficientFunds : DomainError()
    data object PocketHasBudgets : DomainError()
    data object BudgetHasExpenses : DomainError()
    data object Unauthorized : DomainError()
    data class NetworkError(override val message: String) : DomainError()
    data class ServerError(override val message: String) : DomainError()
    data class ApiError(override val message: String) : DomainError()
}
