package com.budgetmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmanager.domain.entity.Pocket
import com.budgetmanager.domain.interactor.PocketInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PocketListState(
    val pockets: List<Pocket> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val totalBalance: Double
        get() = pockets.sumOf { it.balance }
}

class PocketListViewModel(
    private val pocketInteractor: PocketInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(PocketListState())
    val state: StateFlow<PocketListState> = _state.asStateFlow()

    fun loadPockets() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val pockets = pocketInteractor.getPockets()
                _state.value = _state.value.copy(pockets = pockets, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load pockets"
                )
            }
        }
    }

    fun deletePocket(id: Long) {
        viewModelScope.launch {
            try {
                pocketInteractor.deletePocket(id)
                loadPockets()
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
