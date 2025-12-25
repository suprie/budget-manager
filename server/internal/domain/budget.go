package domain

import (
	"time"
)

// Budget represents an envelope in the zero-sum budgeting system
// Money is allocated from a Pocket into Budget envelopes
type Budget struct {
	ID             int64     `json:"id"`
	Name           string    `json:"name"`
	Description    string    `json:"description,omitempty"`
	PocketID       int64     `json:"pocket_id"`
	AllocatedAmount float64   `json:"allocated_amount"` // Amount allocated to this envelope
	SpentAmount    float64   `json:"spent_amount"`      // Amount spent from this envelope
	Period         string    `json:"period"`            // e.g., "2024-01" for monthly budgets
	CreatedAt      time.Time `json:"created_at"`
	UpdatedAt      time.Time `json:"updated_at"`
}

// RemainingAmount returns the amount left in this budget envelope
func (b *Budget) RemainingAmount() float64 {
	return b.AllocatedAmount - b.SpentAmount
}

type CreateBudgetRequest struct {
	Name           string  `json:"name"`
	Description    string  `json:"description,omitempty"`
	PocketID       int64   `json:"pocket_id"`
	AllocatedAmount float64 `json:"allocated_amount"`
	Period         string  `json:"period"`
}

type UpdateBudgetRequest struct {
	Name           *string  `json:"name,omitempty"`
	Description    *string  `json:"description,omitempty"`
	AllocatedAmount *float64 `json:"allocated_amount,omitempty"`
}

// BudgetSummary provides an overview of budget allocations for a period
type BudgetSummary struct {
	Period           string  `json:"period"`
	TotalAllocated   float64 `json:"total_allocated"`
	TotalSpent       float64 `json:"total_spent"`
	TotalRemaining   float64 `json:"total_remaining"`
	UnallocatedFunds float64 `json:"unallocated_funds"` // For zero-sum: should be 0
}
