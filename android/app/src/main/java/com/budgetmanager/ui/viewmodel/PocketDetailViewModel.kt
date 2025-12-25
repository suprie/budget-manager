package com.budgetmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmanager.domain.entity.Budget
import com.budgetmanager.domain.entity.Pocket
import com.budgetmanager.domain.interactor.BudgetInteractor
import com.budgetmanager.domain.interactor.PocketInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PocketDetailState(
    val pocket: Pocket? = null,
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val totalAllocated: Double
        get() = budgets.sumOf { it.allocatedAmount }

    val totalSpent: Double
        get() = budgets.sumOf { it.spentAmount }
}

class PocketDetailViewModel(
    private val pocketId: Long,
    private val pocketInteractor: PocketInteractor,
    private val budgetInteractor: BudgetInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(PocketDetailState())
    val state: StateFlow<PocketDetailState> = _state.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val pocket = pocketInteractor.getPocket(pocketId)
                val budgets = budgetInteractor.getBudgetsByPocketId(pocketId)
                _state.value = _state.value.copy(
                    pocket = pocket,
                    budgets = budgets,
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

    fun addFunds(amount: Double) {
        viewModelScope.launch {
            try {
                pocketInteractor.addFunds(pocketId, amount)
                loadData()
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            try {
                budgetInteractor.deleteBudget(id)
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
