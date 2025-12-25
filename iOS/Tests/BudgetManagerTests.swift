import XCTest
@testable import BudgetManager

final class BudgetManagerTests: XCTestCase {

    var pocketRepository: InMemoryPocketRepository!
    var budgetRepository: InMemoryBudgetRepository!
    var expenseRepository: InMemoryExpenseRepository!

    var pocketInteractor: PocketInteractor!
    var budgetInteractor: BudgetInteractor!
    var expenseInteractor: ExpenseInteractor!

    override func setUp() {
        super.setUp()
        pocketRepository = InMemoryPocketRepository()
        budgetRepository = InMemoryBudgetRepository()
        expenseRepository = InMemoryExpenseRepository()

        pocketInteractor = PocketInteractor(
            pocketRepository: pocketRepository,
            budgetRepository: budgetRepository
        )
        budgetInteractor = BudgetInteractor(
            budgetRepository: budgetRepository,
            pocketRepository: pocketRepository,
            expenseRepository: expenseRepository
        )
        expenseInteractor = ExpenseInteractor(
            expenseRepository: expenseRepository,
            budgetRepository: budgetRepository
        )
    }

    override func tearDown() {
        pocketRepository = nil
        budgetRepository = nil
        expenseRepository = nil
        pocketInteractor = nil
        budgetInteractor = nil
        expenseInteractor = nil
        super.tearDown()
    }

    // MARK: - Pocket Tests

    func testCreatePocket() async throws {
        let pocket = try await pocketInteractor.createPocket(
            name: "Bank Account",
            description: "Main checking",
            balance: 1000
        )

        XCTAssertEqual(pocket.name, "Bank Account")
        XCTAssertEqual(pocket.balance, 1000)
    }

    func testAddFundsToPocket() async throws {
        let pocket = try await pocketInteractor.createPocket(
            name: "Cash",
            balance: 100
        )

        let updated = try await pocketInteractor.addFunds(id: pocket.id, amount: 50)
        XCTAssertEqual(updated.balance, 150)
    }

    // MARK: - Budget Tests (Zero-Sum)

    func testCreateBudgetDeductsPocketBalance() async throws {
        let pocket = try await pocketInteractor.createPocket(
            name: "Bank",
            balance: 1000
        )

        let budget = try await budgetInteractor.createBudget(
            name: "Groceries",
            pocketId: pocket.id,
            allocatedAmount: 300,
            period: "2024-01"
        )

        XCTAssertEqual(budget.allocatedAmount, 300)
        XCTAssertEqual(budget.spentAmount, 0)

        let updatedPocket = try await pocketInteractor.getPocket(id: pocket.id)
        XCTAssertEqual(updatedPocket.balance, 700)
    }

    func testCannotCreateBudgetExceedingPocketBalance() async throws {
        let pocket = try await pocketInteractor.createPocket(
            name: "Bank",
            balance: 100
        )

        do {
            _ = try await budgetInteractor.createBudget(
                name: "Groceries",
                pocketId: pocket.id,
                allocatedAmount: 500,
                period: "2024-01"
            )
            XCTFail("Should throw insufficient funds error")
        } catch DomainError.insufficientFunds {
            // Expected
        }
    }

    func testDeleteBudgetReturnsFundsToPocket() async throws {
        let pocket = try await pocketInteractor.createPocket(
            name: "Bank",
            balance: 1000
        )

        let budget = try await budgetInteractor.createBudget(
            name: "Groceries",
            pocketId: pocket.id,
            allocatedAmount: 300,
            period: "2024-01"
        )

        try await budgetInteractor.deleteBudget(id: budget.id)

        let updatedPocket = try await pocketInteractor.getPocket(id: pocket.id)
        XCTAssertEqual(updatedPocket.balance, 1000)
    }

    // MARK: - Expense Tests

    func testCreateExpenseUpdatesSpentAmount() async throws {
        let pocket = try await pocketInteractor.createPocket(
            name: "Bank",
            balance: 1000
        )

        let budget = try await budgetInteractor.createBudget(
            name: "Groceries",
            pocketId: pocket.id,
            allocatedAmount: 300,
            period: "2024-01"
        )

        let expense = try await expenseInteractor.createExpense(
            budgetId: budget.id,
            amount: 50,
            description: "Weekly shopping"
        )

        XCTAssertEqual(expense.amount, 50)

        let updatedBudget = try await budgetInteractor.getBudget(id: budget.id)
        XCTAssertEqual(updatedBudget.spentAmount, 50)
        XCTAssertEqual(updatedBudget.remainingAmount, 250)
    }

    func testCannotCreateExpenseExceedingBudget() async throws {
        let pocket = try await pocketInteractor.createPocket(
            name: "Bank",
            balance: 1000
        )

        let budget = try await budgetInteractor.createBudget(
            name: "Groceries",
            pocketId: pocket.id,
            allocatedAmount: 100,
            period: "2024-01"
        )

        do {
            _ = try await expenseInteractor.createExpense(
                budgetId: budget.id,
                amount: 150,
                description: "Too expensive"
            )
            XCTFail("Should throw insufficient funds error")
        } catch DomainError.insufficientFunds {
            // Expected
        }
    }

    func testDeleteExpenseRestoresBudgetAmount() async throws {
        let pocket = try await pocketInteractor.createPocket(
            name: "Bank",
            balance: 1000
        )

        let budget = try await budgetInteractor.createBudget(
            name: "Groceries",
            pocketId: pocket.id,
            allocatedAmount: 300,
            period: "2024-01"
        )

        let expense = try await expenseInteractor.createExpense(
            budgetId: budget.id,
            amount: 50,
            description: "Shopping"
        )

        try await expenseInteractor.deleteExpense(id: expense.id)

        let updatedBudget = try await budgetInteractor.getBudget(id: budget.id)
        XCTAssertEqual(updatedBudget.spentAmount, 0)
        XCTAssertEqual(updatedBudget.remainingAmount, 300)
    }

    // MARK: - Summary Tests

    func testBudgetSummary() async throws {
        let pocket = try await pocketInteractor.createPocket(
            name: "Bank",
            balance: 1000
        )

        let period = "2024-01"

        _ = try await budgetInteractor.createBudget(
            name: "Groceries",
            pocketId: pocket.id,
            allocatedAmount: 300,
            period: period
        )

        _ = try await budgetInteractor.createBudget(
            name: "Transport",
            pocketId: pocket.id,
            allocatedAmount: 200,
            period: period
        )

        let summary = try await budgetInteractor.getSummary(forPeriod: period)

        XCTAssertEqual(summary.totalAllocated, 500)
        XCTAssertEqual(summary.totalSpent, 0)
        XCTAssertEqual(summary.totalRemaining, 500)
        XCTAssertEqual(summary.unallocatedFunds, 500) // 1000 - 500
    }
}
