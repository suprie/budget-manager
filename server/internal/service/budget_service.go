package service

import (
	"context"

	"github.com/suprie/budget-manager/internal/domain"
	"github.com/suprie/budget-manager/internal/repository"
)

type BudgetService struct {
	budgetRepo *repository.BudgetRepository
	pocketRepo *repository.PocketRepository
}

func NewBudgetService(budgetRepo *repository.BudgetRepository, pocketRepo *repository.PocketRepository) *BudgetService {
	return &BudgetService{
		budgetRepo: budgetRepo,
		pocketRepo: pocketRepo,
	}
}

func (s *BudgetService) Create(ctx context.Context, req domain.CreateBudgetRequest) (*domain.Budget, error) {
	if req.Name == "" || req.Period == "" {
		return nil, domain.ErrInvalidInput
	}
	if req.AllocatedAmount < 0 {
		return nil, domain.ErrInvalidInput
	}

	// Verify pocket exists
	pocket, err := s.pocketRepo.GetByID(ctx, req.PocketID)
	if err != nil {
		return nil, err
	}

	// Check if pocket has enough balance for allocation
	if pocket.Balance < req.AllocatedAmount {
		return nil, domain.ErrInsufficientFunds
	}

	budget := &domain.Budget{
		Name:           req.Name,
		Description:    req.Description,
		PocketID:       req.PocketID,
		AllocatedAmount: req.AllocatedAmount,
		SpentAmount:    0,
		Period:         req.Period,
	}

	if err := s.budgetRepo.Create(ctx, budget); err != nil {
		return nil, err
	}

	// Deduct allocated amount from pocket balance (zero-sum)
	if err := s.pocketRepo.UpdateBalance(ctx, req.PocketID, -req.AllocatedAmount); err != nil {
		return nil, err
	}

	return budget, nil
}

func (s *BudgetService) GetByID(ctx context.Context, id int64) (*domain.Budget, error) {
	return s.budgetRepo.GetByID(ctx, id)
}

func (s *BudgetService) GetAll(ctx context.Context) ([]*domain.Budget, error) {
	return s.budgetRepo.GetAll(ctx)
}

func (s *BudgetService) GetByPocketID(ctx context.Context, pocketID int64) ([]*domain.Budget, error) {
	return s.budgetRepo.GetByPocketID(ctx, pocketID)
}

func (s *BudgetService) GetByPeriod(ctx context.Context, period string) ([]*domain.Budget, error) {
	return s.budgetRepo.GetByPeriod(ctx, period)
}

func (s *BudgetService) Update(ctx context.Context, id int64, req domain.UpdateBudgetRequest) (*domain.Budget, error) {
	budget, err := s.budgetRepo.GetByID(ctx, id)
	if err != nil {
		return nil, err
	}

	if req.Name != nil {
		budget.Name = *req.Name
	}
	if req.Description != nil {
		budget.Description = *req.Description
	}
	if req.AllocatedAmount != nil {
		oldAmount := budget.AllocatedAmount
		newAmount := *req.AllocatedAmount
		diff := newAmount - oldAmount

		// If increasing allocation, check pocket has enough
		if diff > 0 {
			pocket, err := s.pocketRepo.GetByID(ctx, budget.PocketID)
			if err != nil {
				return nil, err
			}
			if pocket.Balance < diff {
				return nil, domain.ErrInsufficientFunds
			}
		}

		budget.AllocatedAmount = newAmount

		// Adjust pocket balance
		if err := s.pocketRepo.UpdateBalance(ctx, budget.PocketID, -diff); err != nil {
			return nil, err
		}
	}

	if err := s.budgetRepo.Update(ctx, budget); err != nil {
		return nil, err
	}

	return budget, nil
}

func (s *BudgetService) Delete(ctx context.Context, id int64) error {
	budget, err := s.budgetRepo.GetByID(ctx, id)
	if err != nil {
		return err
	}

	// Return unspent funds to pocket
	unspentAmount := budget.AllocatedAmount - budget.SpentAmount
	if unspentAmount > 0 {
		if err := s.pocketRepo.UpdateBalance(ctx, budget.PocketID, unspentAmount); err != nil {
			return err
		}
	}

	return s.budgetRepo.Delete(ctx, id)
}

func (s *BudgetService) GetSummary(ctx context.Context, period string) (*domain.BudgetSummary, error) {
	summary, err := s.budgetRepo.GetSummaryByPeriod(ctx, period)
	if err != nil {
		return nil, err
	}

	// Calculate unallocated funds from all pockets
	pockets, err := s.pocketRepo.GetAll(ctx)
	if err != nil {
		return nil, err
	}

	var totalUnallocated float64
	for _, p := range pockets {
		totalUnallocated += p.Balance
	}
	summary.UnallocatedFunds = totalUnallocated

	return summary, nil
}

// GetRemainingBudget returns how much is left in a specific budget envelope
func (s *BudgetService) GetRemainingBudget(ctx context.Context, id int64) (float64, error) {
	budget, err := s.budgetRepo.GetByID(ctx, id)
	if err != nil {
		return 0, err
	}
	return budget.RemainingAmount(), nil
}
