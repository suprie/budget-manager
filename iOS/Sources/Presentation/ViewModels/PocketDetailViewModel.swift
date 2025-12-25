import Foundation
import Combine

@MainActor
class PocketDetailViewModel: ObservableObject {
    @Published var pocket: Pocket
    @Published var budgets: [Budget] = []
    @Published var errorMessage: String?
    @Published var isLoading = false

    private let pocketInteractor: PocketInteractor
    private let budgetInteractor: BudgetInteractor

    init(pocket: Pocket, pocketInteractor: PocketInteractor, budgetInteractor: BudgetInteractor) {
        self.pocket = pocket
        self.pocketInteractor = pocketInteractor
        self.budgetInteractor = budgetInteractor
    }

    var totalAllocated: Double {
        budgets.reduce(0) { $0 + $1.allocatedAmount }
    }

    var totalSpent: Double {
        budgets.reduce(0) { $0 + $1.spentAmount }
    }

    func loadBudgets() async {
        isLoading = true
        do {
            budgets = try await budgetInteractor.getBudgets(forPocketId: pocket.id)
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func refreshPocket() async {
        do {
            pocket = try await pocketInteractor.getPocket(id: pocket.id)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func addFunds(amount: Double) async -> Bool {
        do {
            pocket = try await pocketInteractor.addFunds(id: pocket.id, amount: amount)
            return true
        } catch {
            errorMessage = error.localizedDescription
            return false
        }
    }

    func deleteBudget(at offsets: IndexSet) async {
        for index in offsets {
            let budget = budgets[index]
            do {
                try await budgetInteractor.deleteBudget(id: budget.id)
                budgets.remove(at: index)
                await refreshPocket()
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }
}
