import SwiftUI

struct BudgetListView: View {
    @StateObject private var viewModel: BudgetListViewModel
    @State private var isShowingAddBudget = false
    
    // Dependency Injection via closure or factory would be better for scalability,
    // but for simplicity we'll pass the interactor to the VM here.
    // In a real app, we might use a DI container or Coordinator.
    let budgetInteractor: BudgetInteractor
    let expenseInteractor: ExpenseInteractor
    
    init(budgetInteractor: BudgetInteractor, expenseInteractor: ExpenseInteractor) {
        self.budgetInteractor = budgetInteractor
        self.expenseInteractor = expenseInteractor
        _viewModel = StateObject(wrappedValue: BudgetListViewModel(budgetInteractor: budgetInteractor))
    }
    
    var body: some View {
        NavigationView {
            List {
                ForEach(viewModel.budgets) { budget in
                    NavigationLink(destination: BudgetDetailView(budget: budget, expenseInteractor: expenseInteractor)) {
                        HStack {
                            Image(systemName: budget.category.icon)
                                .foregroundColor(.blue)
                                .frame(width: 30)
                            
                            VStack(alignment: .leading) {
                                Text(budget.name)
                                    .font(.headline)
                                Text("Limit: \(budget.limit, format: .currency(code: "USD"))")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                }
                .onDelete { indexSet in
                    Task {
                        await viewModel.deleteBudget(at: indexSet)
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
                AddBudgetView(budgetInteractor: budgetInteractor) {
                    Task {
                        await viewModel.loadBudgets()
                    }
                }
            }
            .task {
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
