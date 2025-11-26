import SwiftUI

@main
struct BudgetManagerApp: App {
    // Composition Root
    // In a larger app, we would move this to a DI Container
    
    let budgetRepository: BudgetRepositoryProtocol
    let expenseRepository: ExpenseRepositoryProtocol
    
    let budgetInteractor: BudgetInteractor
    let expenseInteractor: ExpenseInteractor
    
    init() {
        // Initialize Repositories
        let budgetRepo = InMemoryBudgetRepository()
        let expenseRepo = InMemoryExpenseRepository()
        
        self.budgetRepository = budgetRepo
        self.expenseRepository = expenseRepo
        
        // Initialize Interactors
        self.budgetInteractor = BudgetInteractor(repository: budgetRepo)
        self.expenseInteractor = ExpenseInteractor(repository: expenseRepo)
    }
    
    var body: some Scene {
        WindowGroup {
            BudgetListView(budgetInteractor: budgetInteractor, expenseInteractor: expenseInteractor)
        }
    }
}
