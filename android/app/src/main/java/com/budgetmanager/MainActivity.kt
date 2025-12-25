package com.budgetmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.budgetmanager.data.settings.AppSettings
import com.budgetmanager.di.AppContainer
import com.budgetmanager.ui.navigation.AppNavigation
import com.budgetmanager.ui.theme.BudgetManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize app settings and DI container
        AppSettings.init(applicationContext)
        AppContainer.init(applicationContext)

        enableEdgeToEdge()
        setContent {
            BudgetManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
