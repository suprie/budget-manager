import Foundation

/// Budget represents an envelope in the zero-sum budgeting system.
/// Money is allocated from a Pocket into Budget envelopes.
struct Budget: Identifiable, Codable {
    let id: Int64
    var name: String
    var description: String
    var pocketId: Int64
    var allocatedAmount: Double
    var spentAmount: Double
    var period: String
    var createdAt: Date
    var updatedAt: Date

    init(
        id: Int64 = 0,
        name: String,
        description: String = "",
        pocketId: Int64,
        allocatedAmount: Double,
        spentAmount: Double = 0,
        period: String,
        createdAt: Date = Date(),
        updatedAt: Date = Date()
    ) {
        self.id = id
        self.name = name
        self.description = description
        self.pocketId = pocketId
        self.allocatedAmount = allocatedAmount
        self.spentAmount = spentAmount
        self.period = period
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }

    var remainingAmount: Double {
        allocatedAmount - spentAmount
    }
}

enum BudgetCategory: String, Codable, CaseIterable, Identifiable {
    case food = "Food"
    case transport = "Transport"
    case utilities = "Utilities"
    case entertainment = "Entertainment"
    case health = "Health"
    case shopping = "Shopping"
    case other = "Other"

    var id: String { rawValue }

    var icon: String {
        switch self {
        case .food: return "cart"
        case .transport: return "car"
        case .utilities: return "bolt"
        case .entertainment: return "tv"
        case .health: return "heart"
        case .shopping: return "bag"
        case .other: return "star"
        }
    }
}

struct BudgetSummary {
    let period: String
    var totalAllocated: Double
    var totalSpent: Double
    var totalRemaining: Double
    var unallocatedFunds: Double
}
