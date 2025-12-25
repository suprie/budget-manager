# Troubleshooting

Common build errors and their solutions.

## Android

### OutOfMemoryError During Build

**Error:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Fix:** Add to `android/gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
```

---

### Missing Launcher Icons

**Error:**
```
AAPT: error: resource mipmap/ic_launcher not found
AAPT: error: resource mipmap/ic_launcher_round not found
```

**Fix:** Ensure these files exist:
- `res/mipmap-*/ic_launcher.xml`
- `res/mipmap-*/ic_launcher_round.xml`
- `res/drawable/ic_launcher_foreground.xml`
- `res/values/ic_launcher_background.xml`

---

### Compose Material 3 API Compatibility

The project uses `compose-bom:2023.10.01`. Some APIs changed in newer versions.

#### LinearProgressIndicator Wrong Signature

**Error:**
```
None of the following functions can be called with the arguments supplied:
public fun LinearProgressIndicator(progress: Float, ...)
```

**Files:** `BudgetDetailScreen.kt`, `PocketDetailScreen.kt`

**Fix:** Change lambda syntax to direct Float:
```kotlin
// Before (newer API)
LinearProgressIndicator(
    progress = { state.spentPercentage },
    ...
)

// After (older API)
LinearProgressIndicator(
    progress = state.spentPercentage.coerceIn(0f, 1f),
    ...
)
```

---

#### HorizontalDivider Unresolved

**Error:**
```
Unresolved reference: HorizontalDivider
```

**File:** `BudgetListScreen.kt`

**Fix:** Rename to older API name:
```kotlin
// Before (newer API)
HorizontalDivider()

// After (older API)
Divider()
```

---

#### Icons.AutoMirrored Unresolved

**Error:**
```
Unresolved reference: AutoMirrored
```

**File:** `RegisterScreen.kt`

**Fix:** Use default icons:
```kotlin
// Before (newer API)
import androidx.compose.material.icons.automirrored.filled.ArrowBack
Icon(Icons.AutoMirrored.Filled.ArrowBack, ...)

// After (older API)
import androidx.compose.material.icons.filled.ArrowBack
Icon(Icons.Default.ArrowBack, ...)
```

---

### Alternative: Update Compose BOM

Instead of fixing individual APIs, update to a newer BOM in `app/build.gradle.kts`:

```kotlin
// From
implementation(platform("androidx.compose:compose-bom:2023.10.01"))

// To
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
```

---

## Server (Go)

### GCC Not Found

**Error:**
```
cgo: C compiler "gcc" not found
```

**Fix:**

Linux (Debian/Ubuntu):
```bash
sudo apt install build-essential
```

Linux (Arch):
```bash
sudo pacman -S base-devel
```

macOS:
```bash
xcode-select --install
```

---

### SQLite Driver Error

**Error:**
```
undefined: sqlite3.ErrNo
```

**Fix:** Enable CGO when building:
```bash
CGO_ENABLED=1 go build ./cmd/api
```

---

### Database Locked

**Error:**
```
database is locked
```

**Fix:** Ensure only one instance of the server is running, or use a different database file:
```bash
DB_PATH=./budget2.db go run ./cmd/api
```

---

## iOS

### Tuist Not Found

**Error:**
```
command not found: tuist
```

**Fix:** Install Tuist:
```bash
curl -Ls https://install.tuist.io | bash
```

Or open the project directly without Tuist:
```bash
open iOS/BudgetManager.xcodeproj
```

---

### Swift Concurrency Warnings

**Warning:**
```
Converting non-sendable function value may introduce data races
```

**Fix:** These are warnings, not errors. The app uses `@MainActor` for thread safety. To suppress, add to build settings:
```
SWIFT_STRICT_CONCURRENCY = minimal
```
