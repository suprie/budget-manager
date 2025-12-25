package com.budgetmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmanager.domain.entity.Budget
import com.budgetmanager.domain.interactor.BudgetInteractor
import com.budgetmanager.domain.interactor.ExpenseInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddExpenseState(
    val description: String = "",
    val amount: String = "",
    val date: LocalDate = LocalDate.now(),
    val budget: Budget? = null,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

class AddExpenseViewModel(
    private val budgetId: Long,
    private val expenseInteractor: ExpenseInteractor,
    private val budgetInteractor: BudgetInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(AddExpenseState())
    val state: StateFlow<AddExpenseState> = _state.asStateFlow()

    fun loadBudget() {
        viewModelScope.launch {
            try {
                val budget = budgetInteractor.getBudget(budgetId)
                _state.value = _state.value.copy(budget = budget)
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateDescription(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun updateAmount(amount: String) {
        _state.value = _state.value.copy(amount = amount)
    }

    fun updateDate(date: LocalDate) {
        _state.value = _state.value.copy(date = date)
    }

    fun saveExpense() {
        val currentState = _state.value

        if (currentState.description.isBlank()) {
            _state.value = currentState.copy(errorMessage = "Description cannot be empty")
            return
        }

        val amount = currentState.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.value = currentState.copy(errorMessage = "Amount must be a valid number greater than 0")
            return
        }

        viewModelScope.launch {
            try {
                expenseInteractor.createExpense(
                    budgetId = budgetId,
                    amount = amount,
                    description = currentState.description,
                    date = currentState.date
                )
                _state.value = currentState.copy(isSaved = true)
            } catch (e: Exception) {
                _state.value = currentState.copy(errorMessage = e.message ?: "Failed to save expense")
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
