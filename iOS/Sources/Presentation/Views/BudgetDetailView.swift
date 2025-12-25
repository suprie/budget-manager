import SwiftUI

struct BudgetDetailView: View {
    @StateObject private var viewModel: BudgetDetailViewModel
    @State private var isShowingAddExpense = false

    let budgetInteractor: BudgetInteractor
    let expenseInteractor: ExpenseInteractor

    init(budget: Budget, budgetInteractor: BudgetInteractor, expenseInteractor: ExpenseInteractor) {
        self.budgetInteractor = budgetInteractor
        self.expenseInteractor = expenseInteractor
        _viewModel = StateObject(wrappedValue: BudgetDetailViewModel(
            budget: budget,
            budgetInteractor: budgetInteractor,
            expenseInteractor: expenseInteractor
        ))
    }

    private var progressColor: Color {
        if viewModel.spentPercentage >= 1.0 {
            return .red
        } else if viewModel.spentPercentage >= 0.8 {
            return .orange
        }
        return .green
    }

    var body: some View {
        List {
            Section {
                VStack(spacing: 16) {
                    HStack {
                        Text("Allocated")
                        Spacer()
                        Text(viewModel.budget.allocatedAmount, format: .currency(code: "USD"))
                            .font(.headline)
                    }

                    ProgressView(
                        value: min(viewModel.budget.spentAmount, viewModel.budget.allocatedAmount),
                        total: max(viewModel.budget.allocatedAmount, 1)
                    )
                    .tint(progressColor)
                    .scaleEffect(x: 1, y: 2, anchor: .center)

                    HStack {
                        VStack(alignment: .leading) {
                            Text("Spent")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text(viewModel.budget.spentAmount, format: .currency(code: "USD"))
                                .font(.headline)
                                .foregroundColor(.orange)
                        }

                        Spacer()

                        VStack(alignment: .trailing) {
                            Text("Remaining")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text(viewModel.remainingBudget, format: .currency(code: "USD"))
                                .font(.title2)
                                .bold()
                                .foregroundColor(viewModel.remainingBudget >= 0 ? .green : .red)
                        }
                    }

                    Text("Period: \(viewModel.budget.period)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding(.vertical, 8)
            }

            Section(header: Text("Expenses")) {
                if viewModel.expenses.isEmpty {
                    Text("No expenses yet. Tap + to add one.")
                        .foregroundColor(.secondary)
                } else {
                    ForEach(viewModel.expenses) { expense in
                        ExpenseRowView(expense: expense)
                    }
                    .onDelete { indexSet in
                        Task {
                            await viewModel.deleteExpense(at: indexSet)
                        }
                    }
                }
            }
        }
        .navigationTitle(viewModel.budget.name)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { isShowingAddExpense = true }) {
                    Image(systemName: "plus")
                }
            }
        }
        .sheet(isPresented: $isShowingAddExpense) {
            AddExpenseView(budget: viewModel.budget, expenseInteractor: expenseInteractor) {
                Task {
                    await viewModel.loadExpenses()
                    await viewModel.refreshBudget()
                }
            }
        }
        .task {
            await viewModel.loadExpenses()
        }
        .refreshable {
            await viewModel.loadExpenses()
            await viewModel.refreshBudget()
        }
        .alert("Error", isPresented: Binding<Bool>(
            get: { viewModel.errorMessage != nil },
            set: { _ in viewModel.errorMessage = nil }
        )) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(viewModel.errorMessage ?? "")
        }
    }
}

struct ExpenseRowView: View {
    let expense: Expense

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(expense.description)
                    .font(.body)
                Text(expense.date, style: .date)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            Spacer()
            Text(expense.amount, format: .currency(code: "USD"))
                .font(.headline)
                .foregroundColor(.red)
        }
        .padding(.vertical, 4)
    }
}
