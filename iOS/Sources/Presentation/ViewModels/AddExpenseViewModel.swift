import Foundation
import Combine

@MainActor
class AddExpenseViewModel: ObservableObject {
    @Published var description: String = ""
    @Published var amount: String = ""
    @Published var date: Date = Date()
    @Published var errorMessage: String?

    private let budgetId: Int64
    private let expenseInteractor: ExpenseInteractor

    init(budgetId: Int64, expenseInteractor: ExpenseInteractor) {
        self.budgetId = budgetId
        self.expenseInteractor = expenseInteractor
    }

    func addExpense() async -> Bool {
        guard !description.isEmpty else {
            errorMessage = "Description cannot be empty"
            return false
        }

        guard let amountValue = Double(amount), amountValue > 0 else {
            errorMessage = "Amount must be a valid number greater than 0"
            return false
        }

        do {
            _ = try await expenseInteractor.createExpense(
                budgetId: budgetId,
                amount: amountValue,
                description: description,
                date: date
            )
            return true
        } catch {
            errorMessage = error.localizedDescription
            return false
        }
    }
}
