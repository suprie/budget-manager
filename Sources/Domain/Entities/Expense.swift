import Foundation

struct Expense: Identifiable, Codable {
    let id: UUID
    let budgetId: UUID
    var amount: Double
    var description: String
    var date: Date
    
    init(id: UUID = UUID(), budgetId: UUID, amount: Double, description: String, date: Date = Date()) {
        self.id = id
        self.budgetId = budgetId
        self.amount = amount
        self.description = description
        self.date = date
    }
}
