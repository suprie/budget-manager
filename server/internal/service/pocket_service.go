package service

import (
	"context"

	"github.com/suprie/budget-manager/internal/domain"
	"github.com/suprie/budget-manager/internal/repository"
)

type PocketService struct {
	repo *repository.PocketRepository
}

func NewPocketService(repo *repository.PocketRepository) *PocketService {
	return &PocketService{repo: repo}
}

func (s *PocketService) Create(ctx context.Context, req domain.CreatePocketRequest) (*domain.Pocket, error) {
	if req.Name == "" {
		return nil, domain.ErrInvalidInput
	}

	pocket := &domain.Pocket{
		Name:        req.Name,
		Description: req.Description,
		Balance:     req.Balance,
	}

	if err := s.repo.Create(ctx, pocket); err != nil {
		return nil, err
	}

	return pocket, nil
}

func (s *PocketService) GetByID(ctx context.Context, id int64) (*domain.Pocket, error) {
	return s.repo.GetByID(ctx, id)
}

func (s *PocketService) GetAll(ctx context.Context) ([]*domain.Pocket, error) {
	return s.repo.GetAll(ctx)
}

func (s *PocketService) Update(ctx context.Context, id int64, req domain.UpdatePocketRequest) (*domain.Pocket, error) {
	pocket, err := s.repo.GetByID(ctx, id)
	if err != nil {
		return nil, err
	}

	if req.Name != nil {
		pocket.Name = *req.Name
	}
	if req.Description != nil {
		pocket.Description = *req.Description
	}
	if req.Balance != nil {
		pocket.Balance = *req.Balance
	}

	if err := s.repo.Update(ctx, pocket); err != nil {
		return nil, err
	}

	return pocket, nil
}

func (s *PocketService) Delete(ctx context.Context, id int64) error {
	return s.repo.Delete(ctx, id)
}

func (s *PocketService) AddFunds(ctx context.Context, id int64, amount float64) (*domain.Pocket, error) {
	if amount <= 0 {
		return nil, domain.ErrInvalidInput
	}

	if err := s.repo.UpdateBalance(ctx, id, amount); err != nil {
		return nil, err
	}

	return s.repo.GetByID(ctx, id)
}
