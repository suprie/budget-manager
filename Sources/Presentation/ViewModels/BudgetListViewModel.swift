import Foundation
import Combine

@MainActor
class BudgetListViewModel: ObservableObject {
    @Published var budgets: [Budget] = []
    @Published var errorMessage: String?
    
    private let budgetInteractor: BudgetInteractor
    
    init(budgetInteractor: BudgetInteractor) {
        self.budgetInteractor = budgetInteractor
    }
    
    func loadBudgets() async {
        do {
            budgets = try await budgetInteractor.getBudgets()
        } catch {
            errorMessage = "Failed to load budgets: \(error.localizedDescription)"
        }
    }
    
    func deleteBudget(at offsets: IndexSet) async {
        for index in offsets {
            let budget = budgets[index]
            do {
                try await budgetInteractor.deleteBudget(id: budget.id)
                budgets.remove(at: index)
            } catch {
                errorMessage = "Failed to delete budget: \(error.localizedDescription)"
            }
        }
    }
}
