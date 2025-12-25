import SwiftUI

struct AddPocketView: View {
    @Environment(\.dismiss) var dismiss
    @StateObject private var viewModel: AddPocketViewModel
    var onSave: () -> Void

    init(pocketInteractor: PocketInteractor, onSave: @escaping () -> Void) {
        self.onSave = onSave
        _viewModel = StateObject(wrappedValue: AddPocketViewModel(pocketInteractor: pocketInteractor))
    }

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Pocket Details")) {
                    TextField("Name", text: $viewModel.name)
                    TextField("Description (optional)", text: $viewModel.description)
                    TextField("Initial Balance", text: $viewModel.balance)
                        .keyboardType(.decimalPad)
                }

                if let errorMessage = viewModel.errorMessage {
                    Section {
                        Text(errorMessage)
                            .foregroundColor(.red)
                    }
                }
            }
            .navigationTitle("Add Pocket")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        Task {
                            if await viewModel.addPocket() {
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
