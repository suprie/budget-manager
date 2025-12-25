# Budget Manager - Claude Code Memory

## Project Overview

Multi-platform envelope budgeting application with:
- **Backend**: Go REST API with SQLite
- **Android**: Kotlin + Jetpack Compose + Material 3
- **iOS**: Swift + SwiftUI

## Android Architecture

### Patterns Used
- **MVVM**: ViewModels with StateFlow for reactive UI
- **Clean Architecture**: Domain → Data → Presentation layers
- **Manual DI**: `AppContainer` singleton for dependency injection
- **Repository Pattern**: Abstraction over data sources

### Key Directories
```
android/app/src/main/java/com/budgetmanager/
├── di/                 # Dependency injection (AppContainer)
├── domain/
│   ├── entity/         # Data models (Pocket, Budget, Expense)
│   ├── interactor/     # Business logic
│   └── repository/     # Repository interfaces
├── data/
│   ├── api/            # Retrofit API clients
│   └── repository/     # Repository implementations
└── ui/
    ├── component/      # Reusable composables
    ├── navigation/     # NavRoutes, AppNavigation
    ├── screen/         # Screen composables
    └── viewmodel/      # ViewModels with State classes
```

---

## Troubleshooting

### Compose Material 3 Issues

#### `Unresolved reference: HorizontalDivider`
**Problem**: `HorizontalDivider` was introduced in Material3 1.2.0+. Older versions use `Divider`.

**Solution**: Use `Divider` instead of `HorizontalDivider` for compatibility:
```kotlin
// Instead of:
HorizontalDivider()

// Use:
Divider()
```

#### `Unresolved reference: VerticalDivider`
**Solution**: Use `Divider` with modifier:
```kotlin
// Instead of:
VerticalDivider()

// Use:
Divider(
    modifier = Modifier
        .fillMaxHeight()
        .width(1.dp)
)
```

#### `LinearProgressIndicator` - None of the following functions can be called
**Problem**: Material3 1.2.0+ changed `progress` from `Float` to `() -> Float` lambda. Older versions use `Float` directly.

**Solution**: Use `Float` directly for compatibility:
```kotlin
// Instead of (new API):
LinearProgressIndicator(
    progress = { progressValue },
    ...
)

// Use (compatible):
LinearProgressIndicator(
    progress = progressValue,
    ...
)
```

---

### Network Issues

#### Cleartext HTTP traffic not permitted
**Problem**: Android 9+ blocks HTTP (non-HTTPS) by default.

**Solution**: Add network security config:

1. Create `res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">localhost</domain>
    </domain-config>
</network-security-config>
```

2. Add to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />

<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

---

### Navigation Issues

#### Adding new screens
1. Add route to `NavRoutes.kt`
2. Add `composable()` in `AppNavigation.kt`
3. Create ViewModel in `ui/viewmodel/`
4. Create Screen in `ui/screen/`

#### Bottom navigation visibility
Update `showBottomBar` in `AppNavigation.kt`:
```kotlin
val showBottomBar = currentRoute in listOf(
    NavRoutes.Home.route,
    NavRoutes.PocketList.route,
    NavRoutes.BudgetList.route
)
```

---

### ViewModel Patterns

#### Standard ViewModel structure:
```kotlin
data class MyScreenState(
    val data: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MyScreenViewModel(
    private val interactor: MyInteractor
) : ViewModel() {
    private val _state = MutableStateFlow(MyScreenState())
    val state: StateFlow<MyScreenState> = _state.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val data = interactor.getData()
                _state.value = _state.value.copy(data = data, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}
```

---

### Common Import Issues

#### Material 3 components
```kotlin
import androidx.compose.material3.*  // Wildcard import for most M3 components
```

#### Icons
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
// etc.
```

#### Navigation
```kotlin
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
```

---

### Date/Time Handling

Use `java.time` classes:
```kotlin
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Current period (yyyy-MM format)
LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

// Display date
val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
date.format(formatter)
```

---

## Code Conventions

### Naming
- ViewModels: `*ViewModel.kt`
- Screens: `*Screen.kt`
- State classes: `*State` (inside ViewModel file)
- Interactors: `*Interactor.kt`

### Currency formatting
Use the shared utility:
```kotlin
import com.budgetmanager.ui.component.formatCurrency

Text(text = formatCurrency(amount))
```

### Loading indicator
```kotlin
import com.budgetmanager.ui.component.LoadingIndicator

if (state.isLoading) {
    LoadingIndicator()
}
```

---

## API Backend Configuration

### Enabling API Backend
In `AppContainer.kt`:
```kotlin
const val USE_API_BACKEND = true  // Set to true to use Go backend
```

### Emulator Base URL
For Android emulator, use `10.0.2.2` to access host machine's localhost:
```kotlin
// In ApiClient.kt or via Settings
private var baseUrl: String = "http://10.0.2.2:8080/"
```

**IMPORTANT**: `localhost` on Android emulator refers to the emulator itself, NOT your computer!
- Emulator → Host machine: use `10.0.2.2`
- Physical device → Computer on same WiFi: use computer's local IP (e.g., `192.168.1.x`)

### API Repositories
When `USE_API_BACKEND = true`:
- `ApiAuthRepository` → calls `/api/auth/login`, `/api/auth/register`
- `ApiPocketRepository` → calls `/api/pockets`
- `ApiBudgetRepository` → calls `/api/budgets`
- `ApiExpenseRepository` → calls `/api/expenses`

### Initialization
In `MainActivity.onCreate()`:
```kotlin
AppSettings.init(applicationContext)
AppContainer.init(applicationContext)  // Required for ApiAuthRepository
```

---

## Currency Settings

### Supported Currencies
Defined in `AppSettings.kt`:
- USD ($) - US Dollar
- IDR (Rp) - Indonesian Rupiah
- EUR (€) - Euro
- GBP (£) - British Pound
- JPY (¥) - Japanese Yen
- SGD (S$) - Singapore Dollar

### Changing Currency
Via Settings screen or programmatically:
```kotlin
AppSettings.setCurrency(SupportedCurrency.IDR)
```

---

## API Endpoints (Server)

### Public
- `POST /api/auth/register`
- `POST /api/auth/login`

### Protected (requires Bearer token)
- `GET/POST /api/pockets`
- `GET/PUT/DELETE /api/pockets/{id}`
- `POST /api/pockets/{id}/add-funds`
- `GET/POST /api/budgets`
- `GET /api/budgets/by-period?period=yyyy-MM`
- `GET /api/budgets/summary?period=yyyy-MM`
- `GET/POST /api/expenses`
- `GET /api/expenses/by-date-range?start_date=...&end_date=...`
