package repository

import (
	"context"
	"database/sql"
	"time"

	"github.com/suprie/budget-manager/internal/domain"
)

type BudgetRuleRepository struct {
	db *sql.DB
}

func NewBudgetRuleRepository(db *sql.DB) *BudgetRuleRepository {
	return &BudgetRuleRepository{db: db}
}

func (r *BudgetRuleRepository) Create(ctx context.Context, rule *domain.BudgetRule) error {
	now := time.Now()
	result, err := r.db.ExecContext(ctx,
		`INSERT INTO budget_rules (budget_id, keywords, priority, is_active, created_at, updated_at)
		 VALUES (?, ?, ?, ?, ?, ?)`,
		rule.BudgetID, rule.Keywords, rule.Priority, rule.IsActive, now, now,
	)
	if err != nil {
		return err
	}

	id, err := result.LastInsertId()
	if err != nil {
		return err
	}

	rule.ID = id
	rule.CreatedAt = now
	rule.UpdatedAt = now
	return nil
}

func (r *BudgetRuleRepository) GetByID(ctx context.Context, id int64) (*domain.BudgetRule, error) {
	rule := &domain.BudgetRule{}
	err := r.db.QueryRowContext(ctx,
		`SELECT id, budget_id, keywords, priority, is_active, created_at, updated_at
		 FROM budget_rules WHERE id = ?`, id,
	).Scan(&rule.ID, &rule.BudgetID, &rule.Keywords, &rule.Priority,
		&rule.IsActive, &rule.CreatedAt, &rule.UpdatedAt)

	if err == sql.ErrNoRows {
		return nil, domain.ErrNotFound
	}
	if err != nil {
		return nil, err
	}
	return rule, nil
}

func (r *BudgetRuleRepository) GetAll(ctx context.Context) ([]domain.BudgetRuleWithBudget, error) {
	rows, err := r.db.QueryContext(ctx,
		`SELECT br.id, br.budget_id, br.keywords, br.priority, br.is_active,
		        br.created_at, br.updated_at, b.name
		 FROM budget_rules br
		 JOIN budgets b ON br.budget_id = b.id
		 ORDER BY br.priority DESC, br.id ASC`,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var rules []domain.BudgetRuleWithBudget
	for rows.Next() {
		var rule domain.BudgetRuleWithBudget
		if err := rows.Scan(&rule.ID, &rule.BudgetID, &rule.Keywords, &rule.Priority,
			&rule.IsActive, &rule.CreatedAt, &rule.UpdatedAt, &rule.BudgetName); err != nil {
			return nil, err
		}
		rules = append(rules, rule)
	}

	return rules, rows.Err()
}

func (r *BudgetRuleRepository) GetByBudgetID(ctx context.Context, budgetID int64) ([]domain.BudgetRule, error) {
	rows, err := r.db.QueryContext(ctx,
		`SELECT id, budget_id, keywords, priority, is_active, created_at, updated_at
		 FROM budget_rules WHERE budget_id = ? ORDER BY priority DESC`, budgetID,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var rules []domain.BudgetRule
	for rows.Next() {
		var rule domain.BudgetRule
		if err := rows.Scan(&rule.ID, &rule.BudgetID, &rule.Keywords, &rule.Priority,
			&rule.IsActive, &rule.CreatedAt, &rule.UpdatedAt); err != nil {
			return nil, err
		}
		rules = append(rules, rule)
	}

	return rules, rows.Err()
}

func (r *BudgetRuleRepository) GetActiveRules(ctx context.Context) ([]domain.BudgetRule, error) {
	rows, err := r.db.QueryContext(ctx,
		`SELECT id, budget_id, keywords, priority, is_active, created_at, updated_at
		 FROM budget_rules WHERE is_active = 1 ORDER BY priority DESC`,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var rules []domain.BudgetRule
	for rows.Next() {
		var rule domain.BudgetRule
		if err := rows.Scan(&rule.ID, &rule.BudgetID, &rule.Keywords, &rule.Priority,
			&rule.IsActive, &rule.CreatedAt, &rule.UpdatedAt); err != nil {
			return nil, err
		}
		rules = append(rules, rule)
	}

	return rules, rows.Err()
}

func (r *BudgetRuleRepository) Update(ctx context.Context, rule *domain.BudgetRule) error {
	rule.UpdatedAt = time.Now()
	result, err := r.db.ExecContext(ctx,
		`UPDATE budget_rules
		 SET keywords = ?, priority = ?, is_active = ?, updated_at = ?
		 WHERE id = ?`,
		rule.Keywords, rule.Priority, rule.IsActive, rule.UpdatedAt, rule.ID,
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

func (r *BudgetRuleRepository) Delete(ctx context.Context, id int64) error {
	result, err := r.db.ExecContext(ctx,
		`DELETE FROM budget_rules WHERE id = ?`, id,
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
