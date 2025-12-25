import SwiftUI

struct MainTabView: View {
    let pocketInteractor: PocketInteractor
    let budgetInteractor: BudgetInteractor
    let expenseInteractor: ExpenseInteractor

    var body: some View {
        TabView {
            PocketListView(
                pocketInteractor: pocketInteractor,
                budgetInteractor: budgetInteractor,
                expenseInteractor: expenseInteractor
            )
            .tabItem {
                Label("Pockets", systemImage: "wallet.pass")
            }

            BudgetListView(
                budgetInteractor: budgetInteractor,
                pocketInteractor: pocketInteractor,
                expenseInteractor: expenseInteractor
            )
            .tabItem {
                Label("Budgets", systemImage: "chart.pie")
            }
        }
    }
}
