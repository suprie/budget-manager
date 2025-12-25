import Foundation

enum DomainError: LocalizedError {
    case notFound
    case invalidInput(String)
    case insufficientFunds
    case pocketHasBudgets
    case budgetHasExpenses
    case emailAlreadyExists
    case invalidCredentials
    case unauthorized
    case networkError(String)
    case serverError(String)

    var errorDescription: String? {
        switch self {
        case .notFound:
            return "Resource not found"
        case .invalidInput(let message):
            return message
        case .insufficientFunds:
            return "Insufficient funds in budget"
        case .pocketHasBudgets:
            return "Cannot delete pocket with associated budgets"
        case .budgetHasExpenses:
            return "Cannot delete budget with associated expenses"
        case .emailAlreadyExists:
            return "Email already exists"
        case .invalidCredentials:
            return "Invalid email or password"
        case .unauthorized:
            return "Unauthorized"
        case .networkError(let message):
            return "Network error: \(message)"
        case .serverError(let message):
            return "Server error: \(message)"
        }
    }
}
