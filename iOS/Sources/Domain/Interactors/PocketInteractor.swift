import Foundation

class PocketInteractor {
    private let pocketRepository: PocketRepositoryProtocol
    private let budgetRepository: InMemoryBudgetRepository

    init(pocketRepository: PocketRepositoryProtocol, budgetRepository: InMemoryBudgetRepository) {
        self.pocketRepository = pocketRepository
        self.budgetRepository = budgetRepository
    }

    func getPockets() async throws -> [Pocket] {
        return try await pocketRepository.fetchAll()
    }

    func getPocket(id: Int64) async throws -> Pocket {
        return try await pocketRepository.fetch(id: id)
    }

    func createPocket(name: String, description: String = "", balance: Double = 0) async throws -> Pocket {
        guard !name.isEmpty else {
            throw DomainError.invalidInput("Name cannot be empty")
        }

        let pocket = Pocket(name: name, description: description, balance: balance)
        return try await pocketRepository.add(pocket)
    }

    func updatePocket(id: Int64, name: String?, description: String?, balance: Double?) async throws -> Pocket {
        var pocket = try await pocketRepository.fetch(id: id)

        if let name = name {
            pocket.name = name
        }
        if let description = description {
            pocket.description = description
        }
        if let balance = balance {
            pocket.balance = balance
        }

        try await pocketRepository.update(pocket)
        return pocket
    }

    func deletePocket(id: Int64) async throws {
        if budgetRepository.hasBudgets(forPocketId: id) {
            throw DomainError.pocketHasBudgets
        }
        try await pocketRepository.delete(id: id)
    }

    func addFunds(id: Int64, amount: Double) async throws -> Pocket {
        guard amount > 0 else {
            throw DomainError.invalidInput("Amount must be greater than 0")
        }
        try await pocketRepository.updateBalance(id: id, amount: amount)
        return try await pocketRepository.fetch(id: id)
    }
}
