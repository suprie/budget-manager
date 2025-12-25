package com.budgetmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmanager.domain.entity.Pocket
import com.budgetmanager.domain.interactor.BudgetInteractor
import com.budgetmanager.domain.interactor.PocketInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddBudgetState(
    val name: String = "",
    val description: String = "",
    val allocatedAmount: String = "",
    val pockets: List<Pocket> = emptyList(),
    val selectedPocket: Pocket? = null,
    val period: String = BudgetInteractor.currentPeriod(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

class AddBudgetViewModel(
    private val budgetInteractor: BudgetInteractor,
    private val pocketInteractor: PocketInteractor,
    preselectedPocketId: Long? = null
) : ViewModel() {

    private val _state = MutableStateFlow(AddBudgetState())
    val state: StateFlow<AddBudgetState> = _state.asStateFlow()

    private val preselectedId = preselectedPocketId

    fun loadPockets() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val pockets = pocketInteractor.getPockets()
                val selected = if (preselectedId != null) {
                    pockets.find { it.id == preselectedId }
                } else {
                    pockets.firstOrNull()
                }
                _state.value = _state.value.copy(
                    pockets = pockets,
                    selectedPocket = selected,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun updateName(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun updateDescription(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun updateAllocatedAmount(amount: String) {
        _state.value = _state.value.copy(allocatedAmount = amount)
    }

    fun selectPocket(pocket: Pocket) {
        _state.value = _state.value.copy(selectedPocket = pocket)
    }

    fun saveBudget() {
        val currentState = _state.value

        if (currentState.name.isBlank()) {
            _state.value = currentState.copy(errorMessage = "Name cannot be empty")
            return
        }

        val amount = currentState.allocatedAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.value = currentState.copy(errorMessage = "Amount must be a valid number greater than 0")
            return
        }

        val pocket = currentState.selectedPocket
        if (pocket == null) {
            _state.value = currentState.copy(errorMessage = "Please select a pocket")
            return
        }

        viewModelScope.launch {
            try {
                budgetInteractor.createBudget(
                    name = currentState.name,
                    description = currentState.description,
                    pocketId = pocket.id,
                    allocatedAmount = amount,
                    period = currentState.period
                )
                _state.value = currentState.copy(isSaved = true)
            } catch (e: Exception) {
                _state.value = currentState.copy(errorMessage = e.message ?: "Failed to save budget")
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
