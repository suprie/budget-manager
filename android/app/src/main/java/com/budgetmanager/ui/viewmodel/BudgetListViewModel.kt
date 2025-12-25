package com.budgetmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmanager.domain.entity.Budget
import com.budgetmanager.domain.entity.BudgetSummary
import com.budgetmanager.domain.interactor.BudgetInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BudgetListState(
    val budgets: List<Budget> = emptyList(),
    val summary: BudgetSummary? = null,
    val currentPeriod: String = BudgetInteractor.currentPeriod(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class BudgetListViewModel(
    private val budgetInteractor: BudgetInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetListState())
    val state: StateFlow<BudgetListState> = _state.asStateFlow()

    fun loadBudgets() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val period = _state.value.currentPeriod
                val budgets = budgetInteractor.getBudgetsByPeriod(period)
                val summary = budgetInteractor.getSummary(period)
                _state.value = _state.value.copy(
                    budgets = budgets,
                    summary = summary,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load budgets"
                )
            }
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            try {
                budgetInteractor.deleteBudget(id)
                loadBudgets()
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
