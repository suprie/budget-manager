import Foundation
import Combine

@MainActor
class AddBudgetViewModel: ObservableObject {
    @Published var name: String = ""
    @Published var description: String = ""
    @Published var allocatedAmount: String = ""
    @Published var selectedPocket: Pocket?
    @Published var pockets: [Pocket] = []
    @Published var errorMessage: String?
    @Published var isLoading = false

    private let budgetInteractor: BudgetInteractor
    private let pocketInteractor: PocketInteractor
    private let period: String

    init(budgetInteractor: BudgetInteractor, pocketInteractor: PocketInteractor, period: String? = nil) {
        self.budgetInteractor = budgetInteractor
        self.pocketInteractor = pocketInteractor
        self.period = period ?? BudgetInteractor.currentPeriod()
    }

    func loadPockets() async {
        isLoading = true
        do {
            pockets = try await pocketInteractor.getPockets()
            if selectedPocket == nil && !pockets.isEmpty {
                selectedPocket = pockets.first
            }
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func addBudget() async -> Bool {
        guard !name.isEmpty else {
            errorMessage = "Name cannot be empty"
            return false
        }

        guard let amount = Double(allocatedAmount), amount > 0 else {
            errorMessage = "Amount must be a valid number greater than 0"
            return false
        }

        guard let pocket = selectedPocket else {
            errorMessage = "Please select a pocket"
            return false
        }

        do {
            _ = try await budgetInteractor.createBudget(
                name: name,
                description: description,
                pocketId: pocket.id,
                allocatedAmount: amount,
                period: period
            )
            return true
        } catch {
            errorMessage = error.localizedDescription
            return false
        }
    }
}
