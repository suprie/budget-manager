package com.budgetmanager.data.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

enum class SupportedCurrency(
    val code: String,
    val symbol: String,
    val locale: Locale
) {
    USD("USD", "$", Locale.US),
    IDR("IDR", "Rp", Locale("id", "ID")),
    EUR("EUR", "€", Locale.GERMANY),
    GBP("GBP", "£", Locale.UK),
    JPY("JPY", "¥", Locale.JAPAN),
    SGD("SGD", "S$", Locale("en", "SG"));

    companion object {
        fun fromCode(code: String): SupportedCurrency {
            return entries.find { it.code == code } ?: USD
        }
    }
}

object AppSettings {
    private const val PREFS_NAME = "budget_manager_settings"
    private const val KEY_CURRENCY = "currency"
    private const val KEY_API_BASE_URL = "api_base_url"

    private lateinit var prefs: SharedPreferences

    private val _currency = MutableStateFlow(SupportedCurrency.USD)
    val currency: StateFlow<SupportedCurrency> = _currency.asStateFlow()

    private val _apiBaseUrl = MutableStateFlow("http://10.0.2.2:8080/")
    val apiBaseUrl: StateFlow<String> = _apiBaseUrl.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _currency.value = SupportedCurrency.fromCode(
            prefs.getString(KEY_CURRENCY, SupportedCurrency.USD.code) ?: SupportedCurrency.USD.code
        )
        _apiBaseUrl.value = prefs.getString(KEY_API_BASE_URL, "http://10.0.2.2:8080/")
            ?: "http://10.0.2.2:8080/"
    }

    fun setCurrency(currency: SupportedCurrency) {
        _currency.value = currency
        prefs.edit().putString(KEY_CURRENCY, currency.code).apply()
    }

    fun setApiBaseUrl(url: String) {
        _apiBaseUrl.value = url
        prefs.edit().putString(KEY_API_BASE_URL, url).apply()
    }

    fun formatCurrency(amount: Double): String {
        val curr = _currency.value
        val format = NumberFormat.getCurrencyInstance(curr.locale)
        format.currency = Currency.getInstance(curr.code)
        return format.format(amount)
    }
}
