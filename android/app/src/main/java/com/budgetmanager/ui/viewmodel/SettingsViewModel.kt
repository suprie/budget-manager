package com.budgetmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.budgetmanager.data.settings.AppSettings
import com.budgetmanager.data.settings.SupportedCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsState(
    val selectedCurrency: SupportedCurrency = SupportedCurrency.USD,
    val apiBaseUrl: String = "http://10.0.2.2:8080/",
    val availableCurrencies: List<SupportedCurrency> = SupportedCurrency.entries
)

class SettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun loadSettings() {
        _state.value = _state.value.copy(
            selectedCurrency = AppSettings.currency.value,
            apiBaseUrl = AppSettings.apiBaseUrl.value
        )
    }

    fun setCurrency(currency: SupportedCurrency) {
        AppSettings.setCurrency(currency)
        _state.value = _state.value.copy(selectedCurrency = currency)
    }

    fun setApiBaseUrl(url: String) {
        AppSettings.setApiBaseUrl(url)
        _state.value = _state.value.copy(apiBaseUrl = url)
    }
}
