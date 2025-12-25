import Foundation

protocol BudgetRepositoryProtocol {
    func fetchAll() async throws -> [Budget]
    func fetch(id: Int64) async throws -> Budget
    func fetchByPocketId(_ pocketId: Int64) async throws -> [Budget]
    func fetchByPeriod(_ period: String) async throws -> [Budget]
    func add(_ budget: Budget) async throws -> Budget
    func update(_ budget: Budget) async throws
    func updateSpentAmount(id: Int64, amount: Double) async throws
    func delete(id: Int64) async throws
}
