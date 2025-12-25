package main

import (
	"log"
	"net/http"
	"os"

	"github.com/suprie/budget-manager/internal/handler"
	"github.com/suprie/budget-manager/internal/middleware"
	"github.com/suprie/budget-manager/internal/repository"
	"github.com/suprie/budget-manager/internal/service"
	"github.com/suprie/budget-manager/pkg/database"
)

func main() {
	// Get database path from env or use default
	dbPath := os.Getenv("DB_PATH")
	if dbPath == "" {
		dbPath = "./budget.db"
	}

	// Get port from env or use default
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	// Get JWT secret from env or use default (change in production!)
	jwtSecret := os.Getenv("JWT_SECRET")
	if jwtSecret == "" {
		jwtSecret = "your-secret-key-change-in-production"
		log.Println("Warning: Using default JWT secret. Set JWT_SECRET environment variable in production.")
	}

	// Initialize database
	db, err := database.NewSQLiteDB(database.Config{Path: dbPath})
	if err != nil {
		log.Fatalf("Failed to initialize database: %v", err)
	}
	defer db.Close()

	// Initialize repositories
	userRepo := repository.NewUserRepository(db)
	pocketRepo := repository.NewPocketRepository(db)
	budgetRepo := repository.NewBudgetRepository(db)
	expenseRepo := repository.NewExpenseRepository(db)
	budgetRuleRepo := repository.NewBudgetRuleRepository(db)

	// Initialize services
	authService := service.NewAuthService(userRepo, jwtSecret)
	pocketService := service.NewPocketService(pocketRepo)
	budgetService := service.NewBudgetService(budgetRepo, pocketRepo)
	expenseService := service.NewExpenseService(expenseRepo, budgetRepo)
	budgetRuleService := service.NewBudgetRuleService(budgetRuleRepo, budgetRepo)

	// Initialize middleware
	authMiddleware := middleware.NewAuthMiddleware(authService)

	// Initialize handlers
	authHandler := handler.NewAuthHandler(authService)
	pocketHandler := handler.NewPocketHandler(pocketService)
	budgetHandler := handler.NewBudgetHandler(budgetService)
	expenseHandler := handler.NewExpenseHandler(expenseService)
	budgetRuleHandler := handler.NewBudgetRuleHandler(budgetRuleService)

	// Setup routes
	mux := http.NewServeMux()

	// Auth routes (public)
	mux.HandleFunc("POST /api/auth/register", authHandler.Register)
	mux.HandleFunc("POST /api/auth/login", authHandler.Login)

	// Protected routes mux
	protectedMux := http.NewServeMux()

	// Auth routes (protected)
	protectedMux.HandleFunc("GET /api/auth/me", authHandler.Me)

	// Pocket routes
	protectedMux.HandleFunc("POST /api/pockets", pocketHandler.Create)
	protectedMux.HandleFunc("GET /api/pockets", pocketHandler.GetAll)
	protectedMux.HandleFunc("GET /api/pockets/{id}", pocketHandler.GetByID)
	protectedMux.HandleFunc("PUT /api/pockets/{id}", pocketHandler.Update)
	protectedMux.HandleFunc("DELETE /api/pockets/{id}", pocketHandler.Delete)
	protectedMux.HandleFunc("POST /api/pockets/{id}/add-funds", pocketHandler.AddFunds)

	// Budget routes
	protectedMux.HandleFunc("POST /api/budgets", budgetHandler.Create)
	protectedMux.HandleFunc("GET /api/budgets", budgetHandler.GetAll)
	protectedMux.HandleFunc("GET /api/budgets/{id}", budgetHandler.GetByID)
	protectedMux.HandleFunc("PUT /api/budgets/{id}", budgetHandler.Update)
	protectedMux.HandleFunc("DELETE /api/budgets/{id}", budgetHandler.Delete)
	protectedMux.HandleFunc("GET /api/budgets/{id}/remaining", budgetHandler.GetRemaining)
	protectedMux.HandleFunc("GET /api/budgets/by-period", budgetHandler.GetByPeriod)
	protectedMux.HandleFunc("GET /api/budgets/summary", budgetHandler.GetSummary)
	protectedMux.HandleFunc("GET /api/pockets/{pocket_id}/budgets", budgetHandler.GetByPocketID)

	// Expense routes
	protectedMux.HandleFunc("POST /api/expenses", expenseHandler.Create)
	protectedMux.HandleFunc("GET /api/expenses", expenseHandler.GetAll)
	protectedMux.HandleFunc("GET /api/expenses/{id}", expenseHandler.GetByID)
	protectedMux.HandleFunc("PUT /api/expenses/{id}", expenseHandler.Update)
	protectedMux.HandleFunc("DELETE /api/expenses/{id}", expenseHandler.Delete)
	protectedMux.HandleFunc("GET /api/expenses/by-date-range", expenseHandler.GetByDateRange)
	protectedMux.HandleFunc("GET /api/budgets/{budget_id}/expenses", expenseHandler.GetByBudgetID)

	// Budget rule routes
	protectedMux.HandleFunc("POST /api/budget-rules", budgetRuleHandler.Create)
	protectedMux.HandleFunc("GET /api/budget-rules", budgetRuleHandler.GetAll)
	protectedMux.HandleFunc("GET /api/budget-rules/{id}", budgetRuleHandler.GetByID)
	protectedMux.HandleFunc("PUT /api/budget-rules/{id}", budgetRuleHandler.Update)
	protectedMux.HandleFunc("DELETE /api/budget-rules/{id}", budgetRuleHandler.Delete)
	protectedMux.HandleFunc("GET /api/budget-rules/match", budgetRuleHandler.MatchTransaction)
	protectedMux.HandleFunc("GET /api/budgets/{budget_id}/rules", budgetRuleHandler.GetByBudgetID)

	// Apply auth middleware to protected routes
	mux.Handle("/api/", authMiddleware.Authenticate(protectedMux))

	// Health check (public)
	mux.HandleFunc("GET /health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte(`{"status":"ok"}`))
	})

	log.Printf("Server starting on port %s", port)
	if err := http.ListenAndServe(":"+port, mux); err != nil {
		log.Fatalf("Server failed to start: %v", err)
	}
}
