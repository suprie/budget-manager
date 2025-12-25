package com.budgetmanager.ui.navigation

sealed class NavRoutes(val route: String) {
    data object Login : NavRoutes("auth/login")
    data object Register : NavRoutes("auth/register")
    data object PocketList : NavRoutes("pockets")
    data object AddPocket : NavRoutes("pockets/add")
    data object PocketDetail : NavRoutes("pockets/{pocketId}") {
        fun createRoute(pocketId: String) = "pockets/$pocketId"
    }

    data object BudgetList : NavRoutes("budgets")
    data object AddBudget : NavRoutes("budgets/add?pocketId={pocketId}") {
        fun createRoute(pocketId: String? = null) =
            if (pocketId != null) "budgets/add?pocketId=$pocketId" else "budgets/add"
    }

    data object BudgetDetail : NavRoutes("budgets/{budgetId}") {
        fun createRoute(budgetId: String) = "budgets/$budgetId"
    }

    data object AddExpense : NavRoutes("budgets/{budgetId}/expenses/add") {
        fun createRoute(budgetId: String) = "budgets/$budgetId/expenses/add"
    }
}
