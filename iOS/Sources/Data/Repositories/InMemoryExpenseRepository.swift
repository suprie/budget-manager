import Foundation

class InMemoryExpenseRepository: ExpenseRepositoryProtocol {
    private var expenses: [Expense] = []
    private var nextId: Int64 = 1

    func fetchAll() async throws -> [Expense] {
        return expenses.sorted { $0.date > $1.date }
    }

    func fetch(id: Int64) async throws -> Expense {
        guard let expense = expenses.first(where: { $0.id == id }) else {
            throw DomainError.notFound
        }
        return expense
    }

    func fetchByBudgetId(_ budgetId: Int64) async throws -> [Expense] {
        return expenses
            .filter { $0.budgetId == budgetId }
            .sorted { $0.date > $1.date }
    }

    func fetchByDateRange(start: Date, end: Date) async throws -> [Expense] {
        return expenses
            .filter { $0.date >= start && $0.date <= end }
            .sorted { $0.date > $1.date }
    }

    func add(_ expense: Expense) async throws -> Expense {
        var newExpense = expense
        newExpense = Expense(
            id: nextId,
            budgetId: expense.budgetId,
            amount: expense.amount,
            description: expense.description,
            date: expense.date,
            createdAt: expense.createdAt,
            updatedAt: expense.updatedAt
        )
        nextId += 1
        expenses.append(newExpense)
        return newExpense
    }

    func update(_ expense: Expense) async throws {
        guard let index = expenses.firstIndex(where: { $0.id == expense.id }) else {
            throw DomainError.notFound
        }
        var updated = expense
        updated = Expense(
            id: expense.id,
            budgetId: expense.budgetId,
            amount: expense.amount,
            description: expense.description,
            date: expense.date,
            createdAt: expense.createdAt,
            updatedAt: Date()
        )
        expenses[index] = updated
    }

    func delete(id: Int64) async throws {
        guard expenses.contains(where: { $0.id == id }) else {
            throw DomainError.notFound
        }
        expenses.removeAll { $0.id == id }
    }

    func hasExpenses(forBudgetId budgetId: Int64) -> Bool {
        expenses.contains { $0.budgetId == budgetId }
    }
}
