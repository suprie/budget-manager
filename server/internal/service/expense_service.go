package service

import (
	"context"
	"time"

	"github.com/suprie/budget-manager/internal/domain"
	"github.com/suprie/budget-manager/internal/repository"
)

type ExpenseService struct {
	expenseRepo *repository.ExpenseRepository
	budgetRepo  *repository.BudgetRepository
}

func NewExpenseService(expenseRepo *repository.ExpenseRepository, budgetRepo *repository.BudgetRepository) *ExpenseService {
	return &ExpenseService{
		expenseRepo: expenseRepo,
		budgetRepo:  budgetRepo,
	}
}

func (s *ExpenseService) Create(ctx context.Context, req domain.CreateExpenseRequest) (*domain.Expense, error) {
	if req.Amount <= 0 {
		return nil, domain.ErrInvalidInput
	}
	if req.Description == "" {
		return nil, domain.ErrInvalidInput
	}

	// Verify budget exists and has enough funds
	budget, err := s.budgetRepo.GetByID(ctx, req.BudgetID)
	if err != nil {
		return nil, err
	}

	remaining := budget.RemainingAmount()
	if remaining < req.Amount {
		return nil, domain.ErrInsufficientFunds
	}

	// Parse date
	expenseDate, err := time.Parse("2006-01-02", req.Date)
	if err != nil {
		return nil, domain.ErrInvalidInput
	}

	expense := &domain.Expense{
		BudgetID:    req.BudgetID,
		Amount:      req.Amount,
		Description: req.Description,
		Date:        expenseDate,
	}

	if err := s.expenseRepo.Create(ctx, expense); err != nil {
		return nil, err
	}

	// Update budget spent amount
	if err := s.budgetRepo.UpdateSpentAmount(ctx, req.BudgetID, req.Amount); err != nil {
		return nil, err
	}

	return expense, nil
}

func (s *ExpenseService) GetByID(ctx context.Context, id int64) (*domain.Expense, error) {
	return s.expenseRepo.GetByID(ctx, id)
}

func (s *ExpenseService) GetAll(ctx context.Context) ([]*domain.Expense, error) {
	return s.expenseRepo.GetAll(ctx)
}

func (s *ExpenseService) GetByBudgetID(ctx context.Context, budgetID int64) ([]*domain.Expense, error) {
	return s.expenseRepo.GetByBudgetID(ctx, budgetID)
}

func (s *ExpenseService) GetByDateRange(ctx context.Context, startDate, endDate string) ([]*domain.Expense, error) {
	start, err := time.Parse("2006-01-02", startDate)
	if err != nil {
		return nil, domain.ErrInvalidInput
	}
	end, err := time.Parse("2006-01-02", endDate)
	if err != nil {
		return nil, domain.ErrInvalidInput
	}

	return s.expenseRepo.GetByDateRange(ctx, start, end)
}

func (s *ExpenseService) Update(ctx context.Context, id int64, req domain.UpdateExpenseRequest) (*domain.Expense, error) {
	expense, err := s.expenseRepo.GetByID(ctx, id)
	if err != nil {
		return nil, err
	}

	oldAmount := expense.Amount
	oldBudgetID := expense.BudgetID

	if req.Description != nil {
		expense.Description = *req.Description
	}
	if req.Date != nil {
		expenseDate, err := time.Parse("2006-01-02", *req.Date)
		if err != nil {
			return nil, domain.ErrInvalidInput
		}
		expense.Date = expenseDate
	}
	if req.Amount != nil {
		expense.Amount = *req.Amount
	}
	if req.BudgetID != nil {
		expense.BudgetID = *req.BudgetID
	}

	// Handle budget changes
	if expense.BudgetID != oldBudgetID {
		// Moving to different budget
		newBudget, err := s.budgetRepo.GetByID(ctx, expense.BudgetID)
		if err != nil {
			return nil, err
		}
		if newBudget.RemainingAmount() < expense.Amount {
			return nil, domain.ErrInsufficientFunds
		}

		// Restore old budget
		if err := s.budgetRepo.UpdateSpentAmount(ctx, oldBudgetID, -oldAmount); err != nil {
			return nil, err
		}
		// Deduct from new budget
		if err := s.budgetRepo.UpdateSpentAmount(ctx, expense.BudgetID, expense.Amount); err != nil {
			return nil, err
		}
	} else if expense.Amount != oldAmount {
		// Same budget, different amount
		diff := expense.Amount - oldAmount
		budget, err := s.budgetRepo.GetByID(ctx, expense.BudgetID)
		if err != nil {
			return nil, err
		}
		if diff > 0 && budget.RemainingAmount() < diff {
			return nil, domain.ErrInsufficientFunds
		}
		if err := s.budgetRepo.UpdateSpentAmount(ctx, expense.BudgetID, diff); err != nil {
			return nil, err
		}
	}

	if err := s.expenseRepo.Update(ctx, expense); err != nil {
		return nil, err
	}

	return expense, nil
}

func (s *ExpenseService) Delete(ctx context.Context, id int64) error {
	expense, err := s.expenseRepo.GetByID(ctx, id)
	if err != nil {
		return err
	}

	// Restore budget spent amount
	if err := s.budgetRepo.UpdateSpentAmount(ctx, expense.BudgetID, -expense.Amount); err != nil {
		return err
	}

	return s.expenseRepo.Delete(ctx, id)
}
