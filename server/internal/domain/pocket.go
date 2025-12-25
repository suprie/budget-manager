package domain

import (
	"time"
)

// Pocket represents a source of money (e.g., bank account, cash, e-wallet)
type Pocket struct {
	ID          int64     `json:"id"`
	Name        string    `json:"name"`
	Description string    `json:"description,omitempty"`
	Balance     float64   `json:"balance"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
}

type CreatePocketRequest struct {
	Name        string  `json:"name"`
	Description string  `json:"description,omitempty"`
	Balance     float64 `json:"balance"`
}

type UpdatePocketRequest struct {
	Name        *string  `json:"name,omitempty"`
	Description *string  `json:"description,omitempty"`
	Balance     *float64 `json:"balance,omitempty"`
}
