package com.example.datatruth

import androidx.compose.runtime.*
import com.example.datatruth.data.DataRepository
import com.example.datatruth.data.DatabaseDriverFactory
import com.example.datatruth.db.DataTruthDatabase
import com.example.datatruth.platform.IOSDataMonitor
import com.example.datatruth.ui.FullDashboard
import com.example.datatruth.ui.EnhancedViewModel
import com.example.datatruth.models.DataPlanModel
import kotlinx.datetime.Clock
import kotlin.random.Random

@Composable
fun IosApp() {
    // Create database with driver
    val driver = DatabaseDriverFactory().createDriver()
    val database = DataTruthDatabase(driver)
    val repository = DataRepository(database)
    val dataMonitor = IOSDataMonitor()
    
    // Create ViewModel
    val viewModel = remember { EnhancedViewModel(repository, dataMonitor) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Show Dashboard directly (simplified for iOS MVP)
    FullDashboard(
        uiState = uiState,
        logo = { /* No logo for iOS MVP */ },
        onCaptureUsage = { viewModel.captureCurrentUsage() },
        onFetchProvider = { viewModel.fetchProviderData() },
        onSetupPlan = { providerName, limitGb, billingDay ->
            viewModel.setupDataPlan(
                DataPlanModel(
                    id = "ios_plan_${Clock.System.now().toEpochMilliseconds()}",
                    providerName = providerName,
                    dataLimitBytes = (limitGb * 1024 * 1024 * 1024).toLong(),
                    billingCycleStartDay = billingDay,
                    discrepancyThresholdPercentage = 5.0
                )
            )
        },
        onRefresh = { viewModel.loadAllData() },
        onClearMessages = { viewModel.clearMessages() }
    )
}