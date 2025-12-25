package repository

import (
	"context"
	"database/sql"
	"errors"
	"time"

	"github.com/suprie/budget-manager/internal/domain"
)

type ExpenseRepository struct {
	db *sql.DB
}

func NewExpenseRepository(db *sql.DB) *ExpenseRepository {
	return &ExpenseRepository{db: db}
}

func (r *ExpenseRepository) Create(ctx context.Context, expense *domain.Expense) error {
	now := time.Now()
	result, err := r.db.ExecContext(ctx,
		`INSERT INTO expenses (budget_id, amount, description, date, created_at, updated_at)
		 VALUES (?, ?, ?, ?, ?, ?)`,
		expense.BudgetID, expense.Amount, expense.Description, expense.Date, now, now,
	)
	if err != nil {
		return err
	}

	id, err := result.LastInsertId()
	if err != nil {
		return err
	}

	expense.ID = id
	expense.CreatedAt = now
	expense.UpdatedAt = now
	return nil
}

func (r *ExpenseRepository) GetByID(ctx context.Context, id int64) (*domain.Expense, error) {
	expense := &domain.Expense{}
	err := r.db.QueryRowContext(ctx,
		`SELECT id, budget_id, amount, description, date, created_at, updated_at
		 FROM expenses WHERE id = ?`, id,
	).Scan(&expense.ID, &expense.BudgetID, &expense.Amount, &expense.Description,
		&expense.Date, &expense.CreatedAt, &expense.UpdatedAt)

	if errors.Is(err, sql.ErrNoRows) {
		return nil, domain.ErrNotFound
	}
	if err != nil {
		return nil, err
	}
	return expense, nil
}

func (r *ExpenseRepository) GetAll(ctx context.Context) ([]*domain.Expense, error) {
	rows, err := r.db.QueryContext(ctx,
		`SELECT id, budget_id, amount, description, date, created_at, updated_at
		 FROM expenses ORDER BY date DESC, id DESC`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var expenses []*domain.Expense
	for rows.Next() {
		expense := &domain.Expense{}
		if err := rows.Scan(&expense.ID, &expense.BudgetID, &expense.Amount,
			&expense.Description, &expense.Date, &expense.CreatedAt, &expense.UpdatedAt); err != nil {
			return nil, err
		}
		expenses = append(expenses, expense)
	}
	return expenses, rows.Err()
}

func (r *ExpenseRepository) GetByBudgetID(ctx context.Context, budgetID int64) ([]*domain.Expense, error) {
	rows, err := r.db.QueryContext(ctx,
		`SELECT id, budget_id, amount, description, date, created_at, updated_at
		 FROM expenses WHERE budget_id = ? ORDER BY date DESC, id DESC`, budgetID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var expenses []*domain.Expense
	for rows.Next() {
		expense := &domain.Expense{}
		if err := rows.Scan(&expense.ID, &expense.BudgetID, &expense.Amount,
			&expense.Description, &expense.Date, &expense.CreatedAt, &expense.UpdatedAt); err != nil {
			return nil, err
		}
		expenses = append(expenses, expense)
	}
	return expenses, rows.Err()
}

func (r *ExpenseRepository) GetByDateRange(ctx context.Context, startDate, endDate time.Time) ([]*domain.Expense, error) {
	rows, err := r.db.QueryContext(ctx,
		`SELECT id, budget_id, amount, description, date, created_at, updated_at
		 FROM expenses WHERE date >= ? AND date <= ? ORDER BY date DESC, id DESC`,
		startDate, endDate)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var expenses []*domain.Expense
	for rows.Next() {
		expense := &domain.Expense{}
		if err := rows.Scan(&expense.ID, &expense.BudgetID, &expense.Amount,
			&expense.Description, &expense.Date, &expense.CreatedAt, &expense.UpdatedAt); err != nil {
			return nil, err
		}
		expenses = append(expenses, expense)
	}
	return expenses, rows.Err()
}

func (r *ExpenseRepository) Update(ctx context.Context, expense *domain.Expense) error {
	expense.UpdatedAt = time.Now()
	result, err := r.db.ExecContext(ctx,
		`UPDATE expenses SET budget_id = ?, amount = ?, description = ?, date = ?, updated_at = ?
		 WHERE id = ?`,
		expense.BudgetID, expense.Amount, expense.Description, expense.Date, expense.UpdatedAt, expense.ID,
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

func (r *ExpenseRepository) Delete(ctx context.Context, id int64) error {
	result, err := r.db.ExecContext(ctx, `DELETE FROM expenses WHERE id = ?`, id)
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
