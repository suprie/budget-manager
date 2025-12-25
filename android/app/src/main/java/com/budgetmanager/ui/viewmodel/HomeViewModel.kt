package com.budgetmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmanager.domain.entity.Budget
import com.budgetmanager.domain.entity.Expense
import com.budgetmanager.domain.interactor.BudgetInteractor
import com.budgetmanager.domain.interactor.ExpenseInteractor
import com.budgetmanager.domain.interactor.PocketInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeState(
    val totalPocketBalance: Double = 0.0,
    val totalSpent: Double = 0.0,
    val budgets: List<Budget> = emptyList(),
    val recentTransactions: List<ExpenseWithBudgetName> = emptyList(),
    val currentPeriod: String = BudgetInteractor.currentPeriod(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val remainingBalance: Double
        get() = totalPocketBalance - totalSpent
}

data class ExpenseWithBudgetName(
    val expense: Expense,
    val budgetName: String
)

class HomeViewModel(
    private val pocketInteractor: PocketInteractor,
    private val budgetInteractor: BudgetInteractor,
    private val expenseInteractor: ExpenseInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val period = _state.value.currentPeriod

                // Load pockets to get total balance
                val pockets = pocketInteractor.getPockets()
                val totalPocketBalance = pockets.sumOf { it.balance }

                // Load budgets for current period
                val budgets = budgetInteractor.getBudgetsByPeriod(period)
                val totalSpent = budgets.sumOf { it.spentAmount }

                // Load all expenses and take latest 10
                val allExpenses = expenseInteractor.getExpenses()
                val recentExpenses = allExpenses.take(10)

                // Map expenses to include budget names
                val budgetMap = budgets.associateBy { it.id }
                val allBudgets = budgetInteractor.getBudgets()
                val fullBudgetMap = allBudgets.associateBy { it.id }

                val recentTransactions = recentExpenses.map { expense ->
                    ExpenseWithBudgetName(
                        expense = expense,
                        budgetName = fullBudgetMap[expense.budgetId]?.name ?: "Unknown"
                    )
                }

                _state.value = _state.value.copy(
                    totalPocketBalance = totalPocketBalance,
                    totalSpent = totalSpent,
                    budgets = budgets,
                    recentTransactions = recentTransactions,
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

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
