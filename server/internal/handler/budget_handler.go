package handler

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/suprie/budget-manager/internal/domain"
	"github.com/suprie/budget-manager/internal/service"
)

type BudgetHandler struct {
	service *service.BudgetService
}

func NewBudgetHandler(service *service.BudgetService) *BudgetHandler {
	return &BudgetHandler{service: service}
}

func (h *BudgetHandler) Create(w http.ResponseWriter, r *http.Request) {
	var req domain.CreateBudgetRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	budget, err := h.service.Create(r.Context(), req)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusCreated, budget)
}

func (h *BudgetHandler) GetByID(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	budget, err := h.service.GetByID(r.Context(), id)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, budget)
}

func (h *BudgetHandler) GetAll(w http.ResponseWriter, r *http.Request) {
	budgets, err := h.service.GetAll(r.Context())
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, budgets)
}

func (h *BudgetHandler) GetByPocketID(w http.ResponseWriter, r *http.Request) {
	pocketID, err := strconv.ParseInt(r.PathValue("pocket_id"), 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	budgets, err := h.service.GetByPocketID(r.Context(), pocketID)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, budgets)
}

func (h *BudgetHandler) GetByPeriod(w http.ResponseWriter, r *http.Request) {
	period := r.URL.Query().Get("period")
	if period == "" {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	budgets, err := h.service.GetByPeriod(r.Context(), period)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, budgets)
}

func (h *BudgetHandler) Update(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	var req domain.UpdateBudgetRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	budget, err := h.service.Update(r.Context(), id, req)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, budget)
}

func (h *BudgetHandler) Delete(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	if err := h.service.Delete(r.Context(), id); err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, SuccessResponse{Message: "Budget deleted successfully"})
}

func (h *BudgetHandler) GetSummary(w http.ResponseWriter, r *http.Request) {
	period := r.URL.Query().Get("period")
	if period == "" {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	summary, err := h.service.GetSummary(r.Context(), period)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, summary)
}

type RemainingBudgetResponse struct {
	BudgetID  int64   `json:"budget_id"`
	Remaining float64 `json:"remaining"`
}

func (h *BudgetHandler) GetRemaining(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	remaining, err := h.service.GetRemainingBudget(r.Context(), id)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, RemainingBudgetResponse{
		BudgetID:  id,
		Remaining: remaining,
	})
}
