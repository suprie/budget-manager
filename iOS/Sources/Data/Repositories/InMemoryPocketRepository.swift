import Foundation

class InMemoryPocketRepository: PocketRepositoryProtocol {
    private var pockets: [Pocket] = []
    private var nextId: Int64 = 1

    func fetchAll() async throws -> [Pocket] {
        return pockets.sorted { $0.name < $1.name }
    }

    func fetch(id: Int64) async throws -> Pocket {
        guard let pocket = pockets.first(where: { $0.id == id }) else {
            throw DomainError.notFound
        }
        return pocket
    }

    func add(_ pocket: Pocket) async throws -> Pocket {
        let newPocket = Pocket(
            id: nextId,
            name: pocket.name,
            description: pocket.description,
            balance: pocket.balance,
            createdAt: pocket.createdAt,
            updatedAt: pocket.updatedAt
        )
        nextId += 1
        pockets.append(newPocket)
        return newPocket
    }

    func update(_ pocket: Pocket) async throws {
        guard let index = pockets.firstIndex(where: { $0.id == pocket.id }) else {
            throw DomainError.notFound
        }
        var updated = pocket
        updated.updatedAt = Date()
        pockets[index] = updated
    }

    func delete(id: Int64) async throws {
        guard pockets.contains(where: { $0.id == id }) else {
            throw DomainError.notFound
        }
        pockets.removeAll { $0.id == id }
    }

    func updateBalance(id: Int64, amount: Double) async throws {
        guard let index = pockets.firstIndex(where: { $0.id == id }) else {
            throw DomainError.notFound
        }
        pockets[index].balance += amount
        pockets[index].updatedAt = Date()
    }
}
