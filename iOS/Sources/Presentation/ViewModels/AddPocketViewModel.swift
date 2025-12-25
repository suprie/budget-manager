import Foundation
import Combine

@MainActor
class AddPocketViewModel: ObservableObject {
    @Published var name: String = ""
    @Published var description: String = ""
    @Published var balance: String = ""
    @Published var errorMessage: String?

    private let pocketInteractor: PocketInteractor

    init(pocketInteractor: PocketInteractor) {
        self.pocketInteractor = pocketInteractor
    }

    func addPocket() async -> Bool {
        guard !name.isEmpty else {
            errorMessage = "Name cannot be empty"
            return false
        }

        let balanceValue = Double(balance) ?? 0

        do {
            _ = try await pocketInteractor.createPocket(
                name: name,
                description: description,
                balance: balanceValue
            )
            return true
        } catch {
            errorMessage = error.localizedDescription
            return false
        }
    }
}
