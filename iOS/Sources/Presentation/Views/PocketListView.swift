import SwiftUI

struct PocketListView: View {
    @StateObject private var viewModel: PocketListViewModel
    @State private var isShowingAddPocket = false

    let pocketInteractor: PocketInteractor
    let budgetInteractor: BudgetInteractor
    let expenseInteractor: ExpenseInteractor

    init(
        pocketInteractor: PocketInteractor,
        budgetInteractor: BudgetInteractor,
        expenseInteractor: ExpenseInteractor
    ) {
        self.pocketInteractor = pocketInteractor
        self.budgetInteractor = budgetInteractor
        self.expenseInteractor = expenseInteractor
        _viewModel = StateObject(wrappedValue: PocketListViewModel(pocketInteractor: pocketInteractor))
    }

    var body: some View {
        NavigationView {
            List {
                Section {
                    HStack {
                        Text("Total Balance")
                            .font(.headline)
                        Spacer()
                        Text(viewModel.totalBalance, format: .currency(code: "USD"))
                            .font(.title2)
                            .bold()
                            .foregroundColor(.green)
                    }
                    .padding(.vertical, 8)
                }

                Section(header: Text("Pockets")) {
                    ForEach(viewModel.pockets) { pocket in
                        NavigationLink(
                            destination: PocketDetailView(
                                pocket: pocket,
                                pocketInteractor: pocketInteractor,
                                budgetInteractor: budgetInteractor,
                                expenseInteractor: expenseInteractor
                            )
                        ) {
                            PocketRowView(pocket: pocket)
                        }
                    }
                    .onDelete { indexSet in
                        Task {
                            await viewModel.deletePocket(at: indexSet)
                        }
                    }
                }
            }
            .navigationTitle("Pockets")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { isShowingAddPocket = true }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $isShowingAddPocket) {
                AddPocketView(pocketInteractor: pocketInteractor) {
                    Task {
                        await viewModel.loadPockets()
                    }
                }
            }
            .task {
                await viewModel.loadPockets()
            }
            .refreshable {
                await viewModel.loadPockets()
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

struct PocketRowView: View {
    let pocket: Pocket

    var body: some View {
        HStack {
            Image(systemName: "wallet.pass")
                .foregroundColor(.blue)
                .frame(width: 30)

            VStack(alignment: .leading, spacing: 4) {
                Text(pocket.name)
                    .font(.headline)
                if !pocket.description.isEmpty {
                    Text(pocket.description)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }

            Spacer()

            Text(pocket.balance, format: .currency(code: "USD"))
                .font(.subheadline)
                .foregroundColor(pocket.balance >= 0 ? .primary : .red)
        }
        .padding(.vertical, 4)
    }
}
