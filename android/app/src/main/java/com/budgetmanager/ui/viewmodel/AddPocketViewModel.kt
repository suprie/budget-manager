package com.budgetmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmanager.domain.interactor.PocketInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddPocketState(
    val name: String = "",
    val description: String = "",
    val balance: String = "",
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

class AddPocketViewModel(
    private val pocketInteractor: PocketInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(AddPocketState())
    val state: StateFlow<AddPocketState> = _state.asStateFlow()

    fun updateName(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun updateDescription(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun updateBalance(balance: String) {
        _state.value = _state.value.copy(balance = balance)
    }

    fun savePocket() {
        val currentState = _state.value

        if (currentState.name.isBlank()) {
            _state.value = currentState.copy(errorMessage = "Name cannot be empty")
            return
        }

        val balanceValue = currentState.balance.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            try {
                pocketInteractor.createPocket(
                    name = currentState.name,
                    description = currentState.description,
                    balance = balanceValue
                )
                _state.value = currentState.copy(isSaved = true)
            } catch (e: Exception) {
                _state.value = currentState.copy(errorMessage = e.message ?: "Failed to save pocket")
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
