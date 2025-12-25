package repository

import (
	"context"
	"database/sql"
	"errors"
	"time"

	"github.com/suprie/budget-manager/internal/domain"
)

type BudgetRepository struct {
	db *sql.DB
}

func NewBudgetRepository(db *sql.DB) *BudgetRepository {
	return &BudgetRepository{db: db}
}

func (r *BudgetRepository) Create(ctx context.Context, budget *domain.Budget) error {
	now := time.Now()
	result, err := r.db.ExecContext(ctx,
		`INSERT INTO budgets (name, description, pocket_id, allocated_amount, spent_amount, period, created_at, updated_at)
		 VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
		budget.Name, budget.Description, budget.PocketID, budget.AllocatedAmount,
		budget.SpentAmount, budget.Period, now, now,
	)
	if err != nil {
		return err
	}

	id, err := result.LastInsertId()
	if err != nil {
		return err
	}

	budget.ID = id
	budget.CreatedAt = now
	budget.UpdatedAt = now
	return nil
}

func (r *BudgetRepository) GetByID(ctx context.Context, id int64) (*domain.Budget, error) {
	budget := &domain.Budget{}
	err := r.db.QueryRowContext(ctx,
		`SELECT id, name, description, pocket_id, allocated_amount, spent_amount, period, created_at, updated_at
		 FROM budgets WHERE id = ?`, id,
	).Scan(&budget.ID, &budget.Name, &budget.Description, &budget.PocketID,
		&budget.AllocatedAmount, &budget.SpentAmount, &budget.Period,
		&budget.CreatedAt, &budget.UpdatedAt)

	if errors.Is(err, sql.ErrNoRows) {
		return nil, domain.ErrNotFound
	}
	if err != nil {
		return nil, err
	}
	return budget, nil
}

func (r *BudgetRepository) GetAll(ctx context.Context) ([]*domain.Budget, error) {
	rows, err := r.db.QueryContext(ctx,
		`SELECT id, name, description, pocket_id, allocated_amount, spent_amount, period, created_at, updated_at
		 FROM budgets ORDER BY period DESC, name`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var budgets []*domain.Budget
	for rows.Next() {
		budget := &domain.Budget{}
		if err := rows.Scan(&budget.ID, &budget.Name, &budget.Description, &budget.PocketID,
			&budget.AllocatedAmount, &budget.SpentAmount, &budget.Period,
			&budget.CreatedAt, &budget.UpdatedAt); err != nil {
			return nil, err
		}
		budgets = append(budgets, budget)
	}
	return budgets, rows.Err()
}

func (r *BudgetRepository) GetByPocketID(ctx context.Context, pocketID int64) ([]*domain.Budget, error) {
	rows, err := r.db.QueryContext(ctx,
		`SELECT id, name, description, pocket_id, allocated_amount, spent_amount, period, created_at, updated_at
		 FROM budgets WHERE pocket_id = ? ORDER BY period DESC, name`, pocketID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var budgets []*domain.Budget
	for rows.Next() {
		budget := &domain.Budget{}
		if err := rows.Scan(&budget.ID, &budget.Name, &budget.Description, &budget.PocketID,
			&budget.AllocatedAmount, &budget.SpentAmount, &budget.Period,
			&budget.CreatedAt, &budget.UpdatedAt); err != nil {
			return nil, err
		}
		budgets = append(budgets, budget)
	}
	return budgets, rows.Err()
}

func (r *BudgetRepository) GetByPeriod(ctx context.Context, period string) ([]*domain.Budget, error) {
	rows, err := r.db.QueryContext(ctx,
		`SELECT id, name, description, pocket_id, allocated_amount, spent_amount, period, created_at, updated_at
		 FROM budgets WHERE period = ? ORDER BY name`, period)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var budgets []*domain.Budget
	for rows.Next() {
		budget := &domain.Budget{}
		if err := rows.Scan(&budget.ID, &budget.Name, &budget.Description, &budget.PocketID,
			&budget.AllocatedAmount, &budget.SpentAmount, &budget.Period,
			&budget.CreatedAt, &budget.UpdatedAt); err != nil {
			return nil, err
		}
		budgets = append(budgets, budget)
	}
	return budgets, rows.Err()
}

func (r *BudgetRepository) Update(ctx context.Context, budget *domain.Budget) error {
	budget.UpdatedAt = time.Now()
	result, err := r.db.ExecContext(ctx,
		`UPDATE budgets SET name = ?, description = ?, allocated_amount = ?, updated_at = ?
		 WHERE id = ?`,
		budget.Name, budget.Description, budget.AllocatedAmount, budget.UpdatedAt, budget.ID,
	)
	if err != nil {
		return err
	}

	rows, err := result.RowsAffected()
	if err != nil {
		return err
	}
	if rows == 0 {
		return domain.ErrNotFound
	}
	return nil
}

func (r *BudgetRepository) UpdateSpentAmount(ctx context.Context, id int64, amount float64) error {
	result, err := r.db.ExecContext(ctx,
		`UPDATE budgets SET spent_amount = spent_amount + ?, updated_at = ? WHERE id = ?`,
		amount, time.Now(), id,
	)
	if err != nil {
		return err
	}

	rows, err := result.RowsAffected()
	if err != nil {
		return err
	}
	if rows == 0 {
		return domain.ErrNotFound
	}
	return nil
}

func (r *BudgetRepository) Delete(ctx context.Context, id int64) error {
	// Check if budget has expenses
	var count int
	err := r.db.QueryRowContext(ctx,
		`SELECT COUNT(*) FROM expenses WHERE budget_id = ?`, id,
	).Scan(&count)
	if err != nil {
		return err
	}
	if count > 0 {
		return domain.ErrBudgetHasExpenses
	}

	result, err := r.db.ExecContext(ctx, `DELETE FROM budgets WHERE id = ?`, id)
	if err != nil {
		return err
	}

	rows, err := result.RowsAffected()
	if err != nil {
		return err
	}
	if rows == 0 {
		return domain.ErrNotFound
	}
	return nil
}

func (r *BudgetRepository) GetSummaryByPeriod(ctx context.Context, period string) (*domain.BudgetSummary, error) {
	summary := &domain.BudgetSummary{Period: period}

	err := r.db.QueryRowContext(ctx,
		`SELECT COALESCE(SUM(allocated_amount), 0), COALESCE(SUM(spent_amount), 0)
		 FROM budgets WHERE period = ?`, period,
	).Scan(&summary.TotalAllocated, &summary.TotalSpent)

	if err != nil {
		return nil, err
	}

	summary.TotalRemaining = summary.TotalAllocated - summary.TotalSpent
	return summary, nil
}
