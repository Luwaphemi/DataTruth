package com.example.datatruth

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.datatruth.data.DataRepository
import com.example.datatruth.models.DataPlanModel
import com.example.datatruth.platform.DataMonitor
import com.example.datatruth.ui.*
import java.util.UUID

@Composable
fun AppNav(
    repository: DataRepository,
    dataMonitor: DataMonitor
) {
    val navController = rememberNavController()

    val enhancedViewModel: EnhancedViewModel = viewModel {
        EnhancedViewModel(
            repository = repository,
            dataMonitor = dataMonitor
        )
    }

    val uiState by enhancedViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        /* ---------------- Splash ---------------- */
        composable("splash") {
            SplashScreen(
                onFinished = {
                    navController.navigate("welcome") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        /* ---------------- Welcome ---------------- */
        composable("welcome") {
            WelcomeScreen(
                logo = {
                    Image(
                        painter = painterResource(R.drawable.ic_datatruth_logo),
                        contentDescription = "DataTruth Logo",
                        modifier = Modifier.size(180.dp)
                    )
                },
                onGetStarted = {
                    navController.navigate("dashboard") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        /* ---------------- Dashboard ---------------- */
        composable("dashboard") {
            FullDashboard(
                uiState = uiState,

                logo = {
                    Image(
                        painter = painterResource(R.drawable.ic_datatruth_logo),
                        contentDescription = "DataTruth Logo",
                        modifier = Modifier.size(96.dp)
                    )
                },

                onCaptureUsage = {
                    enhancedViewModel.captureCurrentUsage()
                },

                onFetchProvider = {
                    enhancedViewModel.fetchProviderData()
                },

                onSetupPlan = { providerName, limitGb, billingDay ->
                    enhancedViewModel.setupDataPlan(
                        DataPlanModel(
                            id = UUID.randomUUID().toString(),
                            providerName = providerName,
                            dataLimitBytes = (limitGb * 1024 * 1024 * 1024).toLong(),
                            billingCycleStartDay = billingDay,
                            discrepancyThresholdPercentage = 5.0
                        )
                    )
                },

                        onRefresh = {
                    enhancedViewModel.loadAllData()
                },

                onClearMessages = {
                    enhancedViewModel.clearMessages()
                }
            )
        }
    }
}
