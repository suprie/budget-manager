#!/usr/bin/env python3
"""
Budget Rules Manager

CLI tool to manage keyword-based budget rules for auto-categorizing
bank statement transactions.
"""

import argparse
import sqlite3
from datetime import datetime
from pathlib import Path


def get_connection(db_path: str) -> sqlite3.Connection:
    """Get database connection."""
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    return conn


def list_rules(db_path: str, show_inactive: bool = False):
    """List all budget rules."""
    conn = get_connection(db_path)
    cursor = conn.cursor()

    query = """
        SELECT br.id, br.budget_id, b.name as budget_name, br.keywords,
               br.priority, br.is_active
        FROM budget_rules br
        JOIN budgets b ON br.budget_id = b.id
    """
    if not show_inactive:
        query += " WHERE br.is_active = 1"
    query += " ORDER BY br.priority DESC, br.id ASC"

    cursor.execute(query)
    rows = cursor.fetchall()

    if not rows:
        print("No budget rules found.")
        return

    print(f"\n{'ID':<5} {'Budget':<20} {'Keywords':<40} {'Pri':<5} {'Active'}")
    print("-" * 80)

    for row in rows:
        active = "Yes" if row['is_active'] else "No"
        keywords = row['keywords'][:37] + "..." if len(row['keywords']) > 40 else row['keywords']
        print(f"{row['id']:<5} {row['budget_name']:<20} {keywords:<40} {row['priority']:<5} {active}")

    conn.close()


def list_budgets(db_path: str):
    """List all budgets (for reference when creating rules)."""
    conn = get_connection(db_path)
    cursor = conn.cursor()

    cursor.execute("""
        SELECT b.id, b.name, b.period, p.name as pocket_name
        FROM budgets b
        JOIN pockets p ON b.pocket_id = p.id
        ORDER BY b.name
    """)
    rows = cursor.fetchall()

    if not rows:
        print("No budgets found. Create budgets first via the API.")
        return

    print(f"\n{'ID':<5} {'Budget Name':<25} {'Period':<12} {'Pocket'}")
    print("-" * 60)

    for row in rows:
        print(f"{row['id']:<5} {row['name']:<25} {row['period']:<12} {row['pocket_name']}")

    conn.close()


def add_rule(db_path: str, budget_id: int, keywords: str, priority: int = 0):
    """Add a new budget rule."""
    conn = get_connection(db_path)
    cursor = conn.cursor()

    # Verify budget exists
    cursor.execute("SELECT name FROM budgets WHERE id = ?", (budget_id,))
    budget = cursor.fetchone()
    if not budget:
        print(f"Error: Budget ID {budget_id} not found.")
        conn.close()
        return

    now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    cursor.execute("""
        INSERT INTO budget_rules (budget_id, keywords, priority, is_active, created_at, updated_at)
        VALUES (?, ?, ?, 1, ?, ?)
    """, (budget_id, keywords, priority, now, now))

    conn.commit()
    rule_id = cursor.lastrowid
    print(f"Created rule #{rule_id} for budget '{budget['name']}'")
    print(f"  Keywords: {keywords}")
    print(f"  Priority: {priority}")

    conn.close()


def update_rule(db_path: str, rule_id: int, keywords: str = None, priority: int = None):
    """Update an existing rule."""
    conn = get_connection(db_path)
    cursor = conn.cursor()

    cursor.execute("SELECT * FROM budget_rules WHERE id = ?", (rule_id,))
    rule = cursor.fetchone()
    if not rule:
        print(f"Error: Rule ID {rule_id} not found.")
        conn.close()
        return

    updates = []
    params = []

    if keywords is not None:
        updates.append("keywords = ?")
        params.append(keywords)
    if priority is not None:
        updates.append("priority = ?")
        params.append(priority)

    if not updates:
        print("No updates specified.")
        conn.close()
        return

    updates.append("updated_at = ?")
    params.append(datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
    params.append(rule_id)

    cursor.execute(f"UPDATE budget_rules SET {', '.join(updates)} WHERE id = ?", params)
    conn.commit()
    print(f"Updated rule #{rule_id}")

    conn.close()


def toggle_rule(db_path: str, rule_id: int, active: bool):
    """Enable or disable a rule."""
    conn = get_connection(db_path)
    cursor = conn.cursor()

    cursor.execute("""
        UPDATE budget_rules
        SET is_active = ?, updated_at = ?
        WHERE id = ?
    """, (1 if active else 0, datetime.now().strftime('%Y-%m-%d %H:%M:%S'), rule_id))

    if cursor.rowcount == 0:
        print(f"Error: Rule ID {rule_id} not found.")
    else:
        status = "enabled" if active else "disabled"
        print(f"Rule #{rule_id} {status}")

    conn.commit()
    conn.close()


def delete_rule(db_path: str, rule_id: int):
    """Delete a rule."""
    conn = get_connection(db_path)
    cursor = conn.cursor()

    cursor.execute("DELETE FROM budget_rules WHERE id = ?", (rule_id,))

    if cursor.rowcount == 0:
        print(f"Error: Rule ID {rule_id} not found.")
    else:
        print(f"Deleted rule #{rule_id}")

    conn.commit()
    conn.close()


def test_match(db_path: str, description: str):
    """Test which budget a description would match."""
    conn = get_connection(db_path)
    cursor = conn.cursor()

    cursor.execute("""
        SELECT br.id, br.budget_id, b.name as budget_name, br.keywords, br.priority
        FROM budget_rules br
        JOIN budgets b ON br.budget_id = b.id
        WHERE br.is_active = 1
        ORDER BY br.priority DESC
    """)
    rules = cursor.fetchall()

    desc_lower = description.lower()
    matched = None

    print(f"\nTesting: \"{description}\"")
    print("-" * 60)

    for rule in rules:
        keywords = [k.strip().lower() for k in rule['keywords'].split(',')]
        for keyword in keywords:
            if keyword and keyword in desc_lower:
                if matched is None:
                    matched = rule
                    print(f"MATCH: Budget '{rule['budget_name']}' (rule #{rule['id']})")
                    print(f"  Matched keyword: \"{keyword}\"")
                else:
                    print(f"  (would also match: '{rule['budget_name']}' via \"{keyword}\")")
                break

    if not matched:
        print("No matching rule found.")

    conn.close()


def setup_common_rules(db_path: str):
    """Setup common Indonesian expense categorization rules."""
    conn = get_connection(db_path)
    cursor = conn.cursor()

    # Check if budgets exist
    cursor.execute("SELECT COUNT(*) as count FROM budgets")
    if cursor.fetchone()['count'] == 0:
        print("Error: No budgets found. Create budgets first before setting up rules.")
        conn.close()
        return

    common_rules = [
        # Format: (budget_name_pattern, keywords, priority)
        ("transport", "grab,gojek,uber,taxi,taksi,tol,toll,parkir,parking,kereta,train,mrt,lrt,transjakarta,busway", 10),
        ("food", "makan,resto,restaurant,cafe,kopi,coffee,starbucks,mcd,mcdonald,kfc,pizza,bakery,warung,food", 10),
        ("groceries", "supermarket,indomaret,alfamart,giant,carrefour,hypermart,superindo,farmers,pasar", 10),
        ("utilities", "pln,listrik,electric,pdam,air,water,gas,internet,wifi,indihome,biznet,telkom", 20),
        ("phone", "pulsa,telkomsel,xl,indosat,smartfren,tri,axis", 15),
        ("entertainment", "netflix,spotify,youtube,disney,bioskop,cinema,game,steam", 10),
        ("health", "apotek,pharmacy,dokter,doctor,rumah sakit,hospital,klinik,clinic,obat,medicine", 15),
        ("shopping", "tokopedia,shopee,lazada,blibli,zalora,uniqlo,h&m,zara", 5),
    ]

    print("\nSetting up common rules...")
    print("Looking for matching budgets...\n")

    for budget_pattern, keywords, priority in common_rules:
        cursor.execute(
            "SELECT id, name FROM budgets WHERE LOWER(name) LIKE ?",
            (f"%{budget_pattern}%",)
        )
        budget = cursor.fetchone()

        if budget:
            # Check if rule already exists
            cursor.execute(
                "SELECT id FROM budget_rules WHERE budget_id = ?",
                (budget['id'],)
            )
            if cursor.fetchone():
                print(f"  [SKIP] Budget '{budget['name']}' already has rules")
                continue

            now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            cursor.execute("""
                INSERT INTO budget_rules (budget_id, keywords, priority, is_active, created_at, updated_at)
                VALUES (?, ?, ?, 1, ?, ?)
            """, (budget['id'], keywords, priority, now, now))
            print(f"  [OK] Created rule for '{budget['name']}'")
        else:
            print(f"  [SKIP] No budget matching '{budget_pattern}' found")

    conn.commit()
    conn.close()
    print("\nDone! Use 'list' command to see all rules.")


def main():
    parser = argparse.ArgumentParser(
        description='Manage budget rules for auto-categorizing expenses'
    )
    parser.add_argument('--db', '-d', default='./budget.db',
                       help='Path to SQLite database (default: ./budget.db)')

    subparsers = parser.add_subparsers(dest='command', help='Commands')

    # List rules
    list_parser = subparsers.add_parser('list', help='List all budget rules')
    list_parser.add_argument('--all', '-a', action='store_true',
                            help='Include inactive rules')

    # List budgets
    subparsers.add_parser('budgets', help='List all budgets (for reference)')

    # Add rule
    add_parser = subparsers.add_parser('add', help='Add a new budget rule')
    add_parser.add_argument('budget_id', type=int, help='Budget ID to assign')
    add_parser.add_argument('keywords', help='Comma-separated keywords')
    add_parser.add_argument('--priority', '-p', type=int, default=0,
                           help='Rule priority (higher = checked first)')

    # Update rule
    update_parser = subparsers.add_parser('update', help='Update a rule')
    update_parser.add_argument('rule_id', type=int, help='Rule ID to update')
    update_parser.add_argument('--keywords', '-k', help='New keywords')
    update_parser.add_argument('--priority', '-p', type=int, help='New priority')

    # Enable/disable rule
    enable_parser = subparsers.add_parser('enable', help='Enable a rule')
    enable_parser.add_argument('rule_id', type=int)

    disable_parser = subparsers.add_parser('disable', help='Disable a rule')
    disable_parser.add_argument('rule_id', type=int)

    # Delete rule
    delete_parser = subparsers.add_parser('delete', help='Delete a rule')
    delete_parser.add_argument('rule_id', type=int)

    # Test match
    test_parser = subparsers.add_parser('test', help='Test which budget matches a description')
    test_parser.add_argument('description', help='Transaction description to test')

    # Setup common rules
    subparsers.add_parser('setup', help='Setup common Indonesian expense rules')

    args = parser.parse_args()

    # Validate database exists
    if args.command not in [None]:
        db_path = Path(args.db)
        if not db_path.exists():
            print(f"Error: Database not found: {db_path}")
            return 1

    if args.command == 'list':
        list_rules(args.db, args.all)
    elif args.command == 'budgets':
        list_budgets(args.db)
    elif args.command == 'add':
        add_rule(args.db, args.budget_id, args.keywords, args.priority)
    elif args.command == 'update':
        update_rule(args.db, args.rule_id, args.keywords, args.priority)
    elif args.command == 'enable':
        toggle_rule(args.db, args.rule_id, True)
    elif args.command == 'disable':
        toggle_rule(args.db, args.rule_id, False)
    elif args.command == 'delete':
        delete_rule(args.db, args.rule_id)
    elif args.command == 'test':
        test_match(args.db, args.description)
    elif args.command == 'setup':
        setup_common_rules(args.db)
    else:
        parser.print_help()

    return 0


if __name__ == '__main__':
    exit(main())
