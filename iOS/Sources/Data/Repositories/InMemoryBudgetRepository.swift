import Foundation

class InMemoryBudgetRepository: BudgetRepositoryProtocol {
    private var budgets: [Budget] = []
    private var nextId: Int64 = 1

    func fetchAll() async throws -> [Budget] {
        return budgets.sorted { $0.period > $1.period }
    }

    func fetch(id: Int64) async throws -> Budget {
        guard let budget = budgets.first(where: { $0.id == id }) else {
            throw DomainError.notFound
        }
        return budget
    }

    func fetchByPocketId(_ pocketId: Int64) async throws -> [Budget] {
        return budgets
            .filter { $0.pocketId == pocketId }
            .sorted { $0.period > $1.period }
    }

    func fetchByPeriod(_ period: String) async throws -> [Budget] {
        return budgets
            .filter { $0.period == period }
            .sorted { $0.name < $1.name }
    }

    func add(_ budget: Budget) async throws -> Budget {
        let newBudget = Budget(
            id: nextId,
            name: budget.name,
            description: budget.description,
            pocketId: budget.pocketId,
            allocatedAmount: budget.allocatedAmount,
            spentAmount: budget.spentAmount,
            period: budget.period,
            createdAt: budget.createdAt,
            updatedAt: budget.updatedAt
        )
        nextId += 1
        budgets.append(newBudget)
        return newBudget
    }

    func update(_ budget: Budget) async throws {
        guard let index = budgets.firstIndex(where: { $0.id == budget.id }) else {
            throw DomainError.notFound
        }
        var updated = budget
        updated.updatedAt = Date()
        budgets[index] = updated
    }

    func updateSpentAmount(id: Int64, amount: Double) async throws {
        guard let index = budgets.firstIndex(where: { $0.id == id }) else {
            throw DomainError.notFound
        }
        budgets[index].spentAmount += amount
        budgets[index].updatedAt = Date()
    }

    func delete(id: Int64) async throws {
        guard budgets.contains(where: { $0.id == id }) else {
            throw DomainError.notFound
        }
        budgets.removeAll { $0.id == id }
    }

    func hasBudgets(forPocketId pocketId: Int64) -> Bool {
        budgets.contains { $0.pocketId == pocketId }
    }
}
