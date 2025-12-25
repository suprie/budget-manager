package repository

import (
	"context"
	"database/sql"
	"errors"
	"time"

	"github.com/suprie/budget-manager/internal/domain"
)

type PocketRepository struct {
	db *sql.DB
}

func NewPocketRepository(db *sql.DB) *PocketRepository {
	return &PocketRepository{db: db}
}

func (r *PocketRepository) Create(ctx context.Context, pocket *domain.Pocket) error {
	now := time.Now()
	result, err := r.db.ExecContext(ctx,
		`INSERT INTO pockets (name, description, balance, created_at, updated_at)
		 VALUES (?, ?, ?, ?, ?)`,
		pocket.Name, pocket.Description, pocket.Balance, now, now,
	)
	if err != nil {
		return err
	}

	id, err := result.LastInsertId()
	if err != nil {
		return err
	}

	pocket.ID = id
	pocket.CreatedAt = now
	pocket.UpdatedAt = now
	return nil
}

func (r *PocketRepository) GetByID(ctx context.Context, id int64) (*domain.Pocket, error) {
	pocket := &domain.Pocket{}
	err := r.db.QueryRowContext(ctx,
		`SELECT id, name, description, balance, created_at, updated_at
		 FROM pockets WHERE id = ?`, id,
	).Scan(&pocket.ID, &pocket.Name, &pocket.Description, &pocket.Balance,
		&pocket.CreatedAt, &pocket.UpdatedAt)

	if errors.Is(err, sql.ErrNoRows) {
		return nil, domain.ErrNotFound
	}
	if err != nil {
		return nil, err
	}
	return pocket, nil
}

func (r *PocketRepository) GetAll(ctx context.Context) ([]*domain.Pocket, error) {
	rows, err := r.db.QueryContext(ctx,
		`SELECT id, name, description, balance, created_at, updated_at
		 FROM pockets ORDER BY name`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var pockets []*domain.Pocket
	for rows.Next() {
		pocket := &domain.Pocket{}
		if err := rows.Scan(&pocket.ID, &pocket.Name, &pocket.Description,
			&pocket.Balance, &pocket.CreatedAt, &pocket.UpdatedAt); err != nil {
			return nil, err
		}
		pockets = append(pockets, pocket)
	}
	return pockets, rows.Err()
}

func (r *PocketRepository) Update(ctx context.Context, pocket *domain.Pocket) error {
	pocket.UpdatedAt = time.Now()
	result, err := r.db.ExecContext(ctx,
		`UPDATE pockets SET name = ?, description = ?, balance = ?, updated_at = ?
		 WHERE id = ?`,
		pocket.Name, pocket.Description, pocket.Balance, pocket.UpdatedAt, pocket.ID,
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

func (r *PocketRepository) Delete(ctx context.Context, id int64) error {
	// Check if pocket has budgets
	var count int
	err := r.db.QueryRowContext(ctx,
		`SELECT COUNT(*) FROM budgets WHERE pocket_id = ?`, id,
	).Scan(&count)
	if err != nil {
		return err
	}
	if count > 0 {
		return domain.ErrPocketHasBudgets
	}

	result, err := r.db.ExecContext(ctx, `DELETE FROM pockets WHERE id = ?`, id)
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

func (r *PocketRepository) UpdateBalance(ctx context.Context, id int64, amount float64) error {
	result, err := r.db.ExecContext(ctx,
		`UPDATE pockets SET balance = balance + ?, updated_at = ? WHERE id = ?`,
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
