import Foundation

@MainActor
class AuthInteractor: ObservableObject {
    private let authRepository: AuthRepositoryProtocol

    @Published private(set) var isLoggedIn: Bool = false
    @Published private(set) var currentUser: User?

    init(authRepository: AuthRepositoryProtocol) {
        self.authRepository = authRepository
        Task {
            await checkLoginStatus()
        }
    }

    func checkLoginStatus() async {
        let token = await authRepository.getStoredToken()
        isLoggedIn = token != nil
        if isLoggedIn {
            currentUser = await authRepository.getCurrentUser()
        }
    }

    func register(email: String, password: String, name: String) async throws -> AuthResponse {
        guard !email.trimmingCharacters(in: .whitespaces).isEmpty else {
            throw DomainError.invalidInput("Email is required")
        }

        guard email.contains("@") else {
            throw DomainError.invalidInput("Invalid email format")
        }

        guard password.count >= 6 else {
            throw DomainError.invalidInput("Password must be at least 6 characters")
        }

        guard !name.trimmingCharacters(in: .whitespaces).isEmpty else {
            throw DomainError.invalidInput("Name is required")
        }

        let response = try await authRepository.register(
            email: email.trimmingCharacters(in: .whitespaces),
            password: password,
            name: name.trimmingCharacters(in: .whitespaces)
        )

        isLoggedIn = true
        currentUser = response.user

        return response
    }

    func login(email: String, password: String) async throws -> AuthResponse {
        guard !email.trimmingCharacters(in: .whitespaces).isEmpty else {
            throw DomainError.invalidInput("Email is required")
        }

        guard !password.isEmpty else {
            throw DomainError.invalidInput("Password is required")
        }

        let response = try await authRepository.login(
            email: email.trimmingCharacters(in: .whitespaces),
            password: password
        )

        isLoggedIn = true
        currentUser = response.user

        return response
    }

    func logout() async {
        await authRepository.clearToken()
        await authRepository.clearCurrentUser()
        isLoggedIn = false
        currentUser = nil
    }
}
