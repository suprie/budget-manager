import Foundation
import Combine

@MainActor
class AddExpenseViewModel: ObservableObject {
    @Published var description: String = ""
    @Published var amount: String = ""
    @Published var errorMessage: String?
    
    private let budgetId: UUID
    private let expenseInteractor: ExpenseInteractor
    
    init(budgetId: UUID, expenseInteractor: ExpenseInteractor) {
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
            try await expenseInteractor.addExpense(budgetId: budgetId, amount: amountValue, description: description)
            return true
        } catch {
            errorMessage = "Failed to add expense: \(error.localizedDescription)"
            return false
        }
    }
}
