import SwiftUI

struct PocketDetailView: View {
    @StateObject private var viewModel: PocketDetailViewModel
    @State private var isShowingAddBudget = false
    @State private var isShowingAddFunds = false
    @State private var addFundsAmount = ""

    let pocketInteractor: PocketInteractor
    let budgetInteractor: BudgetInteractor
    let expenseInteractor: ExpenseInteractor

    init(
        pocket: Pocket,
        pocketInteractor: PocketInteractor,
        budgetInteractor: BudgetInteractor,
        expenseInteractor: ExpenseInteractor
    ) {
        self.pocketInteractor = pocketInteractor
        self.budgetInteractor = budgetInteractor
        self.expenseInteractor = expenseInteractor
        _viewModel = StateObject(wrappedValue: PocketDetailViewModel(
            pocket: pocket,
            pocketInteractor: pocketInteractor,
            budgetInteractor: budgetInteractor
        ))
    }

    var body: some View {
        List {
            Section {
                VStack(spacing: 12) {
                    HStack {
                        Text("Available Balance")
                        Spacer()
                        Text(viewModel.pocket.balance, format: .currency(code: "USD"))
                            .font(.title2)
                            .bold()
                            .foregroundColor(.green)
                    }

                    HStack {
                        Text("Total Allocated")
                        Spacer()
                        Text(viewModel.totalAllocated, format: .currency(code: "USD"))
                            .foregroundColor(.orange)
                    }

                    HStack {
                        Text("Total Spent")
                        Spacer()
                        Text(viewModel.totalSpent, format: .currency(code: "USD"))
                            .foregroundColor(.red)
                    }
                }
                .padding(.vertical, 8)
            }

            Section(header: Text("Budget Envelopes")) {
                if viewModel.budgets.isEmpty {
                    Text("No budgets yet. Tap + to create one.")
                        .foregroundColor(.secondary)
                } else {
                    ForEach(viewModel.budgets) { budget in
                        NavigationLink(
                            destination: BudgetDetailView(
                                budget: budget,
                                budgetInteractor: budgetInteractor,
                                expenseInteractor: expenseInteractor
                            )
                        ) {
                            BudgetRowView(budget: budget)
                        }
                    }
                    .onDelete { indexSet in
                        Task {
                            await viewModel.deleteBudget(at: indexSet)
                        }
                    }
                }
            }
        }
        .navigationTitle(viewModel.pocket.name)
        .toolbar {
            ToolbarItemGroup(placement: .navigationBarTrailing) {
                Button(action: { isShowingAddFunds = true }) {
                    Image(systemName: "dollarsign.circle")
                }
                Button(action: { isShowingAddBudget = true }) {
                    Image(systemName: "plus")
                }
            }
        }
        .sheet(isPresented: $isShowingAddBudget) {
            AddBudgetView(
                budgetInteractor: budgetInteractor,
                pocketInteractor: pocketInteractor,
                preselectedPocket: viewModel.pocket
            ) {
                Task {
                    await viewModel.loadBudgets()
                    await viewModel.refreshPocket()
                }
            }
        }
        .alert("Add Funds", isPresented: $isShowingAddFunds) {
            TextField("Amount", text: $addFundsAmount)
                .keyboardType(.decimalPad)
            Button("Cancel", role: .cancel) {
                addFundsAmount = ""
            }
            Button("Add") {
                Task {
                    if let amount = Double(addFundsAmount), amount > 0 {
                        _ = await viewModel.addFunds(amount: amount)
                    }
                    addFundsAmount = ""
                }
            }
        } message: {
            Text("Enter the amount to add to this pocket")
        }
        .task {
            await viewModel.loadBudgets()
        }
        .refreshable {
            await viewModel.loadBudgets()
            await viewModel.refreshPocket()
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

struct BudgetRowView: View {
    let budget: Budget

    var progressColor: Color {
        let percentage = budget.allocatedAmount > 0 ? budget.spentAmount / budget.allocatedAmount : 0
        if percentage >= 1.0 {
            return .red
        } else if percentage >= 0.8 {
            return .orange
        }
        return .green
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(budget.name)
                    .font(.headline)
                Spacer()
                Text(budget.remainingAmount, format: .currency(code: "USD"))
                    .font(.subheadline)
                    .foregroundColor(budget.remainingAmount >= 0 ? .green : .red)
            }

            ProgressView(value: min(budget.spentAmount, budget.allocatedAmount), total: max(budget.allocatedAmount, 1))
                .tint(progressColor)

            HStack {
                Text("Spent: \(budget.spentAmount, format: .currency(code: "USD"))")
                    .font(.caption)
                    .foregroundColor(.secondary)
                Spacer()
                Text("of \(budget.allocatedAmount, format: .currency(code: "USD"))")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 4)
    }
}
