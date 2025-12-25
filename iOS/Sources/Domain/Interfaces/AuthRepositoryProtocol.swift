import Foundation

protocol AuthRepositoryProtocol {
    func register(email: String, password: String, name: String) async throws -> AuthResponse
    func login(email: String, password: String) async throws -> AuthResponse
    func getStoredToken() async -> String?
    func saveToken(_ token: String) async
    func clearToken() async
    func getCurrentUser() async -> User?
    func saveCurrentUser(_ user: User) async
    func clearCurrentUser() async
}
