import Foundation

class BudgetInteractor {
    private let budgetRepository: BudgetRepositoryProtocol
    private let pocketRepository: PocketRepositoryProtocol
    private let expenseRepository: InMemoryExpenseRepository

    init(
        budgetRepository: BudgetRepositoryProtocol,
        pocketRepository: PocketRepositoryProtocol,
        expenseRepository: InMemoryExpenseRepository
    ) {
        self.budgetRepository = budgetRepository
        self.pocketRepository = pocketRepository
        self.expenseRepository = expenseRepository
    }

    func getBudgets() async throws -> [Budget] {
        return try await budgetRepository.fetchAll()
    }

    func getBudget(id: Int64) async throws -> Budget {
        return try await budgetRepository.fetch(id: id)
    }

    func getBudgets(forPocketId pocketId: Int64) async throws -> [Budget] {
        return try await budgetRepository.fetchByPocketId(pocketId)
    }

    func getBudgets(forPeriod period: String) async throws -> [Budget] {
        return try await budgetRepository.fetchByPeriod(period)
    }

    func createBudget(
        name: String,
        description: String = "",
        pocketId: Int64,
        allocatedAmount: Double,
        period: String
    ) async throws -> Budget {
        guard !name.isEmpty else {
            throw DomainError.invalidInput("Name cannot be empty")
        }
        guard !period.isEmpty else {
            throw DomainError.invalidInput("Period cannot be empty")
        }
        guard allocatedAmount >= 0 else {
            throw DomainError.invalidInput("Allocated amount must be non-negative")
        }

        let pocket = try await pocketRepository.fetch(id: pocketId)
        guard pocket.balance >= allocatedAmount else {
            throw DomainError.insufficientFunds
        }

        let budget = Budget(
            name: name,
            description: description,
            pocketId: pocketId,
            allocatedAmount: allocatedAmount,
            period: period
        )

        let createdBudget = try await budgetRepository.add(budget)
        try await pocketRepository.updateBalance(id: pocketId, amount: -allocatedAmount)

        return createdBudget
    }

    func updateBudget(
        id: Int64,
        name: String?,
        description: String?,
        allocatedAmount: Double?
    ) async throws -> Budget {
        var budget = try await budgetRepository.fetch(id: id)

        if let name = name {
            budget.name = name
        }
        if let description = description {
            budget.description = description
        }
        if let newAmount = allocatedAmount {
            let diff = newAmount - budget.allocatedAmount

            if diff > 0 {
                let pocket = try await pocketRepository.fetch(id: budget.pocketId)
                guard pocket.balance >= diff else {
                    throw DomainError.insufficientFunds
                }
            }

            budget.allocatedAmount = newAmount
            try await pocketRepository.updateBalance(id: budget.pocketId, amount: -diff)
        }

        try await budgetRepository.update(budget)
        return budget
    }

    func deleteBudget(id: Int64) async throws {
        if expenseRepository.hasExpenses(forBudgetId: id) {
            throw DomainError.budgetHasExpenses
        }

        let budget = try await budgetRepository.fetch(id: id)
        let unspentAmount = budget.allocatedAmount - budget.spentAmount

        if unspentAmount > 0 {
            try await pocketRepository.updateBalance(id: budget.pocketId, amount: unspentAmount)
        }

        try await budgetRepository.delete(id: id)
    }

    func getRemainingBudget(id: Int64) async throws -> Double {
        let budget = try await budgetRepository.fetch(id: id)
        return budget.remainingAmount
    }

    func getSummary(forPeriod period: String) async throws -> BudgetSummary {
        let budgets = try await budgetRepository.fetchByPeriod(period)
        let pockets = try await pocketRepository.fetchAll()

        let totalAllocated = budgets.reduce(0) { $0 + $1.allocatedAmount }
        let totalSpent = budgets.reduce(0) { $0 + $1.spentAmount }
        let unallocatedFunds = pockets.reduce(0) { $0 + $1.balance }

        return BudgetSummary(
            period: period,
            totalAllocated: totalAllocated,
            totalSpent: totalSpent,
            totalRemaining: totalAllocated - totalSpent,
            unallocatedFunds: unallocatedFunds
        )
    }

    static func currentPeriod() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM"
        return formatter.string(from: Date())
    }
}
