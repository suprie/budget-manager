import Foundation
import Combine

@MainActor
class AddBudgetViewModel: ObservableObject {
    @Published var name: String = ""
    @Published var limit: String = ""
    @Published var selectedCategory: BudgetCategory = .other
    @Published var errorMessage: String?
    
    private let budgetInteractor: BudgetInteractor
    
    init(budgetInteractor: BudgetInteractor) {
        self.budgetInteractor = budgetInteractor
    }
    
    func addBudget() async -> Bool {
        guard !name.isEmpty else {
            errorMessage = "Name cannot be empty"
            return false
        }
        
        guard let limitValue = Double(limit), limitValue > 0 else {
            errorMessage = "Limit must be a valid number greater than 0"
            return false
        }
        
        do {
            try await budgetInteractor.createBudget(name: name, limit: limitValue, category: selectedCategory)
            return true
        } catch {
            errorMessage = "Failed to create budget: \(error.localizedDescription)"
            return false
        }
    }
}
