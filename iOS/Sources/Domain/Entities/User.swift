import Foundation

struct User: Identifiable, Codable, Hashable {
    let id: Int64
    var email: String
    var name: String
    var createdAt: Date
    var updatedAt: Date

    init(
        id: Int64 = 0,
        email: String,
        name: String,
        createdAt: Date = Date(),
        updatedAt: Date = Date()
    ) {
        self.id = id
        self.email = email
        self.name = name
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
}

struct AuthResponse {
    let token: String
    let user: User
}
