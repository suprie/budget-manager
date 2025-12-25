package service

import (
	"context"
	"strings"

	"github.com/suprie/budget-manager/internal/domain"
	"github.com/suprie/budget-manager/internal/repository"
)

type BudgetRuleService struct {
	ruleRepo   *repository.BudgetRuleRepository
	budgetRepo *repository.BudgetRepository
}

func NewBudgetRuleService(ruleRepo *repository.BudgetRuleRepository, budgetRepo *repository.BudgetRepository) *BudgetRuleService {
	return &BudgetRuleService{
		ruleRepo:   ruleRepo,
		budgetRepo: budgetRepo,
	}
}

func (s *BudgetRuleService) Create(ctx context.Context, req domain.CreateBudgetRuleRequest) (*domain.BudgetRule, error) {
	if req.BudgetID <= 0 {
		return nil, domain.ErrInvalidInput
	}

	keywords := strings.TrimSpace(req.Keywords)
	if keywords == "" {
		return nil, domain.ErrInvalidInput
	}

	// Verify budget exists
	_, err := s.budgetRepo.GetByID(ctx, req.BudgetID)
	if err != nil {
		return nil, err
	}

	rule := &domain.BudgetRule{
		BudgetID: req.BudgetID,
		Keywords: keywords,
		Priority: req.Priority,
		IsActive: true,
	}

	if err := s.ruleRepo.Create(ctx, rule); err != nil {
		return nil, err
	}

	return rule, nil
}

func (s *BudgetRuleService) GetByID(ctx context.Context, id int64) (*domain.BudgetRule, error) {
	return s.ruleRepo.GetByID(ctx, id)
}

func (s *BudgetRuleService) GetAll(ctx context.Context) ([]domain.BudgetRuleWithBudget, error) {
	return s.ruleRepo.GetAll(ctx)
}

func (s *BudgetRuleService) GetByBudgetID(ctx context.Context, budgetID int64) ([]domain.BudgetRule, error) {
	return s.ruleRepo.GetByBudgetID(ctx, budgetID)
}

func (s *BudgetRuleService) GetActiveRules(ctx context.Context) ([]domain.BudgetRule, error) {
	return s.ruleRepo.GetActiveRules(ctx)
}

func (s *BudgetRuleService) Update(ctx context.Context, id int64, req domain.UpdateBudgetRuleRequest) (*domain.BudgetRule, error) {
	rule, err := s.ruleRepo.GetByID(ctx, id)
	if err != nil {
		return nil, err
	}

	if req.Keywords != nil {
		keywords := strings.TrimSpace(*req.Keywords)
		if keywords == "" {
			return nil, domain.ErrInvalidInput
		}
		rule.Keywords = keywords
	}

	if req.Priority != nil {
		rule.Priority = *req.Priority
	}

	if req.IsActive != nil {
		rule.IsActive = *req.IsActive
	}

	if err := s.ruleRepo.Update(ctx, rule); err != nil {
		return nil, err
	}

	return rule, nil
}

func (s *BudgetRuleService) Delete(ctx context.Context, id int64) error {
	return s.ruleRepo.Delete(ctx, id)
}

// MatchTransaction finds the best matching budget for a transaction description
func (s *BudgetRuleService) MatchTransaction(ctx context.Context, description string) (*int64, error) {
	rules, err := s.ruleRepo.GetActiveRules(ctx)
	if err != nil {
		return nil, err
	}

	descLower := strings.ToLower(description)

	for _, rule := range rules {
		keywords := strings.Split(rule.Keywords, ",")
		for _, keyword := range keywords {
			keyword = strings.TrimSpace(strings.ToLower(keyword))
			if keyword != "" && strings.Contains(descLower, keyword) {
				return &rule.BudgetID, nil
			}
		}
	}

	return nil, nil
}
