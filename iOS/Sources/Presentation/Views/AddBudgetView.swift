import SwiftUI

struct AddBudgetView: View {
    @Environment(\.dismiss) var dismiss
    @StateObject private var viewModel: AddBudgetViewModel
    var onSave: () -> Void

    init(
        budgetInteractor: BudgetInteractor,
        pocketInteractor: PocketInteractor,
        preselectedPocket: Pocket?,
        onSave: @escaping () -> Void
    ) {
        self.onSave = onSave
        let vm = AddBudgetViewModel(budgetInteractor: budgetInteractor, pocketInteractor: pocketInteractor)
        if let pocket = preselectedPocket {
            vm.selectedPocket = pocket
        }
        _viewModel = StateObject(wrappedValue: vm)
    }

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Budget Details")) {
                    TextField("Name", text: $viewModel.name)
                    TextField("Description (optional)", text: $viewModel.description)
                    TextField("Amount", text: $viewModel.allocatedAmount)
                        .keyboardType(.decimalPad)
                }

                Section(header: Text("Source Pocket")) {
                    if viewModel.pockets.isEmpty {
                        Text("No pockets available. Create a pocket first.")
                            .foregroundColor(.secondary)
                    } else {
                        Picker("Pocket", selection: $viewModel.selectedPocket) {
                            ForEach(viewModel.pockets) { pocket in
                                HStack {
                                    Text(pocket.name)
                                    Spacer()
                                    Text(pocket.balance, format: .currency(code: "USD"))
                                        .foregroundColor(.secondary)
                                }
                                .tag(pocket as Pocket?)
                            }
                        }
                        .pickerStyle(.inline)

                        if let pocket = viewModel.selectedPocket {
                            HStack {
                                Text("Available")
                                Spacer()
                                Text(pocket.balance, format: .currency(code: "USD"))
                                    .foregroundColor(.green)
                            }
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
                    .disabled(viewModel.pockets.isEmpty)
                }
            }
            .task {
                await viewModel.loadPockets()
            }
        }
    }
}
