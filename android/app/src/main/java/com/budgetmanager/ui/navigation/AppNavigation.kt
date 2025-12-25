package com.budgetmanager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.budgetmanager.di.AppContainer
import com.budgetmanager.ui.screen.*
import com.budgetmanager.ui.viewmodel.*

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Shared auth view model
    val authViewModel = remember { AuthViewModel(AppContainer.authInteractor) }
    val authState by authViewModel.uiState.collectAsState()

    val bottomNavItems = listOf(
        BottomNavItem(
            route = NavRoutes.Home.route,
            label = "Home",
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
        ),
        BottomNavItem(
            route = NavRoutes.PocketList.route,
            label = "Pockets",
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Pockets") }
        ),
        BottomNavItem(
            route = NavRoutes.BudgetList.route,
            label = "Budgets",
            icon = { Icon(Icons.Default.PieChart, contentDescription = "Budgets") }
        )
    )

    val showBottomBar = currentRoute in listOf(
        NavRoutes.Home.route,
        NavRoutes.PocketList.route,
        NavRoutes.BudgetList.route
    )

    // Determine start destination based on auth state
    val startDestination = if (authState.isLoggedIn) {
        NavRoutes.Home.route
    } else {
        NavRoutes.Login.route
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar && authState.isLoggedIn) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(NavRoutes.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = item.icon,
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            // Auth routes
            composable(NavRoutes.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = {
                        navController.navigate(NavRoutes.Register.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.Register.route) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.Home.route) {
                val viewModel = remember {
                    HomeViewModel(
                        pocketInteractor = AppContainer.pocketInteractor,
                        budgetInteractor = AppContainer.budgetInteractor,
                        expenseInteractor = AppContainer.expenseInteractor
                    )
                }
                HomeScreen(
                    viewModel = viewModel,
                    onBudgetClick = { budgetId ->
                        navController.navigate(NavRoutes.BudgetDetail.createRoute(budgetId.toString()))
                    },
                    onAddExpenseClick = {
                        navController.navigate(NavRoutes.QuickAddExpense.route)
                    },
                    onSettingsClick = {
                        navController.navigate(NavRoutes.Settings.route)
                    }
                )
            }

            composable(NavRoutes.Settings.route) {
                val viewModel = remember { SettingsViewModel() }
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.QuickAddExpense.route) {
                val viewModel = remember {
                    QuickAddExpenseViewModel(
                        pocketInteractor = AppContainer.pocketInteractor,
                        budgetInteractor = AppContainer.budgetInteractor,
                        expenseInteractor = AppContainer.expenseInteractor
                    )
                }
                QuickAddExpenseScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.PocketList.route) {
                val viewModel = remember {
                    PocketListViewModel(AppContainer.pocketInteractor)
                }
                PocketListScreen(
                    viewModel = viewModel,
                    onPocketClick = { pocketId ->
                        navController.navigate(NavRoutes.PocketDetail.createRoute(pocketId.toString()))
                    },
                    onAddPocketClick = {
                        navController.navigate(NavRoutes.AddPocket.route)
                    }
                )
            }

            composable(NavRoutes.AddPocket.route) {
                val viewModel = remember {
                    AddPocketViewModel(AppContainer.pocketInteractor)
                }
                AddPocketScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.PocketDetail.route,
                arguments = listOf(navArgument("pocketId") { type = NavType.StringType })
            ) { backStackEntry ->
                val pocketId = backStackEntry.arguments?.getString("pocketId")?.toLong()!!
                val viewModel = remember(pocketId) {
                    PocketDetailViewModel(
                        pocketId = pocketId,
                        pocketInteractor = AppContainer.pocketInteractor,
                        budgetInteractor = AppContainer.budgetInteractor
                    )
                }
                PocketDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onBudgetClick = { budgetId ->
                        navController.navigate(NavRoutes.BudgetDetail.createRoute(budgetId.toString()))
                    },
                    onAddBudgetClick = {
                        navController.navigate(NavRoutes.AddBudget.createRoute(pocketId.toString()))
                    }
                )
            }

            composable(NavRoutes.BudgetList.route) {
                val viewModel = remember {
                    BudgetListViewModel(AppContainer.budgetInteractor)
                }
                BudgetListScreen(
                    viewModel = viewModel,
                    onBudgetClick = { budgetId ->
                        navController.navigate(NavRoutes.BudgetDetail.createRoute(budgetId.toString()))
                    },
                    onAddBudgetClick = {
                        navController.navigate(NavRoutes.AddBudget.createRoute())
                    }
                )
            }

            composable(
                route = NavRoutes.AddBudget.route,
                arguments = listOf(
                    navArgument("pocketId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val pocketIdStr = backStackEntry.arguments?.getString("pocketId")
                val pocketId = pocketIdStr?.toLongOrNull()
                val viewModel = remember(pocketId) {
                    AddBudgetViewModel(
                        budgetInteractor = AppContainer.budgetInteractor,
                        pocketInteractor = AppContainer.pocketInteractor,
                        preselectedPocketId = pocketId
                    )
                }
                AddBudgetScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.BudgetDetail.route,
                arguments = listOf(navArgument("budgetId") { type = NavType.StringType })
            ) { backStackEntry ->
                val budgetId = backStackEntry.arguments?.getString("budgetId")?.toLong()!!
                val viewModel = remember(budgetId) {
                    BudgetDetailViewModel(
                        budgetId = budgetId,
                        budgetInteractor = AppContainer.budgetInteractor,
                        expenseInteractor = AppContainer.expenseInteractor
                    )
                }
                BudgetDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onAddExpenseClick = {
                        navController.navigate(NavRoutes.AddExpense.createRoute(budgetId.toString()))
                    }
                )
            }

            composable(
                route = NavRoutes.AddExpense.route,
                arguments = listOf(navArgument("budgetId") { type = NavType.LongType })
            ) { backStackEntry ->
                val budgetId = backStackEntry.arguments?.getLong("budgetId")!!
                val viewModel = remember(budgetId) {
                    AddExpenseViewModel(
                        budgetId = budgetId,
                        expenseInteractor = AppContainer.expenseInteractor,
                        budgetInteractor = AppContainer.budgetInteractor
                    )
                }
                AddExpenseScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
