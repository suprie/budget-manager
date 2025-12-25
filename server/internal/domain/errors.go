package domain

import "errors"

var (
	ErrNotFound           = errors.New("resource not found")
	ErrInvalidInput       = errors.New("invalid input")
	ErrInsufficientFunds  = errors.New("insufficient funds in budget")
	ErrDuplicateEntry     = errors.New("duplicate entry")
	ErrPocketHasBudgets   = errors.New("pocket has associated budgets")
	ErrBudgetHasExpenses  = errors.New("budget has associated expenses")
	ErrEmailAlreadyExists = errors.New("email already exists")
	ErrInvalidCredentials = errors.New("invalid credentials")
	ErrUnauthorized       = errors.New("unauthorized")
	ErrInvalidToken       = errors.New("invalid or expired token")
)
