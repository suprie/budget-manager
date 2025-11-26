import Foundation

class ExpenseInteractor {
    private let repository: ExpenseRepositoryProtocol
    
    init(repository: ExpenseRepositoryProtocol) {
        self.repository = repository
    }
    
    func getExpenses(for budgetId: UUID) async throws -> [Expense] {
        return try await repository.fetchExpenses(for: budgetId)
    }
    
    func addExpense(budgetId: UUID, amount: Double, description: String) async throws {
        let expense = Expense(budgetId: budgetId, amount: amount, description: description)
        try await repository.addExpense(expense)
    }
    
    func deleteExpense(id: UUID) async throws {
        try await repository.deleteExpense(id: id)
    }
}
