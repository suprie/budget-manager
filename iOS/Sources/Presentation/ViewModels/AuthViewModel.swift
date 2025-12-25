import Foundation

@MainActor
class AuthViewModel: ObservableObject {
    private let authInteractor: AuthInteractor

    @Published var isLoading = false
    @Published var errorMessage: String?

    var isLoggedIn: Bool {
        authInteractor.isLoggedIn
    }

    var currentUser: User? {
        authInteractor.currentUser
    }

    init(authInteractor: AuthInteractor) {
        self.authInteractor = authInteractor
    }

    func login(email: String, password: String) async {
        isLoading = true
        errorMessage = nil

        do {
            _ = try await authInteractor.login(email: email, password: password)
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func register(email: String, password: String, name: String) async {
        isLoading = true
        errorMessage = nil

        do {
            _ = try await authInteractor.register(email: email, password: password, name: name)
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func logout() async {
        await authInteractor.logout()
    }

    func clearError() {
        errorMessage = nil
    }
}
