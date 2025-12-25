import Foundation

class ExpenseInteractor {
    private let expenseRepository: ExpenseRepositoryProtocol
    private let budgetRepository: BudgetRepositoryProtocol

    init(expenseRepository: ExpenseRepositoryProtocol, budgetRepository: BudgetRepositoryProtocol) {
        self.expenseRepository = expenseRepository
        self.budgetRepository = budgetRepository
    }

    func getExpenses() async throws -> [Expense] {
        return try await expenseRepository.fetchAll()
    }

    func getExpense(id: Int64) async throws -> Expense {
        return try await expenseRepository.fetch(id: id)
    }

    func getExpenses(forBudgetId budgetId: Int64) async throws -> [Expense] {
        return try await expenseRepository.fetchByBudgetId(budgetId)
    }

    func getExpenses(from startDate: Date, to endDate: Date) async throws -> [Expense] {
        return try await expenseRepository.fetchByDateRange(start: startDate, end: endDate)
    }

    func createExpense(
        budgetId: Int64,
        amount: Double,
        description: String,
        date: Date = Date()
    ) async throws -> Expense {
        guard amount > 0 else {
            throw DomainError.invalidInput("Amount must be greater than 0")
        }
        guard !description.isEmpty else {
            throw DomainError.invalidInput("Description cannot be empty")
        }

        let budget = try await budgetRepository.fetch(id: budgetId)
        guard budget.remainingAmount >= amount else {
            throw DomainError.insufficientFunds
        }

        let expense = Expense(
            budgetId: budgetId,
            amount: amount,
            description: description,
            date: date
        )

        let createdExpense = try await expenseRepository.add(expense)
        try await budgetRepository.updateSpentAmount(id: budgetId, amount: amount)

        return createdExpense
    }

    func updateExpense(
        id: Int64,
        budgetId: Int64?,
        amount: Double?,
        description: String?,
        date: Date?
    ) async throws -> Expense {
        var expense = try await expenseRepository.fetch(id: id)
        let oldAmount = expense.amount
        let oldBudgetId = expense.budgetId

        if let description = description {
            expense.description = description
        }
        if let date = date {
            expense.date = date
        }
        if let amount = amount {
            expense.amount = amount
        }
        if let budgetId = budgetId {
            expense.budgetId = budgetId
        }

        if expense.budgetId != oldBudgetId {
            let newBudget = try await budgetRepository.fetch(id: expense.budgetId)
            guard newBudget.remainingAmount >= expense.amount else {
                throw DomainError.insufficientFunds
            }

            try await budgetRepository.updateSpentAmount(id: oldBudgetId, amount: -oldAmount)
            try await budgetRepository.updateSpentAmount(id: expense.budgetId, amount: expense.amount)
        } else if expense.amount != oldAmount {
            let diff = expense.amount - oldAmount
            let budget = try await budgetRepository.fetch(id: expense.budgetId)

            if diff > 0 && budget.remainingAmount < diff {
                throw DomainError.insufficientFunds
            }

            try await budgetRepository.updateSpentAmount(id: expense.budgetId, amount: diff)
        }

        try await expenseRepository.update(expense)
        return expense
    }

    func deleteExpense(id: Int64) async throws {
        let expense = try await expenseRepository.fetch(id: id)
        try await budgetRepository.updateSpentAmount(id: expense.budgetId, amount: -expense.amount)
        try await expenseRepository.delete(id: id)
    }
}
