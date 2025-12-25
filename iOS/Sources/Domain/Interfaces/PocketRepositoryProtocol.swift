import Foundation

protocol PocketRepositoryProtocol {
    func fetchAll() async throws -> [Pocket]
    func fetch(id: Int64) async throws -> Pocket
    func add(_ pocket: Pocket) async throws -> Pocket
    func update(_ pocket: Pocket) async throws
    func delete(id: Int64) async throws
    func updateBalance(id: Int64, amount: Double) async throws
}
