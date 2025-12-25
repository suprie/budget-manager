import Foundation

enum HTTPMethod: String {
    case get = "GET"
    case post = "POST"
    case put = "PUT"
    case delete = "DELETE"
}

struct APIEndpoint {
    let path: String
    let method: HTTPMethod
    let queryItems: [URLQueryItem]?
    let body: Data?

    init(
        path: String,
        method: HTTPMethod = .get,
        queryItems: [URLQueryItem]? = nil,
        body: Data? = nil
    ) {
        self.path = path
        self.method = method
        self.queryItems = queryItems
        self.body = body
    }
}

extension APIEndpoint {
    // MARK: - Expenses

    static func createExpense(budgetId: Int64, amount: Double, description: String, date: Date) -> APIEndpoint {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"

        let body: [String: Any] = [
            "budget_id": budgetId,
            "amount": amount,
            "description": description,
            "date": formatter.string(from: date)
        ]

        return APIEndpoint(
            path: "/api/expenses",
            method: .post,
            body: try? JSONSerialization.data(withJSONObject: body)
        )
    }

    static var getAllExpenses: APIEndpoint {
        APIEndpoint(path: "/api/expenses")
    }

    static func getExpense(id: Int64) -> APIEndpoint {
        APIEndpoint(path: "/api/expenses/\(id)")
    }

    static func updateExpense(id: Int64, budgetId: Int64?, amount: Double?, description: String?, date: Date?) -> APIEndpoint {
        var body: [String: Any] = [:]

        if let budgetId = budgetId {
            body["budget_id"] = budgetId
        }
        if let amount = amount {
            body["amount"] = amount
        }
        if let description = description {
            body["description"] = description
        }
        if let date = date {
            let formatter = DateFormatter()
            formatter.dateFormat = "yyyy-MM-dd"
            body["date"] = formatter.string(from: date)
        }

        return APIEndpoint(
            path: "/api/expenses/\(id)",
            method: .put,
            body: try? JSONSerialization.data(withJSONObject: body)
        )
    }

    static func deleteExpense(id: Int64) -> APIEndpoint {
        APIEndpoint(path: "/api/expenses/\(id)", method: .delete)
    }

    static func getExpensesByBudget(budgetId: Int64) -> APIEndpoint {
        APIEndpoint(path: "/api/budgets/\(budgetId)/expenses")
    }

    static func getExpensesByDateRange(startDate: Date, endDate: Date) -> APIEndpoint {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"

        return APIEndpoint(
            path: "/api/expenses/by-date-range",
            queryItems: [
                URLQueryItem(name: "start_date", value: formatter.string(from: startDate)),
                URLQueryItem(name: "end_date", value: formatter.string(from: endDate))
            ]
        )
    }

    // MARK: - Budgets

    static var getAllBudgets: APIEndpoint {
        APIEndpoint(path: "/api/budgets")
    }

    static func getBudget(id: Int64) -> APIEndpoint {
        APIEndpoint(path: "/api/budgets/\(id)")
    }

    static func createBudget(name: String, description: String, pocketId: Int64, allocatedAmount: Double, period: String) -> APIEndpoint {
        let body: [String: Any] = [
            "name": name,
            "description": description,
            "pocket_id": pocketId,
            "allocated_amount": allocatedAmount,
            "period": period
        ]

        return APIEndpoint(
            path: "/api/budgets",
            method: .post,
            body: try? JSONSerialization.data(withJSONObject: body)
        )
    }

    static func updateBudget(id: Int64, name: String?, description: String?, allocatedAmount: Double?) -> APIEndpoint {
        var body: [String: Any] = [:]

        if let name = name {
            body["name"] = name
        }
        if let description = description {
            body["description"] = description
        }
        if let allocatedAmount = allocatedAmount {
            body["allocated_amount"] = allocatedAmount
        }

        return APIEndpoint(
            path: "/api/budgets/\(id)",
            method: .put,
            body: try? JSONSerialization.data(withJSONObject: body)
        )
    }

    static func deleteBudget(id: Int64) -> APIEndpoint {
        APIEndpoint(path: "/api/budgets/\(id)", method: .delete)
    }

    // MARK: - Pockets

    static var getAllPockets: APIEndpoint {
        APIEndpoint(path: "/api/pockets")
    }

    static func getPocket(id: Int64) -> APIEndpoint {
        APIEndpoint(path: "/api/pockets/\(id)")
    }

    static func createPocket(name: String, description: String, balance: Double) -> APIEndpoint {
        let body: [String: Any] = [
            "name": name,
            "description": description,
            "balance": balance
        ]

        return APIEndpoint(
            path: "/api/pockets",
            method: .post,
            body: try? JSONSerialization.data(withJSONObject: body)
        )
    }

    static func updatePocket(id: Int64, name: String?, description: String?, balance: Double?) -> APIEndpoint {
        var body: [String: Any] = [:]

        if let name = name {
            body["name"] = name
        }
        if let description = description {
            body["description"] = description
        }
        if let balance = balance {
            body["balance"] = balance
        }

        return APIEndpoint(
            path: "/api/pockets/\(id)",
            method: .put,
            body: try? JSONSerialization.data(withJSONObject: body)
        )
    }

    static func deletePocket(id: Int64) -> APIEndpoint {
        APIEndpoint(path: "/api/pockets/\(id)", method: .delete)
    }

    // MARK: - Auth

    static func register(email: String, password: String, name: String) -> APIEndpoint {
        let body: [String: Any] = [
            "email": email,
            "password": password,
            "name": name
        ]

        return APIEndpoint(
            path: "/api/auth/register",
            method: .post,
            body: try? JSONSerialization.data(withJSONObject: body)
        )
    }

    static func login(email: String, password: String) -> APIEndpoint {
        let body: [String: Any] = [
            "email": email,
            "password": password
        ]

        return APIEndpoint(
            path: "/api/auth/login",
            method: .post,
            body: try? JSONSerialization.data(withJSONObject: body)
        )
    }
}
