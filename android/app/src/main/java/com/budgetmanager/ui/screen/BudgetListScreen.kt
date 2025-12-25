package com.budgetmanager.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.budgetmanager.ui.component.LoadingIndicator
import com.budgetmanager.ui.component.formatCurrency
import com.budgetmanager.ui.viewmodel.BudgetListViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetListScreen(
    viewModel: BudgetListViewModel,
    onBudgetClick: (Long) -> Unit,
    onAddBudgetClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBudgets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
                state.summary?.let { summary ->
                    item {
                        BudgetSummaryCard(
                            period = state.currentPeriod,
                            totalAllocated = summary.totalAllocated,
                            totalSpent = summary.totalSpent,
                            totalRemaining = summary.totalRemaining,
                            unallocatedFunds = summary.unallocatedFunds
                        )
                    }
                }

                item {
                    Text(
                        text = "Budgets",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (state.budgets.isEmpty()) {
                    item {
                        Text(
                            text = "No budgets for this period",
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
}

@Composable
private fun BudgetSummaryCard(
    period: String,
    totalAllocated: Double,
    totalSpent: Double,
    totalRemaining: Double,
    unallocatedFunds: Double
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
                Text("Period")
                Text(
                    text = period,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Allocated")
                Text(text = formatCurrency(totalAllocated))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Spent")
                Text(
                    text = formatCurrency(totalSpent),
                    color = Color(0xFFFF9800)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Remaining")
                Text(
                    text = formatCurrency(totalRemaining),
                    fontWeight = FontWeight.Bold,
                    color = if (totalRemaining >= 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Unallocated Funds")
                Text(
                    text = formatCurrency(unallocatedFunds),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
