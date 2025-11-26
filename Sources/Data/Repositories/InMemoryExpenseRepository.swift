import Foundation

class InMemoryExpenseRepository: ExpenseRepositoryProtocol {
    private var expenses: [Expense] = []
    
    func fetchExpenses(for budgetId: UUID) async throws -> [Expense] {
        return expenses.filter { $0.budgetId == budgetId }
    }
    
    func addExpense(_ expense: Expense) async throws {
        expenses.append(expense)
    }
    
    func deleteExpense(id: UUID) async throws {
        expenses.removeAll { $0.id == id }
    }
}
