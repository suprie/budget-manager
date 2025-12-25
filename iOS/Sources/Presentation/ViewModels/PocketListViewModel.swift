import Foundation
import Combine

@MainActor
class PocketListViewModel: ObservableObject {
    @Published var pockets: [Pocket] = []
    @Published var errorMessage: String?
    @Published var isLoading = false

    private let pocketInteractor: PocketInteractor

    init(pocketInteractor: PocketInteractor) {
        self.pocketInteractor = pocketInteractor
    }

    var totalBalance: Double {
        pockets.reduce(0) { $0 + $1.balance }
    }

    func loadPockets() async {
        isLoading = true
        do {
            pockets = try await pocketInteractor.getPockets()
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func deletePocket(at offsets: IndexSet) async {
        for index in offsets {
            let pocket = pockets[index]
            do {
                try await pocketInteractor.deletePocket(id: pocket.id)
                pockets.remove(at: index)
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }
}
