import Foundation

protocol BudgetRepositoryProtocol {
    func fetchBudgets() async throws -> [Budget]
    func addBudget(_ budget: Budget) async throws
    func deleteBudget(id: UUID) async throws
    func updateBudget(_ budget: Budget) async throws
}
