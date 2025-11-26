import SwiftUI

struct BudgetDetailView: View {
    @StateObject private var viewModel: BudgetDetailViewModel
    @State private var isShowingAddExpense = false
    
    let expenseInteractor: ExpenseInteractor
    
    init(budget: Budget, expenseInteractor: ExpenseInteractor) {
        self.expenseInteractor = expenseInteractor
        _viewModel = StateObject(wrappedValue: BudgetDetailViewModel(budget: budget, expenseInteractor: expenseInteractor))
    }
    
    var body: some View {
        VStack {
            VStack(spacing: 10) {
                Text("Limit: \(viewModel.budget.limit, format: .currency(code: "USD"))")
                    .font(.headline)
                Text("Spent: \(viewModel.totalSpent, format: .currency(code: "USD"))")
                    .foregroundColor(.orange)
                Text("Remaining: \(viewModel.remainingBudget, format: .currency(code: "USD"))")
                    .foregroundColor(viewModel.remainingBudget >= 0 ? .green : .red)
                    .font(.title2)
                    .bold()
            }
            .padding()
            .background(Color(UIColor.secondarySystemBackground))
            .cornerRadius(10)
            .padding()
            
            List {
                ForEach(viewModel.expenses) { expense in
                    HStack {
                        VStack(alignment: .leading) {
                            Text(expense.description)
                                .font(.body)
                            Text(expense.date, style: .date)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Text(expense.amount, format: .currency(code: "USD"))
                    }
                }
                .onDelete { indexSet in
                    Task {
                        await viewModel.deleteExpense(at: indexSet)
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
                }
            }
        }
        .task {
            await viewModel.loadExpenses()
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
