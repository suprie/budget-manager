import SwiftUI

struct AddExpenseView: View {
    @Environment(\.dismiss) var dismiss
    @StateObject private var viewModel: AddExpenseViewModel
    let budget: Budget
    var onSave: () -> Void

    init(budget: Budget, expenseInteractor: ExpenseInteractor, onSave: @escaping () -> Void) {
        self.budget = budget
        self.onSave = onSave
        _viewModel = StateObject(wrappedValue: AddExpenseViewModel(
            budgetId: budget.id,
            expenseInteractor: expenseInteractor
        ))
    }

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Expense Details")) {
                    TextField("Description", text: $viewModel.description)
                    TextField("Amount", text: $viewModel.amount)
                        .keyboardType(.decimalPad)
                    DatePicker("Date", selection: $viewModel.date, displayedComponents: .date)
                }

                Section(header: Text("Budget Info")) {
                    HStack {
                        Text("Budget")
                        Spacer()
                        Text(budget.name)
                            .foregroundColor(.secondary)
                    }
                    HStack {
                        Text("Remaining")
                        Spacer()
                        Text(budget.remainingAmount, format: .currency(code: "USD"))
                            .foregroundColor(budget.remainingAmount >= 0 ? .green : .red)
                    }
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
