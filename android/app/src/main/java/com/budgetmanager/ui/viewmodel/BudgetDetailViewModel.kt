package com.budgetmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmanager.domain.entity.Budget
import com.budgetmanager.domain.entity.Expense
import com.budgetmanager.domain.interactor.BudgetInteractor
import com.budgetmanager.domain.interactor.ExpenseInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BudgetDetailState(
    val budget: Budget? = null,
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val remainingBudget: Double
        get() = budget?.remainingAmount ?: 0.0

    val spentPercentage: Float
        get() {
            val b = budget ?: return 0f
            if (b.allocatedAmount <= 0) return 0f
            return (b.spentAmount / b.allocatedAmount).toFloat().coerceIn(0f, 1f)
        }
}

class BudgetDetailViewModel(
    private val budgetId: Long,
    private val budgetInteractor: BudgetInteractor,
    private val expenseInteractor: ExpenseInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetDetailState())
    val state: StateFlow<BudgetDetailState> = _state.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val budget = budgetInteractor.getBudget(budgetId)
                val expenses = expenseInteractor.getExpensesByBudgetId(budgetId)
                _state.value = _state.value.copy(
                    budget = budget,
                    expenses = expenses,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load data"
                )
            }
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            try {
                expenseInteractor.deleteExpense(id)
                loadData()
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
