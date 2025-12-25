import Foundation

/// Expense represents a spending transaction against a budget envelope
struct Expense: Identifiable, Codable {
    let id: Int64
    var budgetId: Int64
    var amount: Double
    var description: String
    var date: Date
    var createdAt: Date
    var updatedAt: Date

    init(
        id: Int64 = 0,
        budgetId: Int64,
        amount: Double,
        description: String,
        date: Date = Date(),
        createdAt: Date = Date(),
        updatedAt: Date = Date()
    ) {
        self.id = id
        self.budgetId = budgetId
        self.amount = amount
        self.description = description
        self.date = date
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
}
