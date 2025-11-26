import Foundation

struct Budget: Identifiable, Codable {
    let id: UUID
    var name: String
    var limit: Double
    var category: BudgetCategory
    var createdAt: Date
    
    init(id: UUID = UUID(), name: String, limit: Double, category: BudgetCategory = .other, createdAt: Date = Date()) {
        self.id = id
        self.name = name
        self.limit = limit
        self.category = category
        self.createdAt = createdAt
    }
}

enum BudgetCategory: String, Codable, CaseIterable, Identifiable {
    case food = "Food"
    case transport = "Transport"
    case utilities = "Utilities"
    case entertainment = "Entertainment"
    case health = "Health"
    case other = "Other"
    
    var id: String { rawValue }
    
    var icon: String {
        switch self {
        case .food: return "cart"
        case .transport: return "car"
        case .utilities: return "bolt"
        case .entertainment: return "tv"
        case .health: return "heart"
        case .other: return "star"
        }
    }
}
