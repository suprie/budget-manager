package handler

import (
	"encoding/json"
	"errors"
	"net/http"

	"github.com/suprie/budget-manager/internal/domain"
)

type ErrorResponse struct {
	Error   string `json:"error"`
	Message string `json:"message,omitempty"`
}

type SuccessResponse struct {
	Message string `json:"message"`
}

func writeJSON(w http.ResponseWriter, status int, data interface{}) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	json.NewEncoder(w).Encode(data)
}

func writeError(w http.ResponseWriter, err error) {
	var status int
	var message string

	switch {
	case errors.Is(err, domain.ErrNotFound):
		status = http.StatusNotFound
		message = "Resource not found"
	case errors.Is(err, domain.ErrInvalidInput):
		status = http.StatusBadRequest
		message = "Invalid input"
	case errors.Is(err, domain.ErrInsufficientFunds):
		status = http.StatusBadRequest
		message = "Insufficient funds"
	case errors.Is(err, domain.ErrPocketHasBudgets):
		status = http.StatusConflict
		message = "Cannot delete pocket with associated budgets"
	case errors.Is(err, domain.ErrBudgetHasExpenses):
		status = http.StatusConflict
		message = "Cannot delete budget with associated expenses"
	case errors.Is(err, domain.ErrEmailAlreadyExists):
		status = http.StatusConflict
		message = "Email already exists"
	case errors.Is(err, domain.ErrInvalidCredentials):
		status = http.StatusUnauthorized
		message = "Invalid email or password"
	case errors.Is(err, domain.ErrUnauthorized):
		status = http.StatusUnauthorized
		message = "Unauthorized"
	case errors.Is(err, domain.ErrInvalidToken):
		status = http.StatusUnauthorized
		message = "Invalid or expired token"
	default:
		status = http.StatusInternalServerError
		message = "Internal server error"
	}

	writeJSON(w, status, ErrorResponse{
		Error:   http.StatusText(status),
		Message: message,
	})
}
