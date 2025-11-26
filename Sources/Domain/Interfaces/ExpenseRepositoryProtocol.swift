import Foundation

protocol ExpenseRepositoryProtocol {
    func fetchExpenses(for budgetId: UUID) async throws -> [Expense]
    func addExpense(_ expense: Expense) async throws
    func deleteExpense(id: UUID) async throws
}
