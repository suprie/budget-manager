import Foundation

actor InMemoryAuthRepository: AuthRepositoryProtocol {
    private var users: [String: (User, String)] = [:] // email -> (user, passwordHash)
    private var storedToken: String?
    private var currentUser: User?
    private var nextId: Int64 = 1

    func register(email: String, password: String, name: String) async throws -> AuthResponse {
        if users[email] != nil {
            throw DomainError.emailAlreadyExists
        }

        let user = User(
            id: nextId,
            email: email,
            name: name,
            createdAt: Date(),
            updatedAt: Date()
        )
        nextId += 1

        users[email] = (user, String(password.hashValue))

        let token = "mock_token_\(user.id)_\(Int(Date().timeIntervalSince1970))"
        storedToken = token
        currentUser = user

        return AuthResponse(token: token, user: user)
    }

    func login(email: String, password: String) async throws -> AuthResponse {
        guard let (user, storedPasswordHash) = users[email] else {
            throw DomainError.invalidCredentials
        }

        if storedPasswordHash != String(password.hashValue) {
            throw DomainError.invalidCredentials
        }

        let token = "mock_token_\(user.id)_\(Int(Date().timeIntervalSince1970))"
        storedToken = token
        currentUser = user

        return AuthResponse(token: token, user: user)
    }

    func getStoredToken() async -> String? {
        return storedToken
    }

    func saveToken(_ token: String) async {
        storedToken = token
    }

    func clearToken() async {
        storedToken = nil
    }

    func getCurrentUser() async -> User? {
        return currentUser
    }

    func saveCurrentUser(_ user: User) async {
        currentUser = user
    }

    func clearCurrentUser() async {
        currentUser = nil
    }
}
