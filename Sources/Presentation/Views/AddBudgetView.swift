import SwiftUI

struct AddBudgetView: View {
    @Environment(\.dismiss) var dismiss
    @StateObject private var viewModel: AddBudgetViewModel
    var onSave: () -> Void
    
    init(budgetInteractor: BudgetInteractor, onSave: @escaping () -> Void) {
        self.onSave = onSave
        _viewModel = StateObject(wrappedValue: AddBudgetViewModel(budgetInteractor: budgetInteractor))
    }
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Budget Details")) {
                    TextField("Name", text: $viewModel.name)
                    TextField("Limit", text: $viewModel.limit)
                        .keyboardType(.decimalPad)
                    Picker("Category", selection: $viewModel.selectedCategory) {
                        ForEach(BudgetCategory.allCases) { category in
                            Label(category.rawValue, systemImage: category.icon)
                                .tag(category)
                        }
                    }
                }
                
                if let errorMessage = viewModel.errorMessage {
                    Section {
                        Text(errorMessage)
                            .foregroundColor(.red)
                    }
                }
            }
            .navigationTitle("Add Budget")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        Task {
                            if await viewModel.addBudget() {
                                onSave()
                                dismiss()
                            }
                        }
                    }
                }
            }
        }
    }
}
