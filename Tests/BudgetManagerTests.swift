import XCTest

// Mocks or direct usage of InMemory repos since they are basically mocks already
class BudgetManagerTests: XCTestCase {
    
    var budgetInteractor: BudgetInteractor!
    var expenseInteractor: ExpenseInteractor!
    var budgetRepo: InMemoryBudgetRepository!
    var expenseRepo: InMemoryExpenseRepository!
    
    override func setUp() async throws {
        budgetRepo = InMemoryBudgetRepository()
        expenseRepo = InMemoryExpenseRepository()
        budgetInteractor = BudgetInteractor(repository: budgetRepo)
        expenseInteractor = ExpenseInteractor(repository: expenseRepo)
    }
    
    func testCreateBudget() async throws {
        try await budgetInteractor.createBudget(name: "Test Budget", limit: 100.0)
        let budgets = try await budgetInteractor.getBudgets()
        XCTAssertEqual(budgets.count, 1)
        XCTAssertEqual(budgets.first?.name, "Test Budget")
        XCTAssertEqual(budgets.first?.limit, 100.0)
    }
    
    func testAddExpense() async throws {
        try await budgetInteractor.createBudget(name: "Test Budget", limit: 100.0)
        let budgets = try await budgetInteractor.getBudgets()
        let budget = budgets.first!
        
        try await expenseInteractor.addExpense(budgetId: budget.id, amount: 20.0, description: "Lunch")
        
        let expenses = try await expenseInteractor.getExpenses(for: budget.id)
        XCTAssertEqual(expenses.count, 1)
        XCTAssertEqual(expenses.first?.amount, 20.0)
        XCTAssertEqual(expenses.first?.description, "Lunch")
    }
    
    func testBudgetRemainingCalculation() async throws {
        // This logic is in the ViewModel, but we can test the underlying data integrity here
        try await budgetInteractor.createBudget(name: "Test Budget", limit: 100.0)
        let budgets = try await budgetInteractor.getBudgets()
        let budget = budgets.first!
        
        try await expenseInteractor.addExpense(budgetId: budget.id, amount: 20.0, description: "Lunch")
        try await expenseInteractor.addExpense(budgetId: budget.id, amount: 30.0, description: "Dinner")
        
        let expenses = try await expenseInteractor.getExpenses(for: budget.id)
        let totalSpent = expenses.reduce(0) { $0 + $1.amount }
        
        XCTAssertEqual(totalSpent, 50.0)
        XCTAssertEqual(budget.limit - totalSpent, 50.0)
    }
}
