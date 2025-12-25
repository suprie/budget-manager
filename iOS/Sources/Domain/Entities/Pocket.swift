import Foundation

/// Pocket represents a source of money (e.g., bank account, cash, e-wallet)
struct Pocket: Identifiable, Codable, Hashable {
    let id: Int64
    var name: String
    var description: String
    var balance: Double
    var createdAt: Date
    var updatedAt: Date

    init(
        id: Int64 = 0,
        name: String,
        description: String = "",
        balance: Double = 0,
        createdAt: Date = Date(),
        updatedAt: Date = Date()
    ) {
        self.id = id
        self.name = name
        self.description = description
        self.balance = balance
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
}
