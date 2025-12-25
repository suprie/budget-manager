import SwiftUI

struct BudgetListView: View {
    @StateObject private var viewModel: BudgetListViewModel
    @State private var isShowingAddBudget = false

    let budgetInteractor: BudgetInteractor
    let pocketInteractor: PocketInteractor
    let expenseInteractor: ExpenseInteractor

    init(
        budgetInteractor: BudgetInteractor,
        pocketInteractor: PocketInteractor,
        expenseInteractor: ExpenseInteractor
    ) {
        self.budgetInteractor = budgetInteractor
        self.pocketInteractor = pocketInteractor
        self.expenseInteractor = expenseInteractor
        _viewModel = StateObject(wrappedValue: BudgetListViewModel(budgetInteractor: budgetInteractor))
    }

    var body: some View {
        NavigationView {
            List {
                if let summary = viewModel.summary {
                    Section {
                        VStack(spacing: 12) {
                            HStack {
                                Text("Period")
                                Spacer()
                                Text(viewModel.currentPeriod)
                                    .font(.headline)
                            }

                            Divider()

                            HStack {
                                Text("Total Allocated")
                                Spacer()
                                Text(summary.totalAllocated, format: .currency(code: "USD"))
                            }

                            HStack {
                                Text("Total Spent")
                                Spacer()
                                Text(summary.totalSpent, format: .currency(code: "USD"))
                                    .foregroundColor(.orange)
                            }

                            HStack {
                                Text("Remaining")
                                Spacer()
                                Text(summary.totalRemaining, format: .currency(code: "USD"))
                                    .font(.headline)
                                    .foregroundColor(summary.totalRemaining >= 0 ? .green : .red)
                            }

                            Divider()

                            HStack {
                                Text("Unallocated Funds")
                                Spacer()
                                Text(summary.unallocatedFunds, format: .currency(code: "USD"))
                                    .foregroundColor(.blue)
                            }
                        }
                        .padding(.vertical, 8)
                    }
                }

                Section(header: Text("Budgets")) {
                    if viewModel.budgets.isEmpty {
                        Text("No budgets for this period")
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
            .navigationTitle("Budgets")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { isShowingAddBudget = true }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $isShowingAddBudget) {
                AddBudgetView(
                    budgetInteractor: budgetInteractor,
                    pocketInteractor: pocketInteractor,
                    preselectedPocket: nil
                ) {
                    Task {
                        await viewModel.loadBudgets()
                    }
                }
            }
            .task {
                await viewModel.loadBudgets()
            }
            .refreshable {
                await viewModel.loadBudgets()
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
}
