#!/usr/bin/env python3
"""
Bank Statement PDF Parser for Budget Manager

Parses PDF bank statements from BCA, JAGO, and Line Bank,
extracts transactions, and inserts them as expenses with
keyword-based budget categorization.
"""

import argparse
import sqlite3
import re
from abc import ABC, abstractmethod
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Optional

import pdfplumber
from dateutil import parser as date_parser


@dataclass
class Transaction:
    """Represents a parsed bank transaction."""
    date: datetime
    description: str
    amount: float
    transaction_type: str  # 'debit' or 'credit'
    raw_text: str = ""


class BankParser(ABC):
    """Abstract base class for bank statement parsers."""

    @abstractmethod
    def parse(self, pdf_path: str) -> list[Transaction]:
        """Parse a PDF file and return list of transactions."""
        pass

    @abstractmethod
    def bank_name(self) -> str:
        """Return the name of the bank."""
        pass


class BCAParser(BankParser):
    """Parser for BCA (Bank Central Asia) PDF statements."""

    def bank_name(self) -> str:
        return "BCA"

    def parse(self, pdf_path: str) -> list[Transaction]:
        transactions = []

        with pdfplumber.open(pdf_path) as pdf:
            for page in pdf.pages:
                text = page.extract_text() or ""
                transactions.extend(self._parse_page(text))

        return transactions

    def _parse_page(self, text: str) -> list[Transaction]:
        transactions = []
        lines = text.split('\n')

        # BCA statement format typically:
        # DD/MM DESCRIPTION DEBIT CREDIT BALANCE
        # Pattern for transaction lines
        date_pattern = r'^(\d{2}/\d{2})\s+'
        amount_pattern = r'([\d,]+\.?\d*)'

        i = 0
        while i < len(lines):
            line = lines[i].strip()
            date_match = re.match(date_pattern, line)

            if date_match:
                date_str = date_match.group(1)
                remaining = line[date_match.end():]

                # Extract amounts (look for numbers with commas/dots)
                amounts = re.findall(amount_pattern, remaining)

                if amounts:
                    # Get description (text before amounts)
                    desc_end = remaining.find(amounts[0])
                    description = remaining[:desc_end].strip() if desc_end > 0 else remaining

                    # Clean up description - may span multiple lines
                    j = i + 1
                    while j < len(lines) and not re.match(date_pattern, lines[j].strip()):
                        next_line = lines[j].strip()
                        if next_line and not re.match(r'^[\d,]+\.?\d*$', next_line):
                            # Check if it looks like a continuation
                            if not any(c.isdigit() for c in next_line[:5]):
                                description += " " + next_line
                        j += 1

                    # Parse amount - typically debit is expense
                    amount_str = amounts[0].replace(',', '')
                    try:
                        amount = float(amount_str)

                        # Determine if debit or credit based on column position
                        # For BCA, debit (expenses) are typically in first amount column
                        trans_type = 'debit' if len(amounts) >= 2 else 'credit'

                        # Parse date (assume current year)
                        current_year = datetime.now().year
                        parsed_date = datetime.strptime(f"{date_str}/{current_year}", "%d/%m/%Y")

                        transactions.append(Transaction(
                            date=parsed_date,
                            description=description.strip(),
                            amount=amount,
                            transaction_type=trans_type,
                            raw_text=line
                        ))
                    except (ValueError, IndexError):
                        pass

            i += 1

        return transactions


class JAGOParser(BankParser):
    """Parser for Bank JAGO PDF statements."""

    def bank_name(self) -> str:
        return "JAGO"

    def parse(self, pdf_path: str) -> list[Transaction]:
        transactions = []

        with pdfplumber.open(pdf_path) as pdf:
            for page in pdf.pages:
                # Try table extraction first (JAGO often uses tables)
                tables = page.extract_tables()
                if tables:
                    for table in tables:
                        transactions.extend(self._parse_table(table))
                else:
                    # Fallback to text extraction
                    text = page.extract_text() or ""
                    transactions.extend(self._parse_text(text))

        return transactions

    def _parse_table(self, table: list) -> list[Transaction]:
        transactions = []

        for row in table:
            if not row or len(row) < 3:
                continue

            # Skip header rows
            if any(header in str(row[0]).lower() for header in ['tanggal', 'date', 'keterangan']):
                continue

            try:
                # JAGO format: Date | Description | Debit | Credit | Balance
                date_str = str(row[0]).strip()
                description = str(row[1]).strip() if len(row) > 1 else ""

                # Try to parse date
                parsed_date = self._parse_date(date_str)
                if not parsed_date:
                    continue

                # Get amount - check debit column first (index 2)
                amount = 0.0
                trans_type = 'credit'

                for idx, col_type in [(2, 'debit'), (3, 'credit')]:
                    if len(row) > idx and row[idx]:
                        amt_str = str(row[idx]).replace(',', '').replace('.', '').strip()
                        amt_str = re.sub(r'[^\d.-]', '', amt_str)
                        if amt_str:
                            try:
                                amount = float(amt_str)
                                if amount > 0:
                                    trans_type = col_type
                                    break
                            except ValueError:
                                pass

                if amount > 0 and description:
                    transactions.append(Transaction(
                        date=parsed_date,
                        description=description,
                        amount=amount,
                        transaction_type=trans_type,
                        raw_text=str(row)
                    ))
            except Exception:
                continue

        return transactions

    def _parse_text(self, text: str) -> list[Transaction]:
        transactions = []
        lines = text.split('\n')

        # JAGO text format pattern
        date_pattern = r'(\d{1,2}\s+\w+\s+\d{4}|\d{2}/\d{2}/\d{4}|\d{2}-\d{2}-\d{4})'

        for line in lines:
            match = re.search(date_pattern, line)
            if match:
                date_str = match.group(1)
                parsed_date = self._parse_date(date_str)

                if parsed_date:
                    remaining = line[match.end():].strip()

                    # Extract amount
                    amounts = re.findall(r'Rp?\s*([\d.,]+)', remaining, re.IGNORECASE)
                    if not amounts:
                        amounts = re.findall(r'([\d,]+\.?\d*)', remaining)

                    if amounts:
                        amount_str = amounts[0].replace(',', '').replace('.', '')
                        try:
                            amount = float(amount_str)
                            # Reconstruct to proper decimal (Indonesian format uses . as thousand sep)
                            if amount > 100000000:  # Likely needs decimal adjustment
                                amount = amount / 100

                            description = remaining
                            for amt in amounts:
                                description = description.replace(amt, '').strip()

                            transactions.append(Transaction(
                                date=parsed_date,
                                description=description.strip(),
                                amount=amount,
                                transaction_type='debit',
                                raw_text=line
                            ))
                        except ValueError:
                            pass

        return transactions

    def _parse_date(self, date_str: str) -> Optional[datetime]:
        """Parse various date formats."""
        # Indonesian month names
        id_months = {
            'januari': 1, 'februari': 2, 'maret': 3, 'april': 4,
            'mei': 5, 'juni': 6, 'juli': 7, 'agustus': 8,
            'september': 9, 'oktober': 10, 'november': 11, 'desember': 12,
            'jan': 1, 'feb': 2, 'mar': 3, 'apr': 4, 'may': 5, 'jun': 6,
            'jul': 7, 'aug': 8, 'sep': 9, 'oct': 10, 'nov': 11, 'dec': 12
        }

        try:
            # Try standard parsing first
            return date_parser.parse(date_str, dayfirst=True)
        except Exception:
            pass

        # Try Indonesian format: "15 Januari 2024"
        match = re.match(r'(\d{1,2})\s+(\w+)\s+(\d{4})', date_str)
        if match:
            day, month_name, year = match.groups()
            month = id_months.get(month_name.lower())
            if month:
                return datetime(int(year), month, int(day))

        return None


class LineBankParser(BankParser):
    """Parser for Line Bank PDF statements."""

    def bank_name(self) -> str:
        return "LINE Bank"

    def parse(self, pdf_path: str) -> list[Transaction]:
        transactions = []

        with pdfplumber.open(pdf_path) as pdf:
            for page in pdf.pages:
                # Line Bank typically uses clean table formats
                tables = page.extract_tables()
                if tables:
                    for table in tables:
                        transactions.extend(self._parse_table(table))
                else:
                    text = page.extract_text() or ""
                    transactions.extend(self._parse_text(text))

        return transactions

    def _parse_table(self, table: list) -> list[Transaction]:
        transactions = []

        for row in table:
            if not row or len(row) < 3:
                continue

            # Skip headers
            first_cell = str(row[0]).lower() if row[0] else ""
            if any(h in first_cell for h in ['tanggal', 'date', 'waktu', 'time']):
                continue

            try:
                # Line Bank format varies but typically: Date/Time | Description | Amount
                date_str = str(row[0]).strip()

                # Parse date
                parsed_date = self._parse_date(date_str)
                if not parsed_date:
                    continue

                description = str(row[1]).strip() if len(row) > 1 else ""

                # Find amount column
                amount = 0.0
                trans_type = 'debit'

                for i in range(2, len(row)):
                    cell = str(row[i]) if row[i] else ""
                    # Check for debit/credit indicators
                    if '-' in cell or 'db' in cell.lower():
                        trans_type = 'debit'
                    elif '+' in cell or 'cr' in cell.lower():
                        trans_type = 'credit'

                    # Extract number
                    amt_str = re.sub(r'[^\d]', '', cell)
                    if amt_str:
                        try:
                            amount = float(amt_str)
                            break
                        except ValueError:
                            pass

                if amount > 0 and description:
                    transactions.append(Transaction(
                        date=parsed_date,
                        description=description,
                        amount=amount,
                        transaction_type=trans_type,
                        raw_text=str(row)
                    ))
            except Exception:
                continue

        return transactions

    def _parse_text(self, text: str) -> list[Transaction]:
        transactions = []
        lines = text.split('\n')

        date_pattern = r'(\d{2}[/-]\d{2}[/-]\d{4}|\d{2}\s+\w+\s+\d{4})'

        for line in lines:
            match = re.search(date_pattern, line)
            if match:
                date_str = match.group(1)
                parsed_date = self._parse_date(date_str)

                if parsed_date:
                    remaining = line.replace(date_str, '').strip()

                    # Extract amount
                    amounts = re.findall(r'([\d.,]+)', remaining)
                    if amounts:
                        # Take the largest number as the amount
                        max_amount = 0
                        for amt_str in amounts:
                            try:
                                amt = float(amt_str.replace(',', '').replace('.', ''))
                                if amt > max_amount:
                                    max_amount = amt
                            except ValueError:
                                pass

                        if max_amount > 0:
                            description = remaining
                            for amt in amounts:
                                description = description.replace(amt, '').strip()

                            transactions.append(Transaction(
                                date=parsed_date,
                                description=description.strip(),
                                amount=max_amount,
                                transaction_type='debit',
                                raw_text=line
                            ))

        return transactions

    def _parse_date(self, date_str: str) -> Optional[datetime]:
        """Parse date string."""
        try:
            return date_parser.parse(date_str, dayfirst=True)
        except Exception:
            return None


class BudgetMatcher:
    """Matches transactions to budgets using keyword rules."""

    def __init__(self, db_path: str):
        self.db_path = db_path
        self.rules: list[tuple[int, str, list[str]]] = []
        self._load_rules()

    def _load_rules(self):
        """Load budget rules from database."""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        try:
            cursor.execute("""
                SELECT budget_id, keywords FROM budget_rules
                WHERE is_active = 1
                ORDER BY priority DESC
            """)

            for row in cursor.fetchall():
                budget_id = row[0]
                keywords = row[1].split(',')
                keywords = [k.strip().lower() for k in keywords if k.strip()]
                if keywords:
                    self.rules.append((budget_id, keywords))
        except sqlite3.OperationalError:
            # Table doesn't exist yet
            pass
        finally:
            conn.close()

    def match(self, description: str) -> Optional[int]:
        """Find matching budget_id for a transaction description."""
        desc_lower = description.lower()

        for budget_id, keywords in self.rules:
            for keyword in keywords:
                if keyword in desc_lower:
                    return budget_id

        return None


class ExpenseImporter:
    """Imports parsed transactions into the budget database as expenses."""

    def __init__(self, db_path: str):
        self.db_path = db_path
        self.matcher = BudgetMatcher(db_path)

    def import_transactions(
        self,
        transactions: list[Transaction],
        default_budget_id: Optional[int] = None,
        dry_run: bool = False
    ) -> dict:
        """Import transactions as expenses. Returns stats."""
        stats = {
            'total': len(transactions),
            'imported': 0,
            'skipped_credits': 0,
            'skipped_no_budget': 0,
            'duplicates': 0,
            'errors': 0
        }

        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        try:
            for tx in transactions:
                # Only import debits (expenses)
                if tx.transaction_type != 'debit':
                    stats['skipped_credits'] += 1
                    continue

                # Match to budget
                budget_id = self.matcher.match(tx.description)
                if budget_id is None:
                    budget_id = default_budget_id

                if budget_id is None:
                    stats['skipped_no_budget'] += 1
                    print(f"  [SKIP] No budget match: {tx.description[:50]}...")
                    continue

                # Check for duplicates (same date, amount, description)
                cursor.execute("""
                    SELECT id FROM expenses
                    WHERE date = ? AND amount = ? AND description = ?
                """, (tx.date.strftime('%Y-%m-%d'), tx.amount, tx.description))

                if cursor.fetchone():
                    stats['duplicates'] += 1
                    continue

                if not dry_run:
                    try:
                        cursor.execute("""
                            INSERT INTO expenses (budget_id, amount, description, date, created_at, updated_at)
                            VALUES (?, ?, ?, ?, datetime('now'), datetime('now'))
                        """, (budget_id, tx.amount, tx.description, tx.date.strftime('%Y-%m-%d')))

                        # Update budget spent_amount
                        cursor.execute("""
                            UPDATE budgets
                            SET spent_amount = spent_amount + ?,
                                updated_at = datetime('now')
                            WHERE id = ?
                        """, (tx.amount, budget_id))

                        stats['imported'] += 1
                        print(f"  [OK] {tx.date.strftime('%Y-%m-%d')} | Rp {tx.amount:,.0f} | {tx.description[:40]}")
                    except Exception as e:
                        stats['errors'] += 1
                        print(f"  [ERR] {e}")
                else:
                    stats['imported'] += 1
                    print(f"  [DRY] {tx.date.strftime('%Y-%m-%d')} | Rp {tx.amount:,.0f} | {tx.description[:40]}")

            if not dry_run:
                conn.commit()
        finally:
            conn.close()

        return stats


def detect_bank(pdf_path: str) -> Optional[str]:
    """Auto-detect bank from PDF content."""
    with pdfplumber.open(pdf_path) as pdf:
        first_page = pdf.pages[0].extract_text() or ""
        first_page_lower = first_page.lower()

        if 'bca' in first_page_lower or 'bank central asia' in first_page_lower:
            return 'bca'
        elif 'jago' in first_page_lower or 'pt bank jago' in first_page_lower:
            return 'jago'
        elif 'line bank' in first_page_lower or 'pt bank keb hana' in first_page_lower:
            return 'linebank'

    return None


def get_parser(bank: str) -> BankParser:
    """Get parser instance for bank type."""
    parsers = {
        'bca': BCAParser(),
        'jago': JAGOParser(),
        'linebank': LineBankParser(),
    }

    parser = parsers.get(bank.lower())
    if not parser:
        raise ValueError(f"Unknown bank: {bank}. Supported: {', '.join(parsers.keys())}")

    return parser


def main():
    parser = argparse.ArgumentParser(
        description='Parse bank statement PDF and import expenses to Budget Manager'
    )
    parser.add_argument('pdf_path', help='Path to the PDF bank statement')
    parser.add_argument('--bank', '-b', choices=['bca', 'jago', 'linebank', 'auto'],
                       default='auto', help='Bank type (default: auto-detect)')
    parser.add_argument('--db', '-d', default='./budget.db',
                       help='Path to SQLite database (default: ./budget.db)')
    parser.add_argument('--default-budget', '-B', type=int,
                       help='Default budget ID for unmatched transactions')
    parser.add_argument('--dry-run', '-n', action='store_true',
                       help='Parse and match but do not insert into database')
    parser.add_argument('--list-transactions', '-l', action='store_true',
                       help='Only list parsed transactions, do not import')

    args = parser.parse_args()

    # Validate PDF exists
    pdf_path = Path(args.pdf_path)
    if not pdf_path.exists():
        print(f"Error: File not found: {pdf_path}")
        return 1

    # Detect or use specified bank
    bank = args.bank
    if bank == 'auto':
        bank = detect_bank(str(pdf_path))
        if not bank:
            print("Error: Could not auto-detect bank. Please specify with --bank")
            return 1
        print(f"Detected bank: {bank.upper()}")

    # Get parser and parse
    try:
        bank_parser = get_parser(bank)
        print(f"Parsing {bank_parser.bank_name()} statement: {pdf_path}")
        transactions = bank_parser.parse(str(pdf_path))
        print(f"Found {len(transactions)} transactions")
    except Exception as e:
        print(f"Error parsing PDF: {e}")
        return 1

    if not transactions:
        print("No transactions found in PDF")
        return 0

    # List only mode
    if args.list_transactions:
        print("\nTransactions:")
        print("-" * 80)
        for tx in transactions:
            print(f"{tx.date.strftime('%Y-%m-%d')} | {tx.transaction_type:6} | "
                  f"Rp {tx.amount:>12,.0f} | {tx.description[:40]}")
        return 0

    # Import to database
    print("\nImporting expenses...")
    importer = ExpenseImporter(args.db)
    stats = importer.import_transactions(
        transactions,
        default_budget_id=args.default_budget,
        dry_run=args.dry_run
    )

    # Print summary
    print("\n" + "=" * 40)
    print("Import Summary:")
    print(f"  Total transactions: {stats['total']}")
    print(f"  Imported:          {stats['imported']}")
    print(f"  Skipped (credits): {stats['skipped_credits']}")
    print(f"  Skipped (no budget): {stats['skipped_no_budget']}")
    print(f"  Duplicates:        {stats['duplicates']}")
    print(f"  Errors:            {stats['errors']}")

    return 0


if __name__ == '__main__':
    exit(main())
