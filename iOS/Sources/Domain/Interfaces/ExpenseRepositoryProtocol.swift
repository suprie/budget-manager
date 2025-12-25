import Foundation

protocol ExpenseRepositoryProtocol {
    func fetchAll() async throws -> [Expense]
    func fetch(id: Int64) async throws -> Expense
    func fetchByBudgetId(_ budgetId: Int64) async throws -> [Expense]
    func fetchByDateRange(start: Date, end: Date) async throws -> [Expense]
    func add(_ expense: Expense) async throws -> Expense
    func update(_ expense: Expense) async throws
    func delete(id: Int64) async throws
}
