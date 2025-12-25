package com.budgetmanager.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.budgetmanager.domain.entity.Budget
import com.budgetmanager.domain.entity.Pocket
import com.budgetmanager.ui.component.LoadingIndicator
import com.budgetmanager.ui.component.formatCurrency
import com.budgetmanager.ui.viewmodel.QuickAddExpenseViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddExpenseScreen(
    viewModel: QuickAddExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showPocketDropdown by remember { mutableStateOf(false) }
    var showBudgetDropdown by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.amount,
                    onValueChange = viewModel::updateAmount,
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.date.format(dateFormatter),
                    onValueChange = { },
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    }
                )

                // Pocket Dropdown
                ExposedDropdownMenuBox(
                    expanded = showPocketDropdown,
                    onExpandedChange = { showPocketDropdown = it }
                ) {
                    OutlinedTextField(
                        value = state.selectedPocket?.name ?: "Select Pocket",
                        onValueChange = { },
                        label = { Text("Pocket") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPocketDropdown)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = showPocketDropdown,
                        onDismissRequest = { showPocketDropdown = false }
                    ) {
                        state.pockets.forEach { pocket ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(pocket.name)
                                        Text(
                                            text = formatCurrency(pocket.balance),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.selectPocket(pocket)
                                    showPocketDropdown = false
                                }
                            )
                        }
                    }
                }

                // Budget Dropdown
                ExposedDropdownMenuBox(
                    expanded = showBudgetDropdown,
                    onExpandedChange = { showBudgetDropdown = it }
                ) {
                    OutlinedTextField(
                        value = state.selectedBudget?.name ?: "Uncategorized",
                        onValueChange = { },
                        label = { Text("Budget") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBudgetDropdown)
                        },
                        supportingText = {
                            if (state.selectedBudget == null && state.selectedPocket != null) {
                                Text(
                                    "Will be added to Uncategorized budget",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = showBudgetDropdown,
                        onDismissRequest = { showBudgetDropdown = false }
                    ) {
                        // Add Uncategorized option
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Uncategorized")
                                    Text(
                                        text = "Default for unassigned expenses",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                viewModel.selectBudget(
                                    Budget(
                                        id = 0,
                                        name = "Uncategorized",
                                        pocketId = state.selectedPocket?.id ?: 0,
                                        allocatedAmount = 0.0,
                                        period = state.currentPeriod
                                    )
                                )
                                showBudgetDropdown = false
                            }
                        )

                        if (state.filteredBudgets.isNotEmpty()) {
                            Divider()
                        }

                        state.filteredBudgets.forEach { budget ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(budget.name)
                                        Text(
                                            text = "Remaining: ${formatCurrency(budget.remainingAmount)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (budget.remainingAmount >= 0)
                                                Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.selectBudget(budget)
                                    showBudgetDropdown = false
                                }
                            )
                        }
                    }
                }

                // Selected Budget Info Card
                state.selectedBudget?.let { budget ->
                    if (budget.id != 0L) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Allocated")
                                    Text(formatCurrency(budget.allocatedAmount))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Spent")
                                    Text(formatCurrency(budget.spentAmount))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Remaining")
                                    Text(
                                        text = formatCurrency(budget.remainingAmount),
                                        color = if (budget.remainingAmount >= 0)
                                            Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }
                    }
                }

                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = viewModel::saveExpense,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.selectedPocket != null
                ) {
                    Text("Save Expense")
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date.toEpochDay() * 24 * 60 * 60 * 1000
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.updateDate(selectedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
