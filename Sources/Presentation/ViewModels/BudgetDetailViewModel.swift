import Foundation
import Combine

@MainActor
class BudgetDetailViewModel: ObservableObject {
    @Published var expenses: [Expense] = []
    @Published var totalSpent: Double = 0
    @Published var errorMessage: String?
    
    let budget: Budget
    private let expenseInteractor: ExpenseInteractor
    
    init(budget: Budget, expenseInteractor: ExpenseInteractor) {
        self.budget = budget
        self.expenseInteractor = expenseInteractor
    }
    
    func loadExpenses() async {
        do {
            expenses = try await expenseInteractor.getExpenses(for: budget.id)
            calculateTotal()
        } catch {
            errorMessage = "Failed to load expenses: \(error.localizedDescription)"
        }
    }
    
    func deleteExpense(at offsets: IndexSet) async {
        for index in offsets {
            let expense = expenses[index]
            do {
                try await expenseInteractor.deleteExpense(id: expense.id)
                expenses.remove(at: index)
                calculateTotal()
            } catch {
                errorMessage = "Failed to delete expense: \(error.localizedDescription)"
            }
        }
    }
    
    private func calculateTotal() {
        totalSpent = expenses.reduce(0) { $0 + $1.amount }
    }
    
    var remainingBudget: Double {
        budget.limit - totalSpent
    }
}
