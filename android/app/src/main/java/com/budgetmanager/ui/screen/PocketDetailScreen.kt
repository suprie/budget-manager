package com.budgetmanager.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.budgetmanager.domain.entity.Budget
import com.budgetmanager.ui.component.LoadingIndicator
import com.budgetmanager.ui.component.formatCurrency
import com.budgetmanager.ui.viewmodel.PocketDetailViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketDetailScreen(
    viewModel: PocketDetailViewModel,
    onNavigateBack: () -> Unit,
    onBudgetClick: (Long) -> Unit,
    onAddBudgetClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showAddFundsDialog by remember { mutableStateOf(false) }
    var addFundsAmount by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.pocket?.name ?: "Pocket Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddFundsDialog = true }) {
                        Icon(Icons.Default.AttachMoney, contentDescription = "Add Funds")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBudgetClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.pocket?.let { pocket ->
                    item {
                        PocketSummaryCard(
                            balance = pocket.balance,
                            totalAllocated = state.totalAllocated,
                            totalSpent = state.totalSpent
                        )
                    }
                }

                item {
                    Text(
                        text = "Budget Envelopes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (state.budgets.isEmpty()) {
                    item {
                        Text(
                            text = "No budgets yet. Tap + to create one.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(state.budgets, key = { it.id }) { budget ->
                        BudgetCard(
                            budget = budget,
                            onClick = { onBudgetClick(budget.id) },
                            onDelete = { viewModel.deleteBudget(budget.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddFundsDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddFundsDialog = false
                addFundsAmount = ""
            },
            title = { Text("Add Funds") },
            text = {
                OutlinedTextField(
                    value = addFundsAmount,
                    onValueChange = { addFundsAmount = it },
                    label = { Text("Amount") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    addFundsAmount.toDoubleOrNull()?.let { amount ->
                        if (amount > 0) {
                            viewModel.addFunds(amount)
                        }
                    }
                    showAddFundsDialog = false
                    addFundsAmount = ""
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddFundsDialog = false
                    addFundsAmount = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PocketSummaryCard(
    balance: Double,
    totalAllocated: Double,
    totalSpent: Double
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Available Balance")
                Text(
                    text = formatCurrency(balance),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Allocated")
                Text(
                    text = formatCurrency(totalAllocated),
                    color = Color(0xFFFF9800)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Spent")
                Text(
                    text = formatCurrency(totalSpent),
                    color = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
fun BudgetCard(
    budget: Budget,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val progress = if (budget.allocatedAmount > 0) {
        (budget.spentAmount / budget.allocatedAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

    val progressColor = when {
        progress >= 1f -> Color(0xFFD32F2F)
        progress >= 0.8f -> Color(0xFFFF9800)
        else -> Color(0xFF2E7D32)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatCurrency(budget.remainingAmount),
                        color = if (budget.remainingAmount >= 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    )
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ${formatCurrency(budget.spentAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "of ${formatCurrency(budget.allocatedAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Budget") },
            text = { Text("Are you sure you want to delete '${budget.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
