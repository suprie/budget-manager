package handler

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/suprie/budget-manager/internal/domain"
	"github.com/suprie/budget-manager/internal/service"
)

type ExpenseHandler struct {
	service *service.ExpenseService
}

func NewExpenseHandler(service *service.ExpenseService) *ExpenseHandler {
	return &ExpenseHandler{service: service}
}

func (h *ExpenseHandler) Create(w http.ResponseWriter, r *http.Request) {
	var req domain.CreateExpenseRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	expense, err := h.service.Create(r.Context(), req)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusCreated, expense)
}

func (h *ExpenseHandler) GetByID(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	expense, err := h.service.GetByID(r.Context(), id)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, expense)
}

func (h *ExpenseHandler) GetAll(w http.ResponseWriter, r *http.Request) {
	expenses, err := h.service.GetAll(r.Context())
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, expenses)
}

func (h *ExpenseHandler) GetByBudgetID(w http.ResponseWriter, r *http.Request) {
	budgetID, err := strconv.ParseInt(r.PathValue("budget_id"), 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	expenses, err := h.service.GetByBudgetID(r.Context(), budgetID)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, expenses)
}

func (h *ExpenseHandler) GetByDateRange(w http.ResponseWriter, r *http.Request) {
	startDate := r.URL.Query().Get("start_date")
	endDate := r.URL.Query().Get("end_date")

	if startDate == "" || endDate == "" {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	expenses, err := h.service.GetByDateRange(r.Context(), startDate, endDate)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, expenses)
}

func (h *ExpenseHandler) Update(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	var req domain.UpdateExpenseRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	expense, err := h.service.Update(r.Context(), id, req)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, expense)
}

func (h *ExpenseHandler) Delete(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	if err := h.service.Delete(r.Context(), id); err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, SuccessResponse{Message: "Expense deleted successfully"})
}
