package domain

import (
	"time"
)

// BudgetRule maps keywords to budget categories for auto-categorization
type BudgetRule struct {
	ID        int64     `json:"id"`
	BudgetID  int64     `json:"budget_id"`
	Keywords  string    `json:"keywords"` // Comma-separated keywords
	Priority  int       `json:"priority"` // Higher priority rules match first
	IsActive  bool      `json:"is_active"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

type CreateBudgetRuleRequest struct {
	BudgetID int64  `json:"budget_id"`
	Keywords string `json:"keywords"`
	Priority int    `json:"priority"`
}

type UpdateBudgetRuleRequest struct {
	Keywords *string `json:"keywords,omitempty"`
	Priority *int    `json:"priority,omitempty"`
	IsActive *bool   `json:"is_active,omitempty"`
}

// BudgetRuleWithBudget includes budget name for display
type BudgetRuleWithBudget struct {
	BudgetRule
	BudgetName string `json:"budget_name"`
}
