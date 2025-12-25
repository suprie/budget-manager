import SwiftUI

struct RootView: View {
    @ObservedObject var authInteractor: AuthInteractor
    let authViewModel: AuthViewModel
    let pocketInteractor: PocketInteractor
    let budgetInteractor: BudgetInteractor
    let expenseInteractor: ExpenseInteractor

    @State private var showingRegister = false

    var body: some View {
        Group {
            if authInteractor.isLoggedIn {
                MainTabView(
                    pocketInteractor: pocketInteractor,
                    budgetInteractor: budgetInteractor,
                    expenseInteractor: expenseInteractor
                )
            } else {
                if showingRegister {
                    RegisterView(
                        viewModel: authViewModel,
                        onNavigateBack: {
                            showingRegister = false
                        }
                    )
                } else {
                    LoginView(
                        viewModel: authViewModel,
                        onNavigateToRegister: {
                            showingRegister = true
                        }
                    )
                }
            }
        }
    }
}
