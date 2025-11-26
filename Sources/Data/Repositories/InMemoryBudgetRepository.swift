import Foundation

class InMemoryBudgetRepository: BudgetRepositoryProtocol {
    private var budgets: [Budget] = []
    
    func fetchBudgets() async throws -> [Budget] {
        return budgets
    }
    
    func addBudget(_ budget: Budget) async throws {
        budgets.append(budget)
    }
    
    func deleteBudget(id: UUID) async throws {
        budgets.removeAll { $0.id == id }
    }
    
    func updateBudget(_ budget: Budget) async throws {
        if let index = budgets.firstIndex(where: { $0.id == budget.id }) {
            budgets[index] = budget
        }
    }
}
