package com.example.datatruth.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datatruth.models.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullDashboard(
    uiState: EnhancedUiState,
    logo: @Composable () -> Unit,
    onCaptureUsage: () -> Unit,
    onFetchProvider: () -> Unit,
    onSetupPlan: (String, Double, Int) -> Unit,
    onRefresh: () -> Unit,
    onClearMessages: () -> Unit
)
 {
    var showSetupDialog by remember { mutableStateOf(false) }

     Scaffold(
         topBar = {
             TopAppBar(
                 title = {
                     Box(
                         modifier = Modifier.fillMaxWidth(),
                         contentAlignment = Alignment.Center
                     ) {
                         Column(
                             horizontalAlignment = Alignment.CenterHorizontally
                         ) {
                             AnimatedVisibility(
                                 visible = true,
                                 enter = fadeIn() + scaleIn(initialScale = 0.9f)
                             ) {
                                 logo()
                             }
                         }
                     }
                 },
                 colors = TopAppBarDefaults.topAppBarColors(
                     containerColor = MaterialTheme.colorScheme.primaryContainer,
                     titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                 )
             )
         }
     )

     { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Messages
                item {
                    AnimatedVisibility(
                        visible = uiState.error != null || uiState.successMessage != null,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        MessageBanner(
                            message = uiState.error ?: uiState.successMessage ?: "",
                            isError = uiState.error != null,
                            onDismiss = onClearMessages
                        )
                    }
                }

                // Quick Actions
                item {
                    QuickActionsCard(
                        onCaptureUsage = onCaptureUsage,
                        onFetchProvider = onFetchProvider,
                        onRefresh = onRefresh,
                        isLoading = uiState.isLoading
                    )
                }

                // Main Stats
                if (uiState.usageSummary != null && uiState.dataPlan != null) {
                    item {
                        MainStatsCard(
                            summary = uiState.usageSummary,
                            plan = uiState.dataPlan
                        )
                    }
                } else {
                    item {
                        SetupPromptCard(
                            onSetup = { showSetupDialog = true }
                        )
                    }
                }

                // Current Device Reading
                if (uiState.currentDeviceUsage != null) {
                    item {
                        CurrentUsageCard(uiState.currentDeviceUsage)
                    }
                }

                // Provider vs Device Comparison
                if (uiState.providerReport != null && uiState.currentDeviceUsage != null) {
                    item {
                        ComparisonCard(
                            deviceUsage = uiState.currentDeviceUsage.totalBytes,
                            providerUsage = uiState.providerReport.reportedDataBytes
                        )
                    }
                }

                // Discrepancies
                if (uiState.recentDiscrepancies.isNotEmpty()) {
                    item {
                        SectionHeader("🚨 Discrepancies Detected")
                    }
                    items(uiState.recentDiscrepancies) { discrepancy ->
                        DiscrepancyCard(discrepancy)
                    }
                }

                // Usage History
                if (uiState.allUsageHistory.isNotEmpty()) {
                    item {
                        SectionHeader("📈 Recent Readings")
                    }
                    items(uiState.allUsageHistory.take(5)) { usage ->
                        UsageHistoryItem(usage)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Loading Overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        strokeWidth = 6.dp
                    )
                }
            }
        }
    }

    if (showSetupDialog) {
        SetupPlanDialog(
            onDismiss = { showSetupDialog = false },
            onConfirm = { provider, limit, day ->
                onSetupPlan(provider, limit, day)
                showSetupDialog = false
            }
        )
    }
}

@Composable
fun MessageBanner(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isError)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isError) "❌" else "✅",
                    fontSize = 24.sp
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    onCaptureUsage: () -> Unit,
    onFetchProvider: () -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    icon = "📱",
                    label = "Capture\nUsage",
                    onClick = onCaptureUsage,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )
                ActionButton(
                    icon = "📡",
                    label = "Fetch\nProvider",
                    onClick = onFetchProvider,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )
                ActionButton(
                    icon = "🔄",
                    label = "Refresh\nAll",
                    onClick = onRefresh,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 28.sp)
            Text(
                label,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun MainStatsCard(
    summary: UsageSummaryModel,
    plan: DataPlanModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (summary.hasDiscrepancy)
                MaterialTheme.colorScheme.errorContainer
            else if (summary.percentageUsed > 80)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Data Usage",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = plan.providerName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${summary.daysRemainingInCycle}\ndays",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 14.sp
                    )
                }
            }

            // Progress
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { (summary.percentageUsed / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        strokeCap = StrokeCap.Round,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${summary.currentCycleUsage.formatToGB().roundTo(2)} GB"
                        ,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${(summary.dataLimit ?: 0L).formatToGB().roundTo(2)} GB",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                }

                Text(
                    text = "${summary.percentageUsed.roundTo(1)}% used • Daily avg: ${summary.averageDailyUsage.formatToMB().toInt()} MB",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }

            // Discrepancy Warning
            if (summary.hasDiscrepancy) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 24.sp)
                        Column {
                            Text(
                                "Discrepancy Detected!",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onError
                            )
                            Text(
                                "Difference: ${kotlin.math.abs(summary.discrepancyAmount).formatToMB().toInt()} MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentUsageCard(usage: DataUsageModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "📱 Device Reading",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                UsageItem("Mobile", usage.mobileDataBytes)
                UsageItem("WiFi", usage.wifiDataBytes)
                UsageItem("Total", usage.totalBytes)
            }
        }
    }
}

@Composable
fun UsageItem(label: String, bytes: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "${bytes.formatToMB().roundTo(0).toInt()} MB",
            fontWeight = FontWeight.Bold
        )

        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ComparisonCard(deviceUsage: Long, providerUsage: Long) {
    val difference = deviceUsage - providerUsage
    val isMatch = kotlin.math.abs(difference) < (providerUsage * 0.05)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isMatch)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (isMatch) "✅ Data Match!" else "⚠️ Discrepancy Found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ComparisonItem("Your Device", deviceUsage)
                Text("VS", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                ComparisonItem("Provider", providerUsage)
            }

            if (!isMatch) {
                Text(
                    "Difference: ${kotlin.math.abs(difference).formatToMB().toInt()} MB",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun ComparisonItem(label: String, bytes: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "${bytes.formatToGB().roundTo(2)} GB",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(kotlin.time.ExperimentalTime::class)
@Composable
fun UsageHistoryItem(usage: DataUsageModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "${usage.totalBytes.formatToMB().toInt()} MB",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    usage.timestamp.toString().substring(0, 16),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "📊",
                fontSize = 24.sp
            )
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SetupPromptCard(onSetup: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("🎯", fontSize = 64.sp)
            Text(
                "Setup Your Data Plan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Configure your plan to start monitoring",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onSetup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Setup Plan", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPlanDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int) -> Unit
) {
    var provider by remember { mutableStateOf("") }
    var dataLimit by remember { mutableStateOf("5") }
    var billingDay by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Setup Data Plan") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text("Provider Name") },
                    placeholder = { Text("e.g., MTN, Airtel") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dataLimit,
                    onValueChange = { dataLimit = it },
                    label = { Text("Data Limit (GB)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = billingDay,
                    onValueChange = { billingDay = it },
                    label = { Text("Billing Start Day (1-31)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = dataLimit.toDoubleOrNull() ?: 5.0
                    val day = billingDay.toIntOrNull()?.coerceIn(1, 31) ?: 1
                    val providerName = provider.ifBlank { "My Provider" }
                    onConfirm(providerName, limit, day)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}