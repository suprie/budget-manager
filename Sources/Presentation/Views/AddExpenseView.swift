import SwiftUI

struct AddExpenseView: View {
    @Environment(\.dismiss) var dismiss
    @StateObject private var viewModel: AddExpenseViewModel
    var onSave: () -> Void
    
    init(budget: Budget, expenseInteractor: ExpenseInteractor, onSave: @escaping () -> Void) {
        self.onSave = onSave
        _viewModel = StateObject(wrappedValue: AddExpenseViewModel(budgetId: budget.id, expenseInteractor: expenseInteractor))
    }
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Expense Details")) {
                    TextField("Description", text: $viewModel.description)
                    TextField("Amount", text: $viewModel.amount)
                        .keyboardType(.decimalPad)
                }
                
                if let errorMessage = viewModel.errorMessage {
                    Section {
                        Text(errorMessage)
                            .foregroundColor(.red)
                    }
                }
            }
            .navigationTitle("Add Expense")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        Task {
                            if await viewModel.addExpense() {
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
