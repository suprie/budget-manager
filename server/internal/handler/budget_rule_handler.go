package handler

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/suprie/budget-manager/internal/domain"
	"github.com/suprie/budget-manager/internal/service"
)

type BudgetRuleHandler struct {
	ruleService *service.BudgetRuleService
}

func NewBudgetRuleHandler(ruleService *service.BudgetRuleService) *BudgetRuleHandler {
	return &BudgetRuleHandler{ruleService: ruleService}
}

func (h *BudgetRuleHandler) Create(w http.ResponseWriter, r *http.Request) {
	var req domain.CreateBudgetRuleRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	rule, err := h.ruleService.Create(r.Context(), req)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusCreated, rule)
}

func (h *BudgetRuleHandler) GetAll(w http.ResponseWriter, r *http.Request) {
	rules, err := h.ruleService.GetAll(r.Context())
	if err != nil {
		writeError(w, err)
		return
	}

	if rules == nil {
		rules = []domain.BudgetRuleWithBudget{}
	}

	writeJSON(w, http.StatusOK, rules)
}

func (h *BudgetRuleHandler) GetByID(w http.ResponseWriter, r *http.Request) {
	idStr := r.PathValue("id")
	id, err := strconv.ParseInt(idStr, 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	rule, err := h.ruleService.GetByID(r.Context(), id)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, rule)
}

func (h *BudgetRuleHandler) GetByBudgetID(w http.ResponseWriter, r *http.Request) {
	budgetIDStr := r.PathValue("budget_id")
	budgetID, err := strconv.ParseInt(budgetIDStr, 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	rules, err := h.ruleService.GetByBudgetID(r.Context(), budgetID)
	if err != nil {
		writeError(w, err)
		return
	}

	if rules == nil {
		rules = []domain.BudgetRule{}
	}

	writeJSON(w, http.StatusOK, rules)
}

func (h *BudgetRuleHandler) Update(w http.ResponseWriter, r *http.Request) {
	idStr := r.PathValue("id")
	id, err := strconv.ParseInt(idStr, 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	var req domain.UpdateBudgetRuleRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	rule, err := h.ruleService.Update(r.Context(), id, req)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, rule)
}

func (h *BudgetRuleHandler) Delete(w http.ResponseWriter, r *http.Request) {
	idStr := r.PathValue("id")
	id, err := strconv.ParseInt(idStr, 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	if err := h.ruleService.Delete(r.Context(), id); err != nil {
		writeError(w, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

// MatchTransaction tests which budget a description would match
func (h *BudgetRuleHandler) MatchTransaction(w http.ResponseWriter, r *http.Request) {
	description := r.URL.Query().Get("description")
	if description == "" {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	budgetID, err := h.ruleService.MatchTransaction(r.Context(), description)
	if err != nil {
		writeError(w, err)
		return
	}

	response := map[string]interface{}{
		"description": description,
		"budget_id":   budgetID,
		"matched":     budgetID != nil,
	}

	writeJSON(w, http.StatusOK, response)
}
