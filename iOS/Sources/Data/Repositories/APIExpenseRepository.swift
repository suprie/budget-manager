import Foundation

class APIExpenseRepository: ExpenseRepositoryProtocol {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func fetchAll() async throws -> [Expense] {
        do {
            return try await apiClient.request(.getAllExpenses)
        } catch let error as APIError {
            throw mapAPIError(error)
        }
    }

    func fetch(id: Int64) async throws -> Expense {
        do {
            return try await apiClient.request(.getExpense(id: id))
        } catch let error as APIError {
            throw mapAPIError(error)
        }
    }

    func fetchByBudgetId(_ budgetId: Int64) async throws -> [Expense] {
        do {
            return try await apiClient.request(.getExpensesByBudget(budgetId: budgetId))
        } catch let error as APIError {
            throw mapAPIError(error)
        }
    }

    func fetchByDateRange(start: Date, end: Date) async throws -> [Expense] {
        do {
            return try await apiClient.request(.getExpensesByDateRange(startDate: start, endDate: end))
        } catch let error as APIError {
            throw mapAPIError(error)
        }
    }

    func add(_ expense: Expense) async throws -> Expense {
        do {
            return try await apiClient.request(
                .createExpense(
                    budgetId: expense.budgetId,
                    amount: expense.amount,
                    description: expense.description,
                    date: expense.date
                )
            )
        } catch let error as APIError {
            throw mapAPIError(error)
        }
    }

    func update(_ expense: Expense) async throws {
        do {
            let _: Expense = try await apiClient.request(
                .updateExpense(
                    id: expense.id,
                    budgetId: expense.budgetId,
                    amount: expense.amount,
                    description: expense.description,
                    date: expense.date
                )
            )
        } catch let error as APIError {
            throw mapAPIError(error)
        }
    }

    func delete(id: Int64) async throws {
        do {
            try await apiClient.requestVoid(.deleteExpense(id: id))
        } catch let error as APIError {
            throw mapAPIError(error)
        }
    }

    private func mapAPIError(_ error: APIError) -> DomainError {
        switch error {
        case .unauthorized:
            return .unauthorized
        case .notFound:
            return .notFound
        case .httpError(_, let message):
            if let message = message {
                if message.lowercased().contains("insufficient") {
                    return .insufficientFunds
                }
                return .serverError(message)
            }
            return .serverError("Unknown error")
        case .networkError(let underlyingError):
            return .networkError(underlyingError.localizedDescription)
        case .decodingError(let underlyingError):
            return .networkError("Failed to parse response: \(underlyingError.localizedDescription)")
        case .invalidURL, .invalidResponse:
            return .networkError(error.localizedDescription ?? "Network error")
        case .serverError(let message):
            return .serverError(message)
        }
    }
}
