package handler

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/suprie/budget-manager/internal/domain"
	"github.com/suprie/budget-manager/internal/service"
)

type PocketHandler struct {
	service *service.PocketService
}

func NewPocketHandler(service *service.PocketService) *PocketHandler {
	return &PocketHandler{service: service}
}

func (h *PocketHandler) Create(w http.ResponseWriter, r *http.Request) {
	var req domain.CreatePocketRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	pocket, err := h.service.Create(r.Context(), req)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusCreated, pocket)
}

func (h *PocketHandler) GetByID(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	pocket, err := h.service.GetByID(r.Context(), id)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, pocket)
}

func (h *PocketHandler) GetAll(w http.ResponseWriter, r *http.Request) {
	pockets, err := h.service.GetAll(r.Context())
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, pockets)
}

func (h *PocketHandler) Update(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	var req domain.UpdatePocketRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	pocket, err := h.service.Update(r.Context(), id, req)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, pocket)
}

func (h *PocketHandler) Delete(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	if err := h.service.Delete(r.Context(), id); err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, SuccessResponse{Message: "Pocket deleted successfully"})
}

type AddFundsRequest struct {
	Amount float64 `json:"amount"`
}

func (h *PocketHandler) AddFunds(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	var req AddFundsRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, domain.ErrInvalidInput)
		return
	}

	pocket, err := h.service.AddFunds(r.Context(), id, req.Amount)
	if err != nil {
		writeError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, pocket)
}
