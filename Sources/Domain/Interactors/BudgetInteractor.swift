import Foundation

class BudgetInteractor {
    private let repository: BudgetRepositoryProtocol
    
    init(repository: BudgetRepositoryProtocol) {
        self.repository = repository
    }
    
    func getBudgets() async throws -> [Budget] {
        return try await repository.fetchBudgets()
    }
    
    func createBudget(name: String, limit: Double, category: BudgetCategory) async throws {
        let budget = Budget(name: name, limit: limit, category: category)
        try await repository.addBudget(budget)
    }
    
    func deleteBudget(id: UUID) async throws {
        try await repository.deleteBudget(id: id)
    }
}
