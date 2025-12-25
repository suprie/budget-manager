# Budget Manager

A personal financial manager using envelope (zero-sum) budgeting. Track your money across different pockets (accounts) and allocate funds to budget envelopes for spending.

## Quick Start

```bash
# Run the Go server
cd server && go run ./cmd/api

# Run Android app (requires Android Studio)
cd android && ./gradlew installDebug

# Run iOS app (requires Xcode on macOS)
cd iOS && open BudgetManager.xcodeproj
```

## Architecture

```
budget-manager/
├── server/              # Go REST API backend (SQLite)
├── android/             # Jetpack Compose Android app
├── iOS/                 # SwiftUI iOS app
└── README.md
```

| Component | Tech Stack |
|-----------|------------|
| **Server** | Go 1.21+, SQLite, net/http |
| **Android** | Kotlin, Jetpack Compose, Material 3, Navigation |
| **iOS** | Swift, SwiftUI, Combine |

## Concepts

### Zero-Sum Envelope Budgeting

```
┌─────────────┐     allocate      ┌─────────────┐      spend       ┌─────────────┐
│   Pocket    │ ───────────────▶  │   Budget    │ ───────────────▶ │   Expense   │
│  (source)   │   deducts from    │ (envelope)  │   deducts from   │ (spending)  │
│             │      pocket       │             │      budget      │             │
└─────────────┘                   └─────────────┘                  └─────────────┘
     $1000                             $300                             $50
       │                                 │                                │
       ▼                                 ▼                                ▼
  Balance: $700                   Remaining: $250                  Recorded expense
```

- **Pocket**: Source of money (bank account, cash, e-wallet)
- **Budget**: Envelope for allocating funds from a pocket (monthly period)
- **Expense**: Spending transaction against a budget envelope
- **Period**: Monthly identifier (e.g., "2024-12")

**Flow:**
1. Add money to a **Pocket** (e.g., $1000 to "Bank Account")
2. Create a **Budget** envelope from that pocket (e.g., $300 for "Groceries")
3. Pocket balance reduces by allocated amount ($1000 → $700)
4. Record **Expenses** against the budget (e.g., $50 for "Weekly shopping")
5. Budget remaining reduces ($300 → $250)
6. Delete budget → unspent funds return to pocket

---

## Server (Go)

### Prerequisites

- Go 1.21+
- GCC (for SQLite driver)

### Run

```bash
cd server
go mod tidy
go run ./cmd/api
# Server starts at http://localhost:8080
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8080` | Server port |
| `DB_PATH` | `./budget.db` | SQLite database path |
| `JWT_SECRET` | (default) | JWT signing secret (change in production!) |

### API Reference

#### Authentication (Public)
```bash
POST   /api/auth/register              # Register new user
POST   /api/auth/login                 # Login, returns JWT token
```

#### Authentication (Protected)
```bash
GET    /api/auth/me                    # Get current user
```

> **Note:** All other endpoints below require authentication.
> Include `Authorization: Bearer <token>` header in requests.

#### Pockets
```bash
POST   /api/pockets                    # Create pocket
GET    /api/pockets                    # List all pockets
GET    /api/pockets/{id}               # Get pocket
PUT    /api/pockets/{id}               # Update pocket
DELETE /api/pockets/{id}               # Delete pocket
POST   /api/pockets/{id}/add-funds     # Add funds
```

#### Budgets
```bash
POST   /api/budgets                    # Create budget
GET    /api/budgets                    # List all budgets
GET    /api/budgets/{id}               # Get budget
PUT    /api/budgets/{id}               # Update budget
DELETE /api/budgets/{id}               # Delete budget
GET    /api/budgets/{id}/remaining     # Get remaining amount
GET    /api/budgets/by-period?period=2024-12      # Filter by period
GET    /api/budgets/summary?period=2024-12        # Period summary
GET    /api/pockets/{pocket_id}/budgets           # Budgets by pocket
```

#### Expenses
```bash
POST   /api/expenses                   # Create expense
GET    /api/expenses                   # List all expenses
GET    /api/expenses/{id}              # Get expense
PUT    /api/expenses/{id}              # Update expense
DELETE /api/expenses/{id}              # Delete expense
GET    /api/expenses/by-date-range?start_date=2024-12-01&end_date=2024-12-31
GET    /api/budgets/{budget_id}/expenses          # Expenses by budget
```

### Example Usage

```bash
# 1. Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "secret123", "name": "John Doe"}'

# Response: {"token": "eyJ...", "user": {...}}

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "secret123"}'

# Save the token for subsequent requests
TOKEN="eyJ..."

# 3. Create a pocket with $5000
curl -X POST http://localhost:8080/api/pockets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name": "Bank Account", "description": "Main checking", "balance": 5000}'

# 4. Create a budget envelope for $500
curl -X POST http://localhost:8080/api/budgets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name": "Groceries", "pocket_id": 1, "allocated_amount": 500, "period": "2024-12"}'

# 5. Record an expense of $50
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"budget_id": 1, "amount": 50.00, "description": "Weekly shopping", "date": "2024-12-24"}'

# 6. Check budget summary
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/budgets/summary?period=2024-12
```

---

## Android App

### Prerequisites

- Android Studio Hedgehog (2023.1.1)+
- JDK 17
- Android SDK 34

### Run

```bash
cd android

# Build APK
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Or open in Android Studio
```

### Project Structure

```
android/app/src/main/java/com/budgetmanager/
├── MainActivity.kt
├── di/AppContainer.kt           # Dependency injection
├── domain/
│   ├── entity/                  # Pocket, Budget, Expense
│   ├── repository/              # Repository interfaces
│   └── interactor/              # Business logic
├── data/
│   ├── repository/              # In-memory implementations
│   └── api/                     # Retrofit API client & DTOs
└── ui/
    ├── theme/                   # Material 3 theming
    ├── navigation/              # Navigation graph
    ├── screen/                  # Compose screens
    └── viewmodel/               # ViewModels
```

### Screens

| Screen | Description |
|--------|-------------|
| Login | Email/password sign in |
| Register | Create new account |
| Pocket List | View all pockets, total balance |
| Pocket Detail | View budgets, add funds |
| Budget List | Period summary, all budgets |
| Budget Detail | Expenses, spending progress |
| Add Pocket/Budget/Expense | Form screens |

### Tech Stack

- **Kotlin** - Language
- **Jetpack Compose** - UI framework
- **Material 3** - Design system
- **Navigation Compose** - Screen navigation
- **ViewModel + StateFlow** - State management
- **Coroutines** - Async operations
- **Retrofit + OkHttp** - Networking (API mode)
- **Gson** - JSON serialization

---

## iOS App

### Prerequisites

- macOS
- Xcode 15+
- Tuist (optional, for project generation)

### Run

```bash
cd iOS

# Option 1: Open directly
open BudgetManager.xcodeproj

# Option 2: Generate with Tuist
tuist generate && open BudgetManager.xcworkspace
```

Then in Xcode: Select simulator → Press `Cmd + R`

### Project Structure

```
iOS/Sources/
├── App/BudgetManagerApp.swift   # Entry point
├── Domain/
│   ├── Entities/                # Pocket, Budget, Expense
│   ├── Interfaces/              # Repository protocols
│   └── Interactors/             # Business logic
├── Data/
│   ├── Repositories/            # In-memory implementations
│   └── Network/                 # API client, endpoints, errors
└── Presentation/
    ├── ViewModels/              # MVVM view models
    └── Views/                   # SwiftUI views
```

### Screens

| Screen | Description |
|--------|-------------|
| Login | Email/password sign in |
| Register | Create new account |
| Pocket List | TabView, all pockets |
| Pocket Detail | Budgets from pocket |
| Budget List | Period budgets, summary |
| Budget Detail | Expenses, progress bar |
| Add Pocket/Budget/Expense | Sheet forms |

### Tech Stack

- **Swift** - Language
- **SwiftUI** - UI framework
- **Combine** - Reactive programming
- **@MainActor** - Thread safety
- **URLSession** - Networking (API mode)
- **async/await** - Modern concurrency

---

## Development

### Database

The server auto-creates SQLite tables on startup:

```sql
users    (id, email, password_hash, name, created_at, updated_at)
pockets  (id, name, description, balance, created_at, updated_at)
budgets  (id, name, description, pocket_id, allocated_amount, spent_amount, period, ...)
expenses (id, budget_id, amount, description, date, created_at, updated_at)
```

### Switching to PostgreSQL

1. Replace `github.com/mattn/go-sqlite3` with `github.com/lib/pq`
2. Update connection string: `postgres://user:pass@localhost/dbname`
3. Change `AUTOINCREMENT` → `SERIAL`

### Connecting Apps to Server

Both mobile apps support API integration with a toggle to switch between in-memory and API storage.

**Android:**
- Set `USE_API_BACKEND = true` in `di/AppContainer.kt`
- API client configured in `data/api/ApiClient.kt`
- Default API URL: `http://localhost:8080`

**iOS:**
- Set `useAPIBackend = true` in `App/BudgetManagerApp.swift`
- API client configured in `Data/Network/APIClient.swift`
- Default API URL: `http://localhost:8080`

**API Implementation Details:**
- Both apps use `Long`/`Int64` IDs (matching server)
- JWT authentication via `Authorization: Bearer <token>` header
- Date format: `yyyy-MM-dd` (server format)

**Android API Stack:**
- Retrofit 2.9 + OkHttp 4.12
- Gson for JSON serialization
- Coroutines for async operations

**iOS API Stack:**
- Native URLSession
- async/await for network calls
- Codable for JSON serialization

---

## Troubleshooting

See [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) for common build errors and solutions.

---

## License

MIT
