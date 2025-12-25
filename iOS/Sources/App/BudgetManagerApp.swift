import SwiftUI

@main
struct BudgetManagerApp: App {
    // Composition Root - In a larger app, use a DI Container

    // Set to true to use API backend, false for in-memory storage
    private static let useAPIBackend = false

    // Repositories
    let authRepository: InMemoryAuthRepository
    let pocketRepository: InMemoryPocketRepository
    let budgetRepository: InMemoryBudgetRepository
    let expenseRepository: InMemoryExpenseRepository
    let apiExpenseRepository: APIExpenseRepository?

    // Interactors
    @StateObject var authInteractor: AuthInteractor
    let pocketInteractor: PocketInteractor
    let budgetInteractor: BudgetInteractor
    let expenseInteractor: ExpenseInteractor

    // ViewModels
    let authViewModel: AuthViewModel

    init() {
        // Initialize Repositories
        let authRepo = InMemoryAuthRepository()
        let pocketRepo = InMemoryPocketRepository()
        let budgetRepo = InMemoryBudgetRepository()
        let expenseRepo = InMemoryExpenseRepository()

        self.authRepository = authRepo
        self.pocketRepository = pocketRepo
        self.budgetRepository = budgetRepo
        self.expenseRepository = expenseRepo

        // Initialize API repository if using backend
        if Self.useAPIBackend {
            self.apiExpenseRepository = APIExpenseRepository()
        } else {
            self.apiExpenseRepository = nil
        }

        // Initialize Interactors with dependencies
        let authInt = AuthInteractor(authRepository: authRepo)
        _authInteractor = StateObject(wrappedValue: authInt)

        self.pocketInteractor = PocketInteractor(
            pocketRepository: pocketRepo,
            budgetRepository: budgetRepo
        )

        self.budgetInteractor = BudgetInteractor(
            budgetRepository: budgetRepo,
            pocketRepository: pocketRepo,
            expenseRepository: expenseRepo
        )

        // Use API repository for expenses when backend is enabled
        let expenseRepoToUse: ExpenseRepositoryProtocol = Self.useAPIBackend
            ? (apiExpenseRepository ?? expenseRepo)
            : expenseRepo

        self.expenseInteractor = ExpenseInteractor(
            expenseRepository: expenseRepoToUse,
            budgetRepository: budgetRepo
        )

        self.authViewModel = AuthViewModel(authInteractor: authInt)
    }

    var body: some Scene {
        WindowGroup {
            RootView(
                authInteractor: authInteractor,
                authViewModel: authViewModel,
                pocketInteractor: pocketInteractor,
                budgetInteractor: budgetInteractor,
                expenseInteractor: expenseInteractor
            )
        }
    }
}
