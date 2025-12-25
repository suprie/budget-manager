package com.budgetmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmanager.domain.entity.Budget
import com.budgetmanager.domain.entity.Pocket
import com.budgetmanager.domain.interactor.BudgetInteractor
import com.budgetmanager.domain.interactor.ExpenseInteractor
import com.budgetmanager.domain.interactor.PocketInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class QuickAddExpenseState(
    val description: String = "",
    val amount: String = "",
    val date: LocalDate = LocalDate.now(),
    val pockets: List<Pocket> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val selectedPocket: Pocket? = null,
    val selectedBudget: Budget? = null,
    val currentPeriod: String = BudgetInteractor.currentPeriod(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
) {
    val filteredBudgets: List<Budget>
        get() = selectedPocket?.let { pocket ->
            budgets.filter { it.pocketId == pocket.id }
        } ?: emptyList()
}

class QuickAddExpenseViewModel(
    private val pocketInteractor: PocketInteractor,
    private val budgetInteractor: BudgetInteractor,
    private val expenseInteractor: ExpenseInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(QuickAddExpenseState())
    val state: StateFlow<QuickAddExpenseState> = _state.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val pockets = pocketInteractor.getPockets()
                val period = _state.value.currentPeriod
                val budgets = budgetInteractor.getBudgetsByPeriod(period)

                val defaultPocket = pockets.firstOrNull()
                val defaultBudget = defaultPocket?.let { pocket ->
                    budgets.find { it.pocketId == pocket.id }
                }

                _state.value = _state.value.copy(
                    pockets = pockets,
                    budgets = budgets,
                    selectedPocket = defaultPocket,
                    selectedBudget = defaultBudget,
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

    fun updateDescription(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun updateAmount(amount: String) {
        _state.value = _state.value.copy(amount = amount)
    }

    fun updateDate(date: LocalDate) {
        _state.value = _state.value.copy(date = date)
    }

    fun selectPocket(pocket: Pocket) {
        val budgetsForPocket = _state.value.budgets.filter { it.pocketId == pocket.id }
        _state.value = _state.value.copy(
            selectedPocket = pocket,
            selectedBudget = budgetsForPocket.firstOrNull()
        )
    }

    fun selectBudget(budget: Budget) {
        _state.value = _state.value.copy(selectedBudget = budget)
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

        val pocket = currentState.selectedPocket
        if (pocket == null) {
            _state.value = currentState.copy(errorMessage = "Please select a pocket")
            return
        }

        viewModelScope.launch {
            try {
                // Get or create uncategorized budget if no budget selected or placeholder uncategorized (id=0)
                val selectedBudget = currentState.selectedBudget
                val budget = if (selectedBudget == null || selectedBudget.id == 0L) {
                    budgetInteractor.getOrCreateUncategorizedBudget(
                        pocketId = pocket.id,
                        period = currentState.currentPeriod
                    )
                } else {
                    selectedBudget
                }

                expenseInteractor.createExpense(
                    budgetId = budget.id,
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
