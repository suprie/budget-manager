import Foundation
import Combine

@MainActor
class BudgetDetailViewModel: ObservableObject {
    @Published var budget: Budget
    @Published var expenses: [Expense] = []
    @Published var errorMessage: String?
    @Published var isLoading = false

    private let budgetInteractor: BudgetInteractor
    private let expenseInteractor: ExpenseInteractor

    init(budget: Budget, budgetInteractor: BudgetInteractor, expenseInteractor: ExpenseInteractor) {
        self.budget = budget
        self.budgetInteractor = budgetInteractor
        self.expenseInteractor = expenseInteractor
    }

    var remainingBudget: Double {
        budget.remainingAmount
    }

    var spentPercentage: Double {
        guard budget.allocatedAmount > 0 else { return 0 }
        return budget.spentAmount / budget.allocatedAmount
    }

    func loadExpenses() async {
        isLoading = true
        do {
            expenses = try await expenseInteractor.getExpenses(forBudgetId: budget.id)
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func refreshBudget() async {
        do {
            budget = try await budgetInteractor.getBudget(id: budget.id)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func deleteExpense(at offsets: IndexSet) async {
        for index in offsets {
            let expense = expenses[index]
            do {
                try await expenseInteractor.deleteExpense(id: expense.id)
                expenses.remove(at: index)
                await refreshBudget()
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }
}
