package domain

import (
	"time"
)

// Expense represents a spending transaction against a budget envelope
type Expense struct {
	ID          int64     `json:"id"`
	BudgetID    int64     `json:"budget_id"`
	Amount      float64   `json:"amount"`
	Description string    `json:"description"`
	Date        time.Time `json:"date"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
}

type CreateExpenseRequest struct {
	BudgetID    int64   `json:"budget_id"`
	Amount      float64 `json:"amount"`
	Description string  `json:"description"`
	Date        string  `json:"date"` // Format: "2006-01-02"
}

type UpdateExpenseRequest struct {
	BudgetID    *int64   `json:"budget_id,omitempty"`
	Amount      *float64 `json:"amount,omitempty"`
	Description *string  `json:"description,omitempty"`
	Date        *string  `json:"date,omitempty"`
}

// ExpenseFilter for querying expenses
type ExpenseFilter struct {
	BudgetID  *int64
	StartDate *time.Time
	EndDate   *time.Time
}
