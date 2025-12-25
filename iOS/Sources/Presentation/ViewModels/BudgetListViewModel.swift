import Foundation
import Combine

@MainActor
class BudgetListViewModel: ObservableObject {
    @Published var budgets: [Budget] = []
    @Published var summary: BudgetSummary?
    @Published var currentPeriod: String
    @Published var errorMessage: String?
    @Published var isLoading = false

    private let budgetInteractor: BudgetInteractor

    init(budgetInteractor: BudgetInteractor) {
        self.budgetInteractor = budgetInteractor
        self.currentPeriod = BudgetInteractor.currentPeriod()
    }

    func loadBudgets() async {
        isLoading = true
        do {
            budgets = try await budgetInteractor.getBudgets(forPeriod: currentPeriod)
            summary = try await budgetInteractor.getSummary(forPeriod: currentPeriod)
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func deleteBudget(at offsets: IndexSet) async {
        for index in offsets {
            let budget = budgets[index]
            do {
                try await budgetInteractor.deleteBudget(id: budget.id)
                budgets.remove(at: index)
                summary = try await budgetInteractor.getSummary(forPeriod: currentPeriod)
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }
}
